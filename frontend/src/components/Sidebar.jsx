export default function Sidebar({ activeTab, onTabChange }) {
  const navItems = [
    { id: 'appointments', icon: '📋', label: 'Appointments' },
    { id: 'call-logs',    icon: '📞', label: 'Call Logs' },
    { id: 'availability', icon: '🕐', label: 'Availability' },
  ]

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <h2>Clinic Voice AI</h2>
        <p>Bolna FSE Assignment</p>
      </div>
      <nav>
        {navItems.map((item) => (
          <button
            key={item.id}
            className={`nav-item ${activeTab === item.id ? 'active' : ''}`}
            onClick={() => onTabChange(item.id)}
          >
            <span className="nav-icon">{item.icon}</span>
            {item.label}
          </button>
        ))}
      </nav>
    </aside>
  )
}
