import { useState, useEffect } from 'react'
import { triggerOutboundCall, getDoctors, getPatients } from '../api'

const CALL_REASONS = [
  {
    value: 'REPORT_READY',
    label: 'Report Ready',
    icon: '🧪',
    placeholder: 'e.g. Blood test report dated April 3rd is ready',
  },
  {
    value: 'RESCHEDULE',
    label: 'Reschedule',
    icon: '📅',
    placeholder: 'e.g. Appointment on April 10 at 9AM has been cancelled',
  },
  {
    value: 'CALLBACK',
    label: 'Callback Request',
    icon: '📲',
    placeholder: 'e.g. Patient requested a callback regarding test results',
  },
]

export default function TriggerCallModal({ onClose, onSuccess }) {
  const [doctors, setDoctors]   = useState([])
  const [patients, setPatients] = useState([])
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState(null)

  const [form, setForm] = useState({
    patientId: '',
    doctorId: '',
    callReason: 'REPORT_READY',
    context: '',
  })

  useEffect(() => {
    Promise.all([getDoctors(), getPatients()]).then(([d, p]) => {
      setDoctors(d.data)
      setPatients(p.data)
    })
  }, [])

  const handleChange = (e) => {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }))
  }

  const selectedReason = CALL_REASONS.find((r) => r.value === form.callReason)
  const selectedPatient = patients.find((p) => String(p.id) === form.patientId)
  const selectedDoctor  = doctors.find((d) => String(d.id) === form.doctorId)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.patientId) { setError('Please select a patient'); return }
    if (!form.doctorId)  { setError('Please select a doctor');  return }
    setLoading(true)
    setError(null)
    try {
      const payload = {
        patientId:    Number(form.patientId),
        patientName:  selectedPatient?.name  || '',
        patientPhone: selectedPatient?.phone || '',
        language:     selectedPatient?.language || 'Hindi',
        doctorId:     Number(form.doctorId),
        callReason:   form.callReason,
        context:      form.context,
      }
      const res = await triggerOutboundCall(payload)
      onSuccess(`Call initiated! ID: ${res.data.callId}`)
      onClose()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal" style={{ width: 500 }}>
        <h2 className="modal-title">📞 Initiate Outbound Call</h2>

        {error && <div className="error-banner">{error}</div>}

        <form onSubmit={handleSubmit}>
          {/* Patient */}
          <div className="form-group">
            <label className="form-label">Patient *</label>
            <select
              className="form-select"
              name="patientId"
              value={form.patientId}
              onChange={handleChange}
              required
            >
              <option value="">— select patient —</option>
              {patients.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} · {p.phone} · {p.language}
                </option>
              ))}
            </select>
          </div>

          {/* Doctor */}
          <div className="form-group">
            <label className="form-label">Doctor *</label>
            <select
              className="form-select"
              name="doctorId"
              value={form.doctorId}
              onChange={handleChange}
              required
            >
              <option value="">— select doctor —</option>
              {doctors.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name} — {d.specialization}
                </option>
              ))}
            </select>
          </div>

          {/* Call Reason */}
          <div className="form-group">
            <label className="form-label">Call Reason *</label>
            <div style={{ display: 'flex', gap: 8 }}>
              {CALL_REASONS.map((r) => (
                <button
                  key={r.value}
                  type="button"
                  onClick={() => setForm((f) => ({ ...f, callReason: r.value, context: '' }))}
                  style={{
                    flex: 1,
                    padding: '8px 4px',
                    borderRadius: 'var(--radius)',
                    border: '1px solid',
                    borderColor: form.callReason === r.value ? 'var(--accent)' : 'var(--border)',
                    background: form.callReason === r.value ? 'rgba(99,102,241,0.15)' : 'var(--bg-secondary)',
                    color: form.callReason === r.value ? 'var(--accent-light)' : 'var(--text-secondary)',
                    cursor: 'pointer',
                    fontSize: 12,
                    fontWeight: 500,
                    textAlign: 'center',
                    lineHeight: 1.4,
                  }}
                >
                  <div style={{ fontSize: 18, marginBottom: 2 }}>{r.icon}</div>
                  {r.label}
                </button>
              ))}
            </div>
          </div>

          {/* Context */}
          <div className="form-group">
            <label className="form-label">Context</label>
            <textarea
              className="form-input"
              name="context"
              value={form.context}
              onChange={handleChange}
              placeholder={selectedReason?.placeholder}
              rows={2}
              style={{ resize: 'vertical' }}
            />
          </div>

          {/* Preview */}
          {form.patientId && form.doctorId && (
            <div style={{
              background: 'var(--bg-secondary)',
              border: '1px solid var(--border)',
              borderRadius: 'var(--radius)',
              padding: '10px 14px',
              fontSize: 12,
              color: 'var(--text-secondary)',
              marginBottom: 16,
            }}>
              <span style={{ color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', fontSize: 10 }}>Call Preview</span>
              <div style={{ marginTop: 6, lineHeight: 1.8 }}>
                <span style={{ color: 'var(--text-primary)' }}>{selectedPatient?.name}</span> · {selectedPatient?.language} · {selectedPatient?.phone}
                <br />
                {selectedReason?.icon} {selectedReason?.label} → <span style={{ color: 'var(--text-primary)' }}>{selectedDoctor?.name}</span> ({selectedDoctor?.specialization})
                {form.context && <><br /><span style={{ color: 'var(--text-muted)' }}>"{form.context}"</span></>}
              </div>
            </div>
          )}

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <><span className="spinner" /> Initiating...</> : '📞 Initiate Call'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
