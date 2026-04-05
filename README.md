# Clinic Voice AI

An AI-powered outbound calling system for diagnostic clinics, built on the Bolna voice agent platform. The system automatically calls patients to confirm reports, reschedule appointments, and handle callbacks — replacing manual phone follow-ups with a natural, conversational voice experience.

---

## The Problem

Diagnostic labs and clinics lose significant revenue and patient trust due to poor follow-up. When a patient's report is ready, staff must manually call them, navigate busy lines, leave voicemails, and repeat attempts across shifts. Appointment no-shows go unaddressed. Reschedules require back-and-forth coordination that ties up front-desk staff.

This creates three compounding problems:

- **Delayed care** — patients don't know their results are ready, delaying treatment decisions
- **Wasted capacity** — unfilled appointment slots that could have been recovered with timely outreach
- **Staff burnout** — receptionists spending hours on repetitive calls instead of in-person patient support

---

## What It Does

Clinic Voice AI automates outbound patient calls through a voice agent named **Shilpa**. When triggered, Shilpa calls the patient, introduces herself, and handles the full interaction — from confirming identity to booking a slot — in the patient's preferred language.

### Call Reasons

| Reason | What Shilpa Does |
|---|---|
| **Report Ready** | Informs the patient their report is ready, summarises key context (e.g. "your blood work is in"), and books a follow-up consultation |
| **Reschedule** | Apologises for the cancellation, offers alternative slots, and books a new appointment |
| **Callback** | Picks up where the patient left off, understands their need, and assists accordingly |

### Booking Flow

1. Shilpa asks whether the patient wants the earliest slot, a specific date, or a range
2. Calls the live availability API to fetch real open slots (IST timezone, doctor-specific)
3. Presents 2–3 options conversationally — no raw datetime strings, no technical jargon
4. Waits for explicit confirmation before booking
5. Books the slot, removes it from availability, and confirms with the patient

### What Gets Logged

Every call — regardless of outcome — is logged with status, transcript, patient, and doctor. The dashboard shows the full call history, appointment outcomes, and availability in real time.

---

## Impact on the Clinic

| Metric | Before | With Clinic Voice AI |
|---|---|---|
| Report follow-up time | Hours to days (manual calls) | Minutes (automated, 24/7) |
| Appointment recovery rate | Depends on staff bandwidth | Every cancellation triggers an immediate rescheduling call |
| Staff time on calls | 2–4 hours/day on follow-ups | Near zero — Shilpa handles it |
| Patient experience | Missed calls, no callbacks | Personalised, language-aware, conversational |
| Slot utilisation | Gaps from no-shows and cancellations | Cancelled slots are instantly freed and re-offered |

---

## Architecture

```
Frontend (React + Vite)          Backend (Spring Boot 3.2)         Bolna Platform
─────────────────────            ──────────────────────────        ──────────────
Dashboard UI              ──▶   REST API (port 8080)        ──▶   Voice Agent (Shilpa)
  Appointments tab               /api/calls/outbound                 calls patient
  Call Logs tab                  /api/availability                   checks slots (tool)
  Availability tab               /api/appointments/book              books slot (tool)
                          ◀──   /api/webhook/bolna          ◀──   post-call webhook
                                 PostgreSQL (Railway)
```

### Key Design Decisions

- **Slots as a first-class table** — `doctor_slot` table with `available` flag, not a JSON blob. Booking marks a slot unavailable; cancellation restores it.
- **IST timezone at the API layer** — all slot times are returned with `+05:30` offset so the voice agent always presents the correct local time to patients.
- **Webhook upsert** — Bolna fires multiple events per call (initiated → ringing → in-progress → completed). The backend upserts on `bolnaCallId` so the dashboard shows one row per call, not one per event.
- **Context-aware agent** — patient name, doctor, call reason, and clinical context are injected at call time via Bolna's `user_data`. Shilpa uses them naturally without reading out IDs or raw data.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, Vite, Axios |
| Backend | Spring Boot 3.2, Java 17, JPA/Hibernate |
| Database | PostgreSQL |
| Voice AI | Bolna (outbound calls, STT, TTS, tool calling) |
| Hosting | Railway (backend + DB), Vercel (frontend) |

---

## Live Demo

- **Frontend:** https://clinic-voice-ai.vercel.app
- **Backend API:** https://clinic-voice-ai-production.up.railway.app/api/doctors

---

## Local Setup

**Prerequisites:** Java 17+, Node 18+, PostgreSQL

```bash
# Backend
cd backend
export DB_URL=jdbc:postgresql://localhost:5432/clinic
export DB_USER=clinic
export DB_PASS=clinic
export BOLNA_API_KEY=your_key
export BOLNA_AGENT_ID=your_agent_id
export BOLNA_VERIFIED_NUMBER=+91xxxxxxxxxx
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm run dev
```

Frontend runs on http://localhost:5173, backend on http://localhost:8080.

For the voice agent to reach your local backend during development, expose port 8080 via a tunnel (e.g. `ngrok http 8080`) and set the webhook + tool URLs in the Bolna dashboard to the tunnel URL.

---

## Seed Data

Three doctors and three patients are pre-loaded. Slots are seeded Mon–Fri from April 2026 through May 2026.

| Doctor | Specialization | Slot Times |
|---|---|---|
| Dr. Priya Sharma | Pathology | 9 AM, 10 AM, 11 AM |
| Dr. Arjun Mehta | Radiology | 10 AM, 2 PM, 4 PM |
| Dr. Sunita Rao | General Medicine | 8 AM, 11 AM, 3 PM |

| Patient | Phone | Language |
|---|---|---|
| Ravi Kumar | +919876543210 | Hindi |
| Meena Patel | +919123456789 | Hinglish |
| Suresh Nair | +919988776655 | English |

---

## API Reference

See [backend/README.md](backend/README.md) for the full API reference including request/response shapes, error formats, and Bolna tool configuration.
