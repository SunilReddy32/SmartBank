import { useCallback, useEffect, useState } from 'react'
import { getAllUsers, getAllAccounts, getPendingLoans, getAllLoans, approveLoan, rejectLoan, updateUserRole } from '../api/admin'
import Layout from '../components/Layout'
import Modal from '../components/Modal'
import toast from 'react-hot-toast'

const tabs = ['Users', 'Accounts', 'Pending Loans', 'All Loans']

const statusBadge = (s) => {
  const m = { PENDING:'badge-pending', ACTIVE:'badge-active', CLOSED:'badge-closed', REJECTED:'badge-rejected' }
  return <span className={m[s] || 'badge-pending'}>{s}</span>
}

export default function AdminPage() {
  const [tab, setTab]             = useState('Users')
  const [users, setUsers]         = useState([])
  const [accounts, setAccounts]   = useState([])
  const [pendingLoans, setPending] = useState([])
  const [allLoans, setAllLoans]   = useState([])
  const [loading, setLoading]     = useState(false)
  const [showReject, setShowReject] = useState(false)
  const [rejectLoanId, setRejectLoanId] = useState(null)
  const [rejectReason, setRejectReason] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const loadTab = useCallback(async() => {
    setLoading(true)
    try {
      if (tab === 'Users')         { const r = await getAllUsers();    setUsers(r.data) }
      if (tab === 'Accounts')      { const r = await getAllAccounts(); setAccounts(r.data) }
      if (tab === 'Pending Loans') { const r = await getPendingLoans(); setPending(r.data) }
      if (tab === 'All Loans')     { const r = await getAllLoans();    setAllLoans(r.data) }
    } catch { toast.error('Failed to load data') }
    finally { setLoading(false) }
    }, [tab])

  useEffect(() => { loadTab() }, [loadTab])

  const handleApprove = async (loanId) => {
    try {
      await approveLoan(loanId)
      toast.success('Loan approved and disbursed!')
      loadTab()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to approve') }
  }

  const handleReject = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await rejectLoan(rejectLoanId, rejectReason)
      toast.success('Loan rejected')
      setShowReject(false)
      setRejectReason('')
      loadTab()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to reject') }
    finally { setSubmitting(false) }
  }

  const handleRoleToggle = async (user) => {
    const newRole = user.role === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN'
    try {
      await updateUserRole(user.id, newRole)
      toast.success(`${user.name} is now ${newRole}`)
      loadTab()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to update role') }
  }

  return (
    <Layout>
      <div className="max-w-6xl mx-auto">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">🛡️ Admin Panel</h1>
          <p className="text-gray-500 text-sm">Manage users, accounts, and loans</p>
        </div>

        {/* Tabs */}
        <div className="flex gap-1 mb-6 bg-gray-100 p-1 rounded-lg w-fit">
          {tabs.map(t => (
            <button key={t} onClick={() => setTab(t)}
              className={`px-4 py-2 rounded-md text-sm font-medium transition-colors ${
                tab === t ? 'bg-white text-blue-700 shadow-sm' : 'text-gray-600 hover:text-gray-800'
              }`}>
              {t}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="flex justify-center py-16">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
          </div>
        ) : (
          <>
            {/* Users Tab */}
            {tab === 'Users' && (
              <div className="card overflow-x-auto">
                <table className="w-full text-sm">
                  <thead><tr className="border-b border-gray-200">
                    <th className="text-left p-3 font-semibold text-gray-600">ID</th>
                    <th className="text-left p-3 font-semibold text-gray-600">Name</th>
                    <th className="text-left p-3 font-semibold text-gray-600">Email</th>
                    <th className="text-left p-3 font-semibold text-gray-600">Role</th>
                    <th className="text-left p-3 font-semibold text-gray-600">Action</th>
                  </tr></thead>
                  <tbody className="divide-y divide-gray-100">
                    {users.map(u => (
                      <tr key={u.id} className="hover:bg-gray-50">
                        <td className="p-3 text-gray-500">#{u.id}</td>
                        <td className="p-3 font-medium">{u.name}</td>
                        <td className="p-3 text-gray-600">{u.email}</td>
                        <td className="p-3">
                          <span className={u.role === 'ROLE_ADMIN' ? 'badge-active' : 'badge-pending'}>
                            {u.role === 'ROLE_ADMIN' ? 'Admin' : 'User'}
                          </span>
                        </td>
                        <td className="p-3">
                          <button onClick={() => handleRoleToggle(u)}
                            className="text-xs btn-secondary py-1 px-2">
                            {u.role === 'ROLE_ADMIN' ? '⬇️ Make User' : '⬆️ Make Admin'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {!users.length && <div className="text-center py-8 text-gray-400">No users found</div>}
              </div>
            )}

            {/* Accounts Tab */}
            {tab === 'Accounts' && (
              <div className="card overflow-x-auto">
                <table className="w-full text-sm">
                  <thead><tr className="border-b border-gray-200">
                    <th className="text-left p-3 font-semibold text-gray-600">Account No.</th>
                    <th className="text-left p-3 font-semibold text-gray-600">Type</th>
                    <th className="text-right p-3 font-semibold text-gray-600">Balance</th>
                    <th className="text-left p-3 font-semibold text-gray-600">User ID</th>
                  </tr></thead>
                  <tbody className="divide-y divide-gray-100">
                    {accounts.map(a => (
                      <tr key={a.id} className="hover:bg-gray-50">
                        <td className="p-3 font-mono">{a.accountNumber}</td>
                        <td className="p-3">{a.accountType}</td>
                        <td className="p-3 text-right font-semibold text-blue-600">
                          ₹{a.balance?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                        </td>
                        <td className="p-3 text-gray-500">#{a.userId}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {!accounts.length && <div className="text-center py-8 text-gray-400">No accounts found</div>}
              </div>
            )}

            {/* Pending Loans Tab */}
            {tab === 'Pending Loans' && (
              <div className="space-y-4">
                {!pendingLoans.length ? (
                  <div className="card text-center py-12 text-gray-400">
                    <div className="text-4xl mb-3">✅</div>
                    <p>No pending loan applications</p>
                  </div>
                ) : pendingLoans.map(loan => (
                  <div key={loan.loanId} className="card">
                    <div className="flex flex-wrap justify-between items-start gap-3">
                      <div>
                        <div className="font-bold text-gray-800 mb-1">Loan #{loan.loanId} — {loan.userName}</div>
                        <div className="text-sm text-gray-500">
                          ₹{loan.loanAmount?.toLocaleString('en-IN')} · {loan.tenureMonths} months · {loan.annualInterestRate}% p.a.
                        </div>
                        <div className="text-xs text-gray-400 mt-1">
                          Monthly EMI: ₹{loan.emiAmount?.toFixed(2)} · Total: ₹{loan.totalPayable?.toFixed(2)}
                        </div>
                        <div className="text-xs text-gray-400">
                          Applied: {loan.appliedAt ? new Date(loan.appliedAt).toLocaleString('en-IN') : '—'}
                        </div>
                      </div>
                      <div className="flex gap-2">
                        <button onClick={() => handleApprove(loan.loanId)} className="btn-success text-sm py-1.5">
                          ✅ Approve
                        </button>
                        <button onClick={() => { setRejectLoanId(loan.loanId); setShowReject(true) }}
                          className="btn-danger text-sm py-1.5">
                          ❌ Reject
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* All Loans Tab */}
            {tab === 'All Loans' && (
              <div className="card overflow-x-auto">
                <table className="w-full text-sm">
                  <thead><tr className="border-b border-gray-200">
                    <th className="text-left p-3 font-semibold text-gray-600">ID</th>
                    <th className="text-left p-3 font-semibold text-gray-600">User</th>
                    <th className="text-right p-3 font-semibold text-gray-600">Amount</th>
                    <th className="text-left p-3 font-semibold text-gray-600">Tenure</th>
                    <th className="text-right p-3 font-semibold text-gray-600">EMI</th>
                    <th className="text-left p-3 font-semibold text-gray-600">Status</th>
                    <th className="text-left p-3 font-semibold text-gray-600">Progress</th>
                  </tr></thead>
                  <tbody className="divide-y divide-gray-100">
                    {allLoans.map(l => (
                      <tr key={l.loanId} className="hover:bg-gray-50">
                        <td className="p-3 text-gray-500">#{l.loanId}</td>
                        <td className="p-3 font-medium">{l.userName}</td>
                        <td className="p-3 text-right font-semibold">₹{l.loanAmount?.toLocaleString('en-IN')}</td>
                        <td className="p-3 text-gray-600">{l.tenureMonths}m</td>
                        <td className="p-3 text-right">₹{l.emiAmount?.toFixed(0)}</td>
                        <td className="p-3">{statusBadge(l.status)}</td>
                        <td className="p-3 text-gray-500">{l.emisPaid}/{l.tenureMonths}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {!allLoans.length && <div className="text-center py-8 text-gray-400">No loans found</div>}
              </div>
            )}
          </>
        )}
      </div>

      {/* Reject Modal */}
      <Modal isOpen={showReject} onClose={() => setShowReject(false)} title="Reject Loan Application">
        <form onSubmit={handleReject} className="space-y-4">
          <div>
            <label className="label">Rejection Reason</label>
            <textarea className="input" rows={3} placeholder="e.g. Insufficient credit history"
              value={rejectReason} onChange={e => setRejectReason(e.target.value)} required />
          </div>
          <div className="flex gap-3">
            <button type="button" onClick={() => setShowReject(false)} className="btn-secondary flex-1">Cancel</button>
            <button type="submit" className="btn-danger flex-1" disabled={submitting}>
              {submitting ? 'Rejecting...' : 'Confirm Reject'}
            </button>
          </div>
        </form>
      </Modal>
    </Layout>
  )
}