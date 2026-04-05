import { useState, useEffect } from 'react'
import { triggerOutboundCall, getDoctors, getPatients } from '../api'

export default function TriggerCallModal({ onClose, onSuccess }) {
  const [doctors, setDoctors]   = useState([])
  const [patients, setPatients] = useState([])
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState(null)

  const [form, setForm] = useState({
    patientPhone: '',
    patientName: '',
    language: 'Hindi',
    doctorId: '',
  })

  useEffect(() => {
    Promise.all([getDoctors(), getPatients()]).then(([d, p]) => {
      setDoctors(d.data)
      setPatients(p.data)
    })
  }, [])

  const handlePatientChange = (e) => {
    const patient = patients.find((p) => String(p.id) === e.target.value)
    if (patient) {
      setForm((f) => ({
        ...f,
        patientPhone: patient.phone,
        patientName: patient.name,
        language: patient.language || 'Hindi',
      }))
    }
  }

  const handleChange = (e) => {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const payload = {
        patientPhone: form.patientPhone,
        patientName: form.patientName,
        language: form.language,
        ...(form.doctorId && { doctorId: Number(form.doctorId) }),
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
      <div className="modal">
        <h2 className="modal-title">📞 Trigger Outbound Call</h2>

        {error && <div className="error-banner">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Select Existing Patient</label>
            <select className="form-select" onChange={handlePatientChange} defaultValue="">
              <option value="">— pick a patient to auto-fill —</option>
              {patients.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} ({p.phone})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Patient Name *</label>
            <input
              className="form-input"
              name="patientName"
              value={form.patientName}
              onChange={handleChange}
              placeholder="e.g. Ravi Kumar"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Phone Number *</label>
            <input
              className="form-input"
              name="patientPhone"
              value={form.patientPhone}
              onChange={handleChange}
              placeholder="+919876543210"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Language</label>
            <select className="form-select" name="language" value={form.language} onChange={handleChange}>
              <option>Hindi</option>
              <option>English</option>
              <option>Gujarati</option>
              <option>Tamil</option>
              <option>Telugu</option>
              <option>Marathi</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Doctor Preference</label>
            <select className="form-select" name="doctorId" value={form.doctorId} onChange={handleChange}>
              <option value="">— no preference —</option>
              {doctors.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name} ({d.specialization})
                </option>
              ))}
            </select>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <><span className="spinner" /> Initiating...</> : '📞 Trigger Call'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
