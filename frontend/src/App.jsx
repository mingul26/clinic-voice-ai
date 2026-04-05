import { useState } from 'react'
import Sidebar from './components/Sidebar'
import AppointmentsTable from './components/AppointmentsTable'
import CallLogsTable from './components/CallLogsTable'
import AvailabilityView from './components/AvailabilityView'
import ToastContainer from './components/ToastContainer'

export default function App() {
  const [activeTab, setActiveTab] = useState('appointments')
  const [toasts, setToasts] = useState([])

  const addToast = (message, type = 'info') => {
    const id = Date.now()
    setToasts((prev) => [...prev, { id, message, type }])
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 4000)
  }

  return (
    <div className="layout">
      <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />
      <main className="main-content">
        {activeTab === 'appointments' && (
          <AppointmentsTable onToast={addToast} />
        )}
        {activeTab === 'call-logs' && (
          <CallLogsTable onToast={addToast} />
        )}
        {activeTab === 'availability' && (
          <AvailabilityView onToast={addToast} />
        )}
      </main>
      <ToastContainer toasts={toasts} />
    </div>
  )
}
