import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { getAccountsByUser } from '../api/accounts'
import { transferByAccountNumber } from '../api/transactions'
import Layout from '../components/Layout'
import toast from 'react-hot-toast'

export default function TransferPage() {
  const { user } = useAuth()
  const [accounts, setAccounts] = useState([])
  const [form, setForm] = useState({
    fromAccountNumber: '',
    toAccountNumber: '',
    amount: '',
    pin: '',
  })
  const [loading, setLoading]     = useState(false)
  const [lastResult, setLastResult] = useState(null)

  useEffect(() => {
    if (user?.userId) {
      getAccountsByUser(user.userId).then(r => {
        setAccounts(r.data)
        if (r.data.length > 0) setForm(f => ({ ...f, fromAccountNumber: r.data[0].accountNumber }))
      })
    }
  }, [user])

  const selectedAcc = accounts.find(a => a.accountNumber === form.fromAccountNumber)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!selectedAcc?.pinSet) {
      toast.error('Please set a transaction PIN for this account first')
      return
    }
    setLoading(true)
    try {
      const res = await transferByAccountNumber({
        fromAccountNumber: form.fromAccountNumber,
        toAccountNumber: form.toAccountNumber,
        amount: Number(form.amount),
        pin: form.pin,
      })
      setLastResult(res.data)
      toast.success('Transfer successful!')
      setForm(f => ({ ...f, toAccountNumber: '', amount: '', pin: '' }))
    } catch (err) {
      toast.error(err.response?.data?.message || 'Transfer failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Layout>
      <div className="max-w-lg mx-auto">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Transfer Money</h1>
          <p className="text-gray-500 text-sm">Send money using account number</p>
        </div>

        <div className="card">
          <form onSubmit={handleSubmit} className="space-y-5">
            {/* From account */}
            <div>
              <label className="label">From Account</label>
              <select className="input" value={form.fromAccountNumber}
                onChange={e => setForm({ ...form, fromAccountNumber: e.target.value })}>
                {accounts.map(a => (
                  <option key={a.id} value={a.accountNumber}>
                    {a.accountNumber} — ₹{a.balance?.toLocaleString('en-IN')} ({a.accountType})
                  </option>
                ))}
              </select>
              {selectedAcc && (
                <div className="mt-1 flex gap-3 text-xs text-gray-400">
                  <span>Balance: ₹{selectedAcc.balance?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                  <span>Daily transfer limit: ₹{selectedAcc.dailyTransferLimit?.toLocaleString('en-IN')}</span>
                  {!selectedAcc.pinSet && <span className="text-red-400">⚠️ PIN not set</span>}
                </div>
              )}
            </div>

            {/* To account */}
            <div>
              <label className="label">To Account Number</label>
              <input className="input font-mono" placeholder="10-digit account number"
                value={form.toAccountNumber}
                onChange={e => setForm({ ...form, toAccountNumber: e.target.value })}
                maxLength={10} required />
            </div>

            {/* Amount */}
            <div>
              <label className="label">Amount (₹)</label>
              <input type="number" className="input" min="1" placeholder="Enter amount"
                value={form.amount}
                onChange={e => setForm({ ...form, amount: e.target.value })} required />
            </div>

            {/* PIN */}
            <div>
              <label className="label">Transaction PIN</label>
              <input type="password" className="input" maxLength={4} placeholder="••••"
                value={form.pin}
                onChange={e => setForm({ ...form, pin: e.target.value })} required />
              <p className="text-xs text-gray-400 mt-1">Your 4-digit account PIN</p>
            </div>

            <button type="submit" className="btn-primary w-full py-3" disabled={loading}>
              {loading ? 'Processing...' : '💸 Transfer Now'}
            </button>
          </form>
        </div>

        {/* Success result */}
        {lastResult && (
          <div className="card mt-4 border border-green-200 bg-green-50">
            <h3 className="font-semibold text-green-700 mb-2">✅ Transfer Successful</h3>
            <div className="text-sm space-y-1 text-gray-700">
              <div>Transaction ID: <span className="font-mono">#{lastResult.transactionId}</span></div>
              <div>Amount: <span className="font-bold">₹{lastResult.amount?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span></div>
              <div>Time: {lastResult.createdAt ? new Date(lastResult.createdAt).toLocaleString('en-IN') : '—'}</div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  )
}