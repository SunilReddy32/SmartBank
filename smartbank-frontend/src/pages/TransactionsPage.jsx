import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { getAccountsByUser } from '../api/accounts'
import { getTransactions, deposit, withdraw } from '../api/transactions'
import Layout from '../components/Layout'
import Modal from '../components/Modal'
import TransactionBadge from '../components/TransactionBadge'
import toast from 'react-hot-toast'

export default function TransactionsPage() {
  const { user } = useAuth()
  const [accounts, setAccounts]       = useState([])
  const [selectedAccId, setSelectedAccId] = useState('')
  const [transactions, setTxns]       = useState([])
  const [filter, setFilter]           = useState('')
  const [page, setPage]               = useState(0)
  const [loading, setLoading]         = useState(false)
  const [showDeposit, setShowDeposit] = useState(false)
  const [showWithdraw, setShowWithdraw] = useState(false)
  const [submitting, setSubmitting]   = useState(false)
  const [depositForm, setDepositForm] = useState({ amount: '' })
  const [withdrawForm, setWithdrawForm] = useState({ amount: '', pin: '' })

  useEffect(() => {
    if (user?.userId) {
      getAccountsByUser(user.userId).then(r => {
        setAccounts(r.data)
        if (r.data.length > 0) setSelectedAccId(r.data[0].id)
      })
    }
  }, [user])

  useEffect(() => {
    if (selectedAccId) loadTxns()
  }, [selectedAccId, filter, page])

  const loadTxns = async () => {
    setLoading(true)
    try {
      const params = { page }
      if (filter) params.type = filter
      const res = await getTransactions(selectedAccId, params)
      setTxns(res.data)
    } catch { toast.error('Failed to load transactions') }
    finally { setLoading(false) }
  }

  const handleDeposit = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await deposit(selectedAccId, { amount: Number(depositForm.amount) })
      toast.success('Deposit successful!')
      setShowDeposit(false)
      setDepositForm({ amount: '' })
      loadTxns()
    } catch (err) { toast.error(err.response?.data?.message || 'Deposit failed') }
    finally { setSubmitting(false) }
  }

  const handleWithdraw = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await withdraw(selectedAccId, { amount: Number(withdrawForm.amount), pin: withdrawForm.pin })
      toast.success('Withdrawal successful!')
      setShowWithdraw(false)
      setWithdrawForm({ amount: '', pin: '' })
      loadTxns()
    } catch (err) { toast.error(err.response?.data?.message || 'Withdrawal failed') }
    finally { setSubmitting(false) }
  }

  const selectedAcc = accounts.find(a => a.id === selectedAccId)

  return (
    <Layout>
      <div className="max-w-4xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Transactions</h1>
            <p className="text-gray-500 text-sm">View and manage your transactions</p>
          </div>
          <div className="flex gap-2">
            <button onClick={() => setShowDeposit(true)} className="btn-success text-sm" disabled={!selectedAccId}>
              ➕ Deposit
            </button>
            <button onClick={() => setShowWithdraw(true)} className="btn-danger text-sm" disabled={!selectedAccId}>
              ➖ Withdraw
            </button>
          </div>
        </div>

        {/* Account selector + balance */}
        {selectedAcc && (
          <div className="card mb-4 flex flex-wrap justify-between items-center gap-4">
            <div>
              <label className="label text-xs">Select Account</label>
              <select className="input w-auto" value={selectedAccId}
                onChange={e => { setSelectedAccId(Number(e.target.value)); setPage(0) }}>
                {accounts.map(a => (
                  <option key={a.id} value={a.id}>{a.accountNumber} ({a.accountType})</option>
                ))}
              </select>
            </div>
            <div className="text-right">
              <div className="text-sm text-gray-500">Current Balance</div>
              <div className="text-2xl font-bold text-blue-600">
                ₹{selectedAcc.balance?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
              </div>
            </div>
          </div>
        )}

        {/* Filters */}
        <div className="flex flex-wrap gap-2 mb-4">
          {['', 'DEPOSIT', 'WITHDRAW', 'TRANSFER', 'INTEREST_CREDIT', 'EMI_PAYMENT'].map(f => (
            <button key={f}
              onClick={() => { setFilter(f); setPage(0) }}
              className={`text-sm px-3 py-1.5 rounded-full border transition-colors ${
                filter === f ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50'
              }`}>
              {f || 'All'}
            </button>
          ))}
        </div>

        {/* Transactions list */}
        <div className="card">
          {loading ? (
            <div className="flex justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
            </div>
          ) : transactions.length === 0 ? (
            <div className="text-center py-12 text-gray-400">
              <div className="text-4xl mb-3">📋</div>
              <p>No transactions found</p>
            </div>
          ) : (
            <div className="divide-y divide-gray-100">
              {transactions.map(tx => (
                <div key={tx.transactionId} className="flex justify-between items-center py-3">
                  <div>
                    <TransactionBadge type={tx.type} />
                    <div className="text-xs text-gray-400 mt-1">
                      {tx.createdAt ? new Date(tx.createdAt).toLocaleString('en-IN') : '—'}
                    </div>
                  </div>
                  <div className={`font-bold text-base ${
                    ['DEPOSIT','INTEREST_CREDIT','LOAN_DISBURSEMENT'].includes(tx.type)
                      ? 'text-green-600' : 'text-red-600'
                  }`}>
                    {['DEPOSIT','INTEREST_CREDIT','LOAN_DISBURSEMENT'].includes(tx.type) ? '+' : '-'}
                    ₹{tx.amount?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Pagination */}
          <div className="flex justify-between items-center mt-4 pt-4 border-t border-gray-100">
            <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
              className="btn-secondary text-sm py-1.5 px-3 disabled:opacity-40">← Prev</button>
            <span className="text-sm text-gray-500">Page {page + 1}</span>
            <button onClick={() => setPage(p => p + 1)} disabled={transactions.length < 10}
              className="btn-secondary text-sm py-1.5 px-3 disabled:opacity-40">Next →</button>
          </div>
        </div>
      </div>

      {/* Deposit Modal */}
      <Modal isOpen={showDeposit} onClose={() => setShowDeposit(false)} title={`Deposit — ${selectedAcc?.accountNumber}`}>
        <form onSubmit={handleDeposit} className="space-y-4">
          <div>
            <label className="label">Amount (₹)</label>
            <input type="number" className="input" min="1" placeholder="Enter amount"
              value={depositForm.amount} onChange={e => setDepositForm({ amount: e.target.value })} required />
          </div>
          <p className="text-xs text-gray-400">No PIN required for deposits.</p>
          <div className="flex gap-3">
            <button type="button" onClick={() => setShowDeposit(false)} className="btn-secondary flex-1">Cancel</button>
            <button type="submit" className="btn-success flex-1" disabled={submitting}>
              {submitting ? 'Processing...' : 'Deposit'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Withdraw Modal */}
      <Modal isOpen={showWithdraw} onClose={() => setShowWithdraw(false)} title={`Withdraw — ${selectedAcc?.accountNumber}`}>
        <form onSubmit={handleWithdraw} className="space-y-4">
          <div>
            <label className="label">Amount (₹)</label>
            <input type="number" className="input" min="1" placeholder="Enter amount"
              value={withdrawForm.amount} onChange={e => setWithdrawForm({ ...withdrawForm, amount: e.target.value })} required />
          </div>
          <div>
            <label className="label">Transaction PIN</label>
            <input type="password" className="input" maxLength={4} placeholder="••••"
              value={withdrawForm.pin} onChange={e => setWithdrawForm({ ...withdrawForm, pin: e.target.value })} required />
          </div>
          <div className="flex gap-3">
            <button type="button" onClick={() => setShowWithdraw(false)} className="btn-secondary flex-1">Cancel</button>
            <button type="submit" className="btn-danger flex-1" disabled={submitting}>
              {submitting ? 'Processing...' : 'Withdraw'}
            </button>
          </div>
        </form>
      </Modal>
    </Layout>
  )
}