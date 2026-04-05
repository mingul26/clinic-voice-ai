import { useState, useEffect } from 'react'
import { getDoctors, getAvailability } from '../api'

export default function AvailabilityView({ onToast }) {
  const [doctors, setDoctors]   = useState([])
  const [doctorId, setDoctorId] = useState('')
  const [date, setDate]         = useState('')
  const [slots, setSlots]       = useState([])
  const [loading, setLoading]   = useState(false)
  const [searched, setSearched] = useState(false)

  useEffect(() => {
    getDoctors().then((res) => setDoctors(res.data)).catch((err) => onToast(err.message, 'error'))
  }, [onToast])

  const handleSearch = async (e) => {
    e.preventDefault()
    if (!doctorId) {
      onToast('Please select a doctor', 'error')
      return
    }
    setLoading(true)
    setSearched(true)
    try {
      const res = await getAvailability(doctorId, date || undefined)
      setSlots(res.data.slots || [])
    } catch (err) {
      onToast(err.message, 'error')
    } finally {
      setLoading(false)
    }
  }

  function formatSlot(iso) {
    const utc = iso.endsWith('Z') ? iso : iso + 'Z'
    return new Date(utc).toLocaleString('en-IN', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      timeZone: 'Asia/Kolkata',
    })
  }

  const selectedDoctor = doctors.find((d) => String(d.id) === doctorId)

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Availability</h1>
          <p className="page-subtitle">Check open slots by doctor and date</p>
        </div>
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: 16, alignItems: 'flex-end', flexWrap: 'wrap' }}>
          <div className="form-group" style={{ flex: '1 1 200px', marginBottom: 0 }}>
            <label className="form-label">Doctor *</label>
            <select
              className="form-select"
              value={doctorId}
              onChange={(e) => { setDoctorId(e.target.value); setSearched(false) }}
            >
              <option value="">— select doctor —</option>
              {doctors.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name} — {d.specialization}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group" style={{ flex: '1 1 160px', marginBottom: 0 }}>
            <label className="form-label">Date (optional)</label>
            <input
              type="date"
              className="form-input"
              value={date}
              onChange={(e) => { setDate(e.target.value); setSearched(false) }}
            />
          </div>

          <button type="submit" className="btn btn-primary" disabled={loading} style={{ marginBottom: 0 }}>
            {loading ? <><span className="spinner" /> Checking...</> : '🔍 Check Slots'}
          </button>
        </form>
      </div>

      {searched && (
        <div className="card">
          <div className="section-header">
            <div>
              <div className="section-title">
                {selectedDoctor ? `${selectedDoctor.name} — ${selectedDoctor.specialization}` : 'Results'}
              </div>
              {date && (
                <div style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 2 }}>
                  Filtered by: {new Date(date + 'T00:00').toLocaleDateString('en-IN', { dateStyle: 'long' })}
                </div>
              )}
            </div>
            <span className="badge badge-confirmed">{slots.length} slots</span>
          </div>

          {slots.length === 0 ? (
            <div className="empty-state" style={{ padding: '32px 0' }}>
              <p>No available slots{date ? ' on this date' : ''}.</p>
            </div>
          ) : (
            <div className="slot-grid">
              {slots.map((slot, i) => (
                <div key={i} className="slot-chip">
                  {formatSlot(slot)}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {!searched && doctors.length > 0 && (
        <div className="card">
          <div className="section-header">
            <span className="section-title">All Doctors</span>
          </div>
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Name</th>
                  <th>Specialization</th>
                </tr>
              </thead>
              <tbody>
                {doctors.map((d) => (
                  <tr key={d.id} style={{ cursor: 'pointer' }} onClick={() => setDoctorId(String(d.id))}>
                    <td style={{ color: 'var(--text-muted)' }}>{d.id}</td>
                    <td>{d.name}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>{d.specialization}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </>
  )
}
