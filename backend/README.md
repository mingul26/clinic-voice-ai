# Clinic Voice AI — Backend API Reference

Spring Boot 3.2 REST API for the Bolna FSE Assignment.  
**Base URL (production):** `https://clinic-voice-ai-production.up.railway.app`  
**Base URL (local):** `http://localhost:8080`

All requests and responses use `Content-Type: application/json`.

---

## Entities

### Patient
| Field | Type | Description |
|---|---|---|
| id | Long | Auto-generated |
| name | String | Full name |
| phone | String | E.164 format e.g. `+919876543210` |
| language | String | Preferred language e.g. `Hindi`, `English` |

### Doctor
| Field | Type | Description |
|---|---|---|
| id | Long | Auto-generated |
| name | String | Full name |
| specialization | String | e.g. `Pathology`, `Radiology` |

### DoctorSlot
| Field | Type | Description |
|---|---|---|
| id | Long | Auto-generated |
| doctor | Doctor | FK to doctor |
| slotTime | LocalDateTime | ISO-8601 e.g. `2026-04-10T09:00` |
| available | boolean | `true` = open, `false` = booked |

Slots are marked `available=false` when an appointment is booked, and restored to `available=true` when an appointment is cancelled.

### Appointment
| Field | Type | Description |
|---|---|---|
| id | Long | Auto-generated |
| patient | Patient | Nested patient object |
| doctor | Doctor | Nested doctor object |
| slotTime | LocalDateTime | ISO-8601 e.g. `2026-04-10T09:00:00` |
| status | Enum | `PENDING` \| `CONFIRMED` \| `CANCELLED` |
| bolnaCallId | String | Bolna call ID that triggered this booking |

### CallLog
| Field | Type | Description |
|---|---|---|
| id | Long | Auto-generated |
| bolnaCallId | String | Bolna's call ID (maps to `id` field in Bolna webhook) |
| status | String | e.g. `completed`, `in-progress`, `ringing`, `initiated`, `call-disconnected` |
| transcript | String | Full call transcript |
| patient | Patient | Linked patient (from webhook `context_details.recipient_data`) |
| doctor | Doctor | Linked doctor (from webhook `context_details.recipient_data`) |
| appointment | Appointment | Linked appointment if booked (nullable) |
| createdAt | LocalDateTime | Timestamp (UTC, stored without timezone) |

CallLogs are **upserted** — multiple webhook events for the same call ID update the same row rather than creating duplicates.

---

## APIs

### 1. Trigger Outbound Call
Initiates a Bolna outbound call to a patient.

```
POST /api/calls/outbound
```

**Request Body:**
```json
{
  "patientId": 1,
  "doctorId": 2,
  "callReason": "REPORT_READY",
  "context": "Blood work results are in, HDL is low",
  "language": "Hindi"
}
```

| Field | Required | Description |
|---|---|---|
| patientId | ✅ | Existing patient ID — name and phone resolved from DB |
| doctorId | ✅ | Existing doctor ID — name and specialization passed to agent |
| callReason | ✅ | `REPORT_READY` \| `RESCHEDULE` \| `CALLBACK` |
| context | ❌ | Extra context passed to the Bolna agent |
| language | ❌ | Defaults to patient's preferred language |

**Response `200`:**
```json
{
  "callId": "bolna-execution-uuid",
  "message": "Outbound call initiated successfully"
}
```

---

### 2. Bolna Webhook (post-call callback)
Bolna fires this after every call status change. Upserts a CallLog and links patient/doctor from the call context.

```
POST /api/webhook/bolna
```

**Actual Bolna payload shape:**
```json
{
  "id": "bolna-call-uuid",
  "status": "completed",
  "transcript": "assistant: Hello...\nuser: ...",
  "context_details": {
    "recipient_data": {
      "patient_id": "1",
      "patient_name": "Ravi Kumar",
      "doctor_id": "2",
      "doctor_name": "Dr. Arjun Mehta",
      "call_reason": "report_ready",
      "language": "Hindi",
      "context": "low hdl"
    }
  }
}
```

**Response `200`:**
```json
{
  "received": true,
  "callLogId": 5
}
```

---

### 3. List All Appointments
```
GET /api/appointments
```

**Response `200`:** Array of Appointment objects, ordered by slot time descending.
```json
[
  {
    "id": 1,
    "patient": { "id": 1, "name": "Ravi Kumar", "phone": "+919876543210", "language": "Hindi" },
    "doctor": { "id": 2, "name": "Dr. Arjun Mehta", "specialization": "Radiology" },
    "slotTime": "2026-04-10T09:00:00",
    "status": "CONFIRMED",
    "bolnaCallId": "bolna-call-uuid"
  }
]
```

---

### 4. Book an Appointment
Books a slot and marks it unavailable in `doctor_slot`.

```
POST /api/appointments/book
```

**Request Body:**
```json
{
  "patientId": 1,
  "doctorId": 2,
  "slotTime": "2026-04-10T09:00:00",
  "bolnaCallId": "optional-call-id"
}
```

| Field | Required | Description |
|---|---|---|
| patientId | ✅ | Existing patient ID |
| doctorId | ✅ | Existing doctor ID |
| slotTime | ✅ | Accepts `2026-04-10T09:00` or `2026-04-10T09:00:00` |
| bolnaCallId | ❌ | Links this appointment to a Bolna call |

**Response `200`:** The created Appointment object.

---

### 5. Update Appointment Status
Cancelling an appointment restores the slot back to available.

```
PATCH /api/appointments/{id}/status?status=CANCELLED
```

| Query Param | Values |
|---|---|
| status | `PENDING` \| `CONFIRMED` \| `CANCELLED` |

**Response `200`:** Updated Appointment object.

---

### 6. List All Call Logs
```
GET /api/call-logs
```

**Response `200`:** Array of CallLog objects, ordered by creation time descending.
```json
[
  {
    "id": 5,
    "bolnaCallId": "bolna-call-uuid",
    "status": "completed",
    "transcript": "assistant: Hello Ravi...",
    "patient": { "id": 1, "name": "Ravi Kumar", "phone": "+919876543210" },
    "doctor": { "id": 2, "name": "Dr. Arjun Mehta", "specialization": "Radiology" },
    "appointment": null,
    "createdAt": "2026-04-06T10:05:00"
  }
]
```

---

### 7. Get Available Slots
Returns only slots where `available=true` from the `doctor_slot` table. Supports four query modes.

```
GET /api/availability
```

| Query Param | Required | Description |
|---|---|---|
| doctorId | ✅ | Doctor ID |
| asap | ❌ | `true` — returns only the single next available slot |
| date | ❌ | Exact date filter `YYYY-MM-DD` |
| from | ❌ | Start of date range `YYYY-MM-DD` (use with `to`) |
| to | ❌ | End of date range `YYYY-MM-DD` (use with `from`) |

Priority order: `asap` → `from+to` → `date` → all upcoming slots.

**Examples:**

```
# Next available slot
GET /api/availability?doctorId=1&asap=true

# Specific date
GET /api/availability?doctorId=1&date=2026-04-10

# Week range
GET /api/availability?doctorId=1&from=2026-04-13&to=2026-04-17

# All upcoming slots
GET /api/availability?doctorId=1
```

**Response `200`:**
```json
{
  "doctorId": 1,
  "slots": [
    "2026-04-10T09:00",
    "2026-04-10T10:00",
    "2026-04-10T11:00"
  ]
}
```

---

### 8. List All Doctors
```
GET /api/doctors
```

**Response `200`:** Array of Doctor objects (no slot data — use `/api/availability` for slots).

---

### 9. List All Patients
```
GET /api/patients
```

**Response `200`:** Array of Patient objects.

---

## Error Responses

```json
{
  "timestamp": "2026-04-06T10:00:00",
  "status": 404,
  "error": "Doctor not found: 99"
}
```

Validation errors (400) include a `fieldErrors` map:
```json
{
  "timestamp": "2026-04-06T10:00:00",
  "status": 400,
  "error": "Validation failed",
  "fieldErrors": {
    "patientId": "Patient ID is required"
  }
}
```

---

## Bolna Agent Configuration

**Webhook URL:**
```
POST https://clinic-voice-ai-production.up.railway.app/api/webhook/bolna
```

**Tool: check_availability**
```
GET https://clinic-voice-ai-production.up.railway.app/api/availability
Params:
  doctorId={doctorId}        required
  asap=true                  when patient says "as soon as possible"
  date={date}                specific date YYYY-MM-DD
  from={from}&to={to}        date range YYYY-MM-DD
```

**Tool: book_appointment**
```
POST https://clinic-voice-ai-production.up.railway.app/api/appointments/book
Body: { patientId, doctorId, slotTime }
```

In Bolna tool `param` bindings, use single braces: `{doctorId}`, `{slotTime}`, etc.

---

## Seed Data

**Doctors:**
| ID | Name | Specialization | Slots |
|---|---|---|---|
| 1 | Dr. Priya Sharma | Pathology | 09:00, 10:00, 11:00 Mon–Fri |
| 2 | Dr. Arjun Mehta | Radiology | 10:00, 14:00, 16:00 Mon–Fri |
| 3 | Dr. Sunita Rao | General Medicine | 08:00, 11:00, 15:00 Mon–Fri |

Slots are seeded from 2026-04-06 to 2026-05-05.

**Patients:**
| ID | Name | Phone | Language |
|---|---|---|---|
| 1 | Ravi Kumar | +919876543210 | Hindi |
| 2 | Meena Patel | +919123456789 | Hinglish |
| 3 | Suresh Nair | +919988776655 | English |
