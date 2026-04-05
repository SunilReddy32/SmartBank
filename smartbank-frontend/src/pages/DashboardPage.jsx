import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getDashboard } from '../api/dashboard'
import Layout from '../components/Layout'
import StatCard from '../components/StatCard'
import TransactionBadge from '../components/TransactionBadge'

export default function DashboardPage() {
  const { user, loginUser } = useAuth()
  const navigate = useNavigate()
  const [data, setData]     = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        // If we don't have userId yet, find it by fetching all users isn't ideal
        // Instead: after login store userId from a dedicated endpoint or profile fetch
        // We'll resolve userId by calling GET /users/me equivalent — but our backend
        // doesn't have /me. We use the stored userId if available, else prompt re-login.
        let userId = user?.userId
        if (!userId) {
          // Try to get userId from accounts endpoint which requires auth
          // The simplest approach: re-use the email stored to look up via admin (not available)
          // Better: store userId at login time — which we now do via localStorage
          const stored = localStorage.getItem('userId')
          if (stored && stored !== 'null') {
            userId = Number(stored)
            loginUser({ ...user, userId: Number(stored) })
          }
        }

        if (!userId) {
          navigate('/login')
          return
        }

        const res = await getDashboard(userId)
        setData(res.data)

        // Also update name in auth context
        if (res.data.userName) {
          loginUser({ ...user, userId, name: res.data.userName })
        }
      } catch (err) {
        console.error(err)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  if (loading) return (
    <Layout>
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
      </div>
    </Layout>
  )

  return (
    <Layout>
      <div className="max-w-6xl mx-auto">
        {/* Page header */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-gray-800">
            Good day, {data?.userName || user?.name} 👋
          </h1>
          <p className="text-gray-500 text-sm mt-1">Here's your financial overview</p>
        </div>

        {/* Stat cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-5 mb-8">
          <StatCard icon="💰" label="Total Balance"
            value={`₹${data?.totalBalance?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
            color="green" />
          <StatCard icon="🏦" label="Total Accounts"   value={data?.totalAccounts} color="blue" />
          <StatCard icon="📋" label="Recent Transactions" value={data?.recentTransactions?.length} color="purple" />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Accounts */}
          <div className="card">
            <div className="flex justify-between items-center mb-4">
              <h2 className="font-bold text-gray-800">Your Accounts</h2>
              <button onClick={() => navigate('/accounts')}
                className="text-sm text-blue-600 hover:underline">View all →</button>
            </div>
            <div className="space-y-3">
              {data?.accounts?.map(acc => (
                <div key={acc.id} className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                  <div>
                    <div className="font-mono text-sm text-gray-600">{acc.accountNumber}</div>
                    <div className="text-xs text-gray-400">{acc.accountType}</div>
                  </div>
                  <div className="text-right">
                    <div className="font-bold text-gray-800">
                      ₹{acc.balance?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </div>
                    <div className={`text-xs ${acc.pinSet ? 'text-green-500' : 'text-red-400'}`}>
                      {acc.pinSet ? '🔐 PIN set' : '⚠️ PIN not set'}
                    </div>
                  </div>
                </div>
              ))}
              {!data?.accounts?.length && (
                <p className="text-gray-400 text-sm text-center py-4">
                  No accounts yet.{' '}
                  <button onClick={() => navigate('/accounts')} className="text-blue-600 hover:underline">
                    Create one
                  </button>
                </p>
              )}
            </div>
          </div>

          {/* Recent transactions */}
          <div className="card">
            <div className="flex justify-between items-center mb-4">
              <h2 className="font-bold text-gray-800">Recent Transactions</h2>
              <button onClick={() => navigate('/transactions')}
                className="text-sm text-blue-600 hover:underline">View all →</button>
            </div>
            <div className="space-y-3">
              {data?.recentTransactions?.map(tx => (
                <div key={tx.transactionId} className="flex justify-between items-center">
                  <div>
                    <TransactionBadge type={tx.type} />
                    <div className="text-xs text-gray-400 mt-1">
                      {tx.createdAt ? new Date(tx.createdAt).toLocaleString('en-IN') : '—'}
                    </div>
                  </div>
                  <div className="font-semibold text-gray-800">
                    ₹{tx.amount?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </div>
                </div>
              ))}
              {!data?.recentTransactions?.length && (
                <p className="text-gray-400 text-sm text-center py-4">No recent transactions</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </Layout>
  )
}