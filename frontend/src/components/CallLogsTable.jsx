import { useState, useEffect, useCallback } from 'react'
import { getCallLogs } from '../api'

const STATUS_BADGE = {
  completed: 'badge-completed',
  failed:    'badge-failed',
  initiated: 'badge-initiated',
}

function formatDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  })
}

export default function CallLogsTable({ onToast }) {
  const [logs, setLogs]     = useState([])
  const [loading, setLoading] = useState(true)
  const [expanded, setExpanded] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getCallLogs()
      setLogs(res.data)
    } catch (err) {
      onToast(err.message, 'error')
    } finally {
      setLoading(false)
    }
  }, [onToast])

  useEffect(() => { load() }, [load])

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Call Logs</h1>
          <p className="page-subtitle">All Bolna voice calls and their outcomes</p>
        </div>
        <button className="btn btn-secondary" onClick={load} disabled={loading}>
          {loading ? <span className="spinner" /> : '↻'} Refresh
        </button>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-label">Total Calls</div>
          <div className="stat-value">{logs.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Completed</div>
          <div className="stat-value" style={{ color: 'var(--accent-light)' }}>
            {logs.filter((l) => l.status === 'completed').length}
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Failed</div>
          <div className="stat-value" style={{ color: 'var(--danger)' }}>
            {logs.filter((l) => l.status === 'failed').length}
          </div>
        </div>
      </div>

      <div className="card">
        <div className="table-wrapper">
          {loading ? (
            <div className="empty-state"><span className="spinner" /></div>
          ) : logs.length === 0 ? (
            <div className="empty-state">
              <p>No call logs yet. Trigger a call to see results here.</p>
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Call ID</th>
                  <th>Status</th>
                  <th>Patient</th>
                  <th>Doctor</th>
                  <th>Booked Slot</th>
                  <th>Time</th>
                  <th>Transcript</th>
                </tr>
              </thead>
              <tbody>
                {logs.map((log) => (
                  <>
                    <tr key={log.id}>
                      <td style={{ color: 'var(--text-muted)' }}>{log.id}</td>
                      <td style={{ fontFamily: 'monospace', fontSize: 11, color: 'var(--text-muted)' }}>
                        {log.bolnaCallId}
                      </td>
                      <td>
                        <span className={`badge ${STATUS_BADGE[log.status] || 'badge-initiated'}`}>
                          {log.status}
                        </span>
                      </td>
                      <td>{log.appointment?.patient?.name || '—'}</td>
                      <td>{log.appointment?.doctor?.name || '—'}</td>
                      <td>
                        {log.appointment?.slotTime
                          ? new Date(log.appointment.slotTime).toLocaleString('en-IN', {
                              dateStyle: 'medium', timeStyle: 'short',
                            })
                          : '—'}
                      </td>
                      <td>{formatDate(log.createdAt)}</td>
                      <td>
                        {log.transcript && (
                          <button
                            className="btn btn-secondary"
                            style={{ padding: '3px 10px', fontSize: 11 }}
                            onClick={() => setExpanded(expanded === log.id ? null : log.id)}
                          >
                            {expanded === log.id ? 'Hide' : 'View'}
                          </button>
                        )}
                      </td>
                    </tr>
                    {expanded === log.id && (
                      <tr key={`${log.id}-transcript`}>
                        <td colSpan={8} style={{ padding: '0 14px 14px' }}>
                          <div className="transcript-box">{log.transcript}</div>
                        </td>
                      </tr>
                    )}
                  </>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  )
}
