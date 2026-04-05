import { useEffect, useState, useCallback } from 'react'
import { useAuth } from '../context/AuthContext'
import { getAnalytics } from '../api/analytics'
import Layout from '../components/Layout'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  PieChart, Pie, Cell, ResponsiveContainer
} from 'recharts'
import toast from 'react-hot-toast'

const COLORS = ['#3b82f6','#ef4444','#8b5cf6','#10b981','#f59e0b','#06b6d4']

const TYPE_LABELS = {
  DEPOSIT: 'Deposits', WITHDRAW: 'Withdrawals', TRANSFER: 'Transfers',
  INTEREST_CREDIT: 'Interest', EMI_PAYMENT: 'EMI Payments', LOAN_DISBURSEMENT: 'Loans Received'
}

export default function AnalyticsPage() {
  const { user } = useAuth()
  const [data, setData]     = useState(null)
  const [months, setMonths] = useState(6)
  const [loading, setLoading] = useState(true)

const load = useCallback(async () => {
    if (!user?.userId) return
    setLoading(true)
    try {
      const res = await getAnalytics(user.userId, months)
      setData(res.data)
    } catch { toast.error('Failed to load analytics') }
    finally { setLoading(false) }
}, [user, months])

 useEffect(() => { load() }, [load])

  // Pie chart data
  const pieData = data ? Object.entries(data.breakdownByType).map(([type, value]) => ({
    name: TYPE_LABELS[type] || type, value
  })) : []

  // Bar chart data — monthly breakdown
  const barData = data?.monthlyBreakdown?.map(m => ({
    month: m.monthLabel,
    Deposits:    m.byType?.DEPOSIT || 0,
    Withdrawals: m.byType?.WITHDRAW || 0,
    Transfers:   m.byType?.TRANSFER || 0,
  })) || []

  const fmt = (n) => `₹${Number(n || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`

  return (
    <Layout>
      <div className="max-w-5xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Spending Analytics</h1>
            <p className="text-gray-500 text-sm">{data?.period || ''}</p>
          </div>
          <div className="flex items-center gap-2">
            <label className="text-sm text-gray-600">Period:</label>
            <select className="input w-auto text-sm" value={months} onChange={e => setMonths(Number(e.target.value))}>
              {[1,3,6,12].map(m => <option key={m} value={m}>Last {m} month{m>1?'s':''}</option>)}
            </select>
          </div>
        </div>

        {loading ? (
          <div className="flex justify-center py-24">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600" />
          </div>
        ) : (
          <>
            {/* Summary cards */}
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-6">
              {[
                { label: 'Total Deposited',    value: data?.totalDeposited,    color: 'text-green-600', icon: '⬇️' },
                { label: 'Total Withdrawn',    value: data?.totalWithdrawn,    color: 'text-red-600',   icon: '⬆️' },
                { label: 'Total Transferred',  value: data?.totalTransferred,  color: 'text-blue-600',  icon: '↔️' },
                { label: 'Interest Earned',    value: data?.totalInterestEarned, color: 'text-purple-600', icon: '💰' },
                { label: 'EMI Paid',           value: data?.totalEmiPaid,      color: 'text-orange-600', icon: '🏦' },
                { label: 'Net Flow',           value: data?.netFlow,           color: data?.netFlow >= 0 ? 'text-green-600' : 'text-red-600', icon: '📊' },
              ].map(({ label, value, color, icon }) => (
                <div key={label} className="card">
                  <div className="text-2xl mb-1">{icon}</div>
                  <div className={`text-xl font-bold ${color}`}>{fmt(value)}</div>
                  <div className="text-xs text-gray-500 mt-1">{label}</div>
                </div>
              ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Bar chart */}
              <div className="card">
                <h2 className="font-bold text-gray-800 mb-4">Monthly Breakdown</h2>
                {barData.length === 0 ? (
                  <div className="text-center py-12 text-gray-400">No data available</div>
                ) : (
                  <ResponsiveContainer width="100%" height={280}>
                    <BarChart data={barData} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                      <YAxis tick={{ fontSize: 11 }} tickFormatter={v => `₹${(v/1000).toFixed(0)}k`} />
                      <Tooltip formatter={v => `₹${v.toLocaleString('en-IN')}`} />
                      <Legend />
                      <Bar dataKey="Deposits"    fill="#10b981" radius={[3,3,0,0]} />
                      <Bar dataKey="Withdrawals" fill="#ef4444" radius={[3,3,0,0]} />
                      <Bar dataKey="Transfers"   fill="#3b82f6" radius={[3,3,0,0]} />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </div>

              {/* Pie chart */}
              <div className="card">
                <h2 className="font-bold text-gray-800 mb-4">Transaction Distribution</h2>
                {pieData.length === 0 ? (
                  <div className="text-center py-12 text-gray-400">No data available</div>
                ) : (
                  <ResponsiveContainer width="100%" height={280}>
                    <PieChart>
                      <Pie data={pieData} cx="50%" cy="50%" outerRadius={100}
                        dataKey="value" label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                        labelLine={false}>
                        {pieData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                      </Pie>
                      <Tooltip formatter={v => `₹${Number(v).toLocaleString('en-IN')}`} />
                    </PieChart>
                  </ResponsiveContainer>
                )}
              </div>
            </div>
          </>
        )}
      </div>
    </Layout>
  )
}