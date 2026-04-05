import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import toast from 'react-hot-toast'

const navItems = [
  { to: '/dashboard',     label: '🏠 Dashboard'     },
  { to: '/accounts',      label: '🏦 Accounts'      },
  { to: '/transactions',  label: '📋 Transactions'  },
  { to: '/transfer',      label: '💸 Transfer'      },
  { to: '/loans',         label: '📄 Loans'         },
  { to: '/analytics',     label: '📊 Analytics'     },
]

export default function Sidebar() {
  const { user, logout, isAdmin } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    toast.success('Logged out successfully')
    navigate('/login')
  }

  return (
    <aside className="w-64 min-h-screen bg-blue-900 text-white flex flex-col">
      {/* Logo */}
      <div className="p-6 border-b border-blue-800">
        <h1 className="text-2xl font-bold text-white">💳 SmartBank</h1>
        <p className="text-blue-300 text-sm mt-1">Welcome, {user?.name || 'User'}</p>
      </div>

      {/* Nav */}
      <nav className="flex-1 p-4 space-y-1">
        {navItems.map(({ to, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `block px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-blue-700 text-white'
                  : 'text-blue-200 hover:bg-blue-800 hover:text-white'
              }`
            }
          >
            {label}
          </NavLink>
        ))}

        {isAdmin && (
          <NavLink
            to="/admin"
            className={({ isActive }) =>
              `block px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-yellow-600 text-white'
                  : 'text-yellow-300 hover:bg-blue-800 hover:text-white'
              }`
            }
          >
            🛡️ Admin Panel
          </NavLink>
        )}
      </nav>

      {/* User + Logout */}
      <div className="p-4 border-t border-blue-800">
        <div className="text-xs text-blue-400 mb-2">{user?.email}</div>
        <div className="text-xs text-blue-400 mb-3">
          {user?.role === 'ROLE_ADMIN' ? '🛡️ Administrator' : '👤 User'}
        </div>
        <button onClick={handleLogout} className="w-full btn-danger text-sm py-2">
          🚪 Logout
        </button>
      </div>
    </aside>
  )
}