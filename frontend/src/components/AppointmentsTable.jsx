import { useState, useEffect, useCallback } from 'react'
import { getAppointments, updateAppointmentStatus } from '../api'
import TriggerCallModal from './TriggerCallModal'

const STATUS_BADGE = {
  CONFIRMED: 'badge-confirmed',
  PENDING:   'badge-pending',
  CANCELLED: 'badge-cancelled',
}

function formatSlot(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

export default function AppointmentsTable({ onToast }) {
  const [appointments, setAppointments] = useState([])
  const [loading, setLoading]           = useState(true)
  const [showModal, setShowModal]       = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getAppointments()
      setAppointments(res.data)
    } catch (err) {
      onToast(err.message, 'error')
    } finally {
      setLoading(false)
    }
  }, [onToast])

  useEffect(() => { load() }, [load])

  const handleCancel = async (id) => {
    try {
      await updateAppointmentStatus(id, 'CANCELLED')
      onToast('Appointment cancelled', 'success')
      load()
    } catch (err) {
      onToast(err.message, 'error')
    }
  }

  const stats = {
    total:     appointments.length,
    confirmed: appointments.filter((a) => a.status === 'CONFIRMED').length,
    pending:   appointments.filter((a) => a.status === 'PENDING').length,
    cancelled: appointments.filter((a) => a.status === 'CANCELLED').length,
  }

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Appointments</h1>
          <p className="page-subtitle">All booked and pending appointments</p>
        </div>
        <div style={{ display: 'flex', gap: 10 }}>
          <button className="btn btn-secondary" onClick={load} disabled={loading}>
            {loading ? <span className="spinner" /> : '↻'} Refresh
          </button>
          <button className="btn btn-primary" onClick={() => setShowModal(true)}>
            + Trigger Call
          </button>
        </div>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">Total</div>
          <div className="stat-value">{stats.total}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Confirmed</div>
          <div className="stat-value" style={{ color: 'var(--success)' }}>{stats.confirmed}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Pending</div>
          <div className="stat-value" style={{ color: 'var(--warning)' }}>{stats.pending}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Cancelled</div>
          <div className="stat-value" style={{ color: 'var(--danger)' }}>{stats.cancelled}</div>
        </div>
      </div>

      <div className="card">
        <div className="table-wrapper">
          {loading ? (
            <div className="empty-state"><span className="spinner" /></div>
          ) : appointments.length === 0 ? (
            <div className="empty-state">
              <p>No appointments yet. Trigger a call to get started.</p>
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Patient</th>
                  <th>Phone</th>
                  <th>Doctor</th>
                  <th>Specialization</th>
                  <th>Slot</th>
                  <th>Status</th>
                  <th>Call ID</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {appointments.map((a) => (
                  <tr key={a.id}>
                    <td style={{ color: 'var(--text-muted)' }}>{a.id}</td>
                    <td>{a.patient?.name}</td>
                    <td style={{ color: 'var(--text-secondary)', fontFamily: 'monospace' }}>
                      {a.patient?.phone}
                    </td>
                    <td>{a.doctor?.name}</td>
                    <td style={{ color: 'var(--text-secondary)' }}>{a.doctor?.specialization}</td>
                    <td>{formatSlot(a.slotTime)}</td>
                    <td>
                      <span className={`badge ${STATUS_BADGE[a.status] || ''}`}>
                        {a.status}
                      </span>
                    </td>
                    <td style={{ color: 'var(--text-muted)', fontFamily: 'monospace', fontSize: 11 }}>
                      {a.bolnaCallId || '—'}
                    </td>
                    <td>
                      {a.status !== 'CANCELLED' && (
                        <button
                          className="btn btn-danger"
                          style={{ padding: '4px 10px', fontSize: 11 }}
                          onClick={() => handleCancel(a.id)}
                        >
                          Cancel
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {showModal && (
        <TriggerCallModal
          onClose={() => setShowModal(false)}
          onSuccess={(msg) => {
            onToast(msg, 'success')
            load()
          }}
        />
      )}
    </>
  )
}
