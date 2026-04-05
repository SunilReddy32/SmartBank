import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'

import LoginPage       from './pages/LoginPage'
import RegisterPage    from './pages/RegisterPage'
import DashboardPage   from './pages/DashboardPage'
import AccountsPage    from './pages/AccountsPage'
import TransactionsPage from './pages/TransactionsPage'
import TransferPage    from './pages/TransferPage'
import LoansPage       from './pages/LoansPage'
import AnalyticsPage   from './pages/AnalyticsPage'
import AdminPage       from './pages/AdminPage'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public routes */}
          <Route path="/login"    element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected user routes */}
          <Route path="/dashboard"    element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
          <Route path="/accounts"     element={<ProtectedRoute><AccountsPage /></ProtectedRoute>} />
          <Route path="/transactions" element={<ProtectedRoute><TransactionsPage /></ProtectedRoute>} />
          <Route path="/transfer"     element={<ProtectedRoute><TransferPage /></ProtectedRoute>} />
          <Route path="/loans"        element={<ProtectedRoute><LoansPage /></ProtectedRoute>} />
          <Route path="/analytics"    element={<ProtectedRoute><AnalyticsPage /></ProtectedRoute>} />

          {/* Admin-only route */}
          <Route path="/admin" element={
            <ProtectedRoute adminOnly>
              <AdminPage />
            </ProtectedRoute>
          } />

          {/* Default redirect */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}