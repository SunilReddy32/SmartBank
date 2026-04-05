import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { getAccountsByUser, createAccount, setPin, setDailyLimits, downloadStatement } from '../api/accounts'
import Layout from '../components/Layout'
import Modal from '../components/Modal'
import toast from 'react-hot-toast'

export default function AccountsPage() {
  const { user } = useAuth()
  const [accounts, setAccounts]         = useState([])
  const [loading, setLoading]           = useState(true)
  const [showCreate, setShowCreate]     = useState(false)
  const [showPin, setShowPin]           = useState(false)
  const [showLimits, setShowLimits]     = useState(false)
  const [showStatement, setShowStatement] = useState(false)
  const [selected, setSelected]         = useState(null)
  const [submitting, setSubmitting]     = useState(false)

  // Forms
  const [createForm, setCreateForm]     = useState({ initialBalance: 0, accountType: 'SAVINGS' })
  const [pinForm, setPinForm]           = useState({ pin: '', confirmPin: '' })
  const [limitForm, setLimitForm]       = useState({ dailyWithdrawalLimit: 50000, dailyTransferLimit: 100000 })
  const [stmtForm, setStmtForm]         = useState({ fromYear: 2025, fromMonth: 1, toYear: 2025, toMonth: 12 })

  const load = async () => {
    try {
      const res = await getAccountsByUser(user.userId)
      setAccounts(res.data)
    } catch { toast.error('Failed to load accounts') }
    finally { setLoading(false) }
  }

  useEffect(() => { if (user?.userId) load() }, [user])

  const handleCreate = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await createAccount(user.userId, createForm)
      toast.success('Account created!')
      setShowCreate(false)
      setCreateForm({ initialBalance: 0, accountType: 'SAVINGS' })
      load()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to create account') }
    finally { setSubmitting(false) }
  }

  const handleSetPin = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await setPin(selected.id, pinForm)
      toast.success('PIN set successfully!')
      setShowPin(false)
      setPinForm({ pin: '', confirmPin: '' })
      load()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to set PIN') }
    finally { setSubmitting(false) }
  }

  const handleSetLimits = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await setDailyLimits(selected.id, limitForm)
      toast.success('Daily limits updated!')
      setShowLimits(false)
      load()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to update limits') }
    finally { setSubmitting(false) }
  }

  const handleDownloadStatement = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      const res = await downloadStatement(selected.id, stmtForm)
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `SmartBank_Statement_${selected.accountNumber}.pdf`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      toast.success('Statement downloaded!')
      setShowStatement(false)
    } catch { toast.error('Failed to download statement') }
    finally { setSubmitting(false) }
  }

  const openPin = (acc) => { setSelected(acc); setShowPin(true) }
  const openLimits = (acc) => { setSelected(acc); setLimitForm({ dailyWithdrawalLimit: acc.dailyWithdrawalLimit, dailyTransferLimit: acc.dailyTransferLimit }); setShowLimits(true) }
  const openStatement = (acc) => { setSelected(acc); setShowStatement(true) }

  return (
    <Layout>
      <div className="max-w-4xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">My Accounts</h1>
            <p className="text-gray-500 text-sm">Manage your bank accounts</p>
          </div>
          <button onClick={() => setShowCreate(true)} className="btn-primary">+ New Account</button>
        </div>

        {loading ? (
          <div className="flex justify-center py-16">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
          </div>
        ) : accounts.length === 0 ? (
          <div className="card text-center py-16">
            <div className="text-5xl mb-4">🏦</div>
            <p className="text-gray-500 mb-4">You don't have any accounts yet</p>
            <button onClick={() => setShowCreate(true)} className="btn-primary">Create Your First Account</button>
          </div>
        ) : (
          <div className="space-y-4">
            {accounts.map(acc => (
              <div key={acc.id} className="card">
                <div className="flex flex-wrap justify-between items-start gap-4">
                  <div>
                    <div className="flex items-center gap-3 mb-2">
                      <span className="text-2xl">{acc.accountType === 'SAVINGS' ? '💰' : '🏢'}</span>
                      <div>
                        <div className="font-bold text-gray-800">{acc.accountType} Account</div>
                        <div className="font-mono text-sm text-gray-500">{acc.accountNumber}</div>
                      </div>
                    </div>
                    <div className="text-3xl font-bold text-blue-600">
                      ₹{acc.balance?.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </div>
                    <div className="flex gap-4 mt-2 text-xs text-gray-500">
                      <span>Withdrawal limit: ₹{acc.dailyWithdrawalLimit?.toLocaleString('en-IN') || 'Unlimited'}/day</span>
                      <span>Transfer limit: ₹{acc.dailyTransferLimit?.toLocaleString('en-IN') || 'Unlimited'}/day</span>
                    </div>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    <span className={`text-xs font-semibold px-2 py-1 rounded-full ${acc.pinSet ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                      {acc.pinSet ? '🔐 PIN Set' : '⚠️ PIN Not Set'}
                    </span>
                  </div>
                </div>

                <div className="flex flex-wrap gap-2 mt-4 pt-4 border-t border-gray-100">
                  <button onClick={() => openPin(acc)} className="btn-secondary text-sm py-1.5">
                    🔐 {acc.pinSet ? 'Change PIN' : 'Set PIN'}
                  </button>
                  <button onClick={() => openLimits(acc)} className="btn-secondary text-sm py-1.5">
                    📊 Set Limits
                  </button>
                  <button onClick={() => openStatement(acc)} className="btn-secondary text-sm py-1.5">
                    📄 Download Statement
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Create Account Modal */}
      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="Create New Account">
        <form onSubmit={handleCreate} className="space-y-4">
          <div>
            <label className="label">Account Type</label>
            <select className="input" value={createForm.accountType}
              onChange={e => setCreateForm({ ...createForm, accountType: e.target.value })}>
              <option value="SAVINGS">Savings (earns 4% annual interest)</option>
              <option value="CURRENT">Current (no interest)</option>
            </select>
          </div>
          <div>
            <label className="label">Initial Deposit (₹)</label>
            <input type="number" className="input" min="0" value={createForm.initialBalance}
              onChange={e => setCreateForm({ ...createForm, initialBalance: Number(e.target.value) })} />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => setShowCreate(false)} className="btn-secondary flex-1">Cancel</button>
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Creating...' : 'Create Account'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Set PIN Modal */}
      <Modal isOpen={showPin} onClose={() => setShowPin(false)} title={`Set Transaction PIN — ${selected?.accountNumber}`}>
        <form onSubmit={handleSetPin} className="space-y-4">
          <p className="text-sm text-gray-500">PIN is required for all withdrawals and transfers.</p>
          <div>
            <label className="label">New PIN (4 digits)</label>
            <input type="password" className="input" maxLength={4} placeholder="••••"
              value={pinForm.pin} onChange={e => setPinForm({ ...pinForm, pin: e.target.value })} required />
          </div>
          <div>
            <label className="label">Confirm PIN</label>
            <input type="password" className="input" maxLength={4} placeholder="••••"
              value={pinForm.confirmPin} onChange={e => setPinForm({ ...pinForm, confirmPin: e.target.value })} required />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => setShowPin(false)} className="btn-secondary flex-1">Cancel</button>
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Saving...' : 'Set PIN'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Daily Limits Modal */}
      <Modal isOpen={showLimits} onClose={() => setShowLimits(false)} title={`Daily Limits — ${selected?.accountNumber}`}>
        <form onSubmit={handleSetLimits} className="space-y-4">
          <p className="text-sm text-gray-500">Set to 0 for unlimited.</p>
          <div>
            <label className="label">Daily Withdrawal Limit (₹)</label>
            <input type="number" className="input" min="0" value={limitForm.dailyWithdrawalLimit}
              onChange={e => setLimitForm({ ...limitForm, dailyWithdrawalLimit: Number(e.target.value) })} />
          </div>
          <div>
            <label className="label">Daily Transfer Limit (₹)</label>
            <input type="number" className="input" min="0" value={limitForm.dailyTransferLimit}
              onChange={e => setLimitForm({ ...limitForm, dailyTransferLimit: Number(e.target.value) })} />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => setShowLimits(false)} className="btn-secondary flex-1">Cancel</button>
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Saving...' : 'Update Limits'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Statement Modal */}
      <Modal isOpen={showStatement} onClose={() => setShowStatement(false)} title={`Download Statement — ${selected?.accountNumber}`}>
        <form onSubmit={handleDownloadStatement} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label">From Year</label>
              <input type="number" className="input" value={stmtForm.fromYear}
                onChange={e => setStmtForm({ ...stmtForm, fromYear: Number(e.target.value) })} />
            </div>
            <div>
              <label className="label">From Month</label>
              <select className="input" value={stmtForm.fromMonth}
                onChange={e => setStmtForm({ ...stmtForm, fromMonth: Number(e.target.value) })}>
                {Array.from({ length: 12 }, (_, i) => (
                  <option key={i+1} value={i+1}>{new Date(0, i).toLocaleString('en', { month: 'long' })}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="label">To Year</label>
              <input type="number" className="input" value={stmtForm.toYear}
                onChange={e => setStmtForm({ ...stmtForm, toYear: Number(e.target.value) })} />
            </div>
            <div>
              <label className="label">To Month</label>
              <select className="input" value={stmtForm.toMonth}
                onChange={e => setStmtForm({ ...stmtForm, toMonth: Number(e.target.value) })}>
                {Array.from({ length: 12 }, (_, i) => (
                  <option key={i+1} value={i+1}>{new Date(0, i).toLocaleString('en', { month: 'long' })}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => setShowStatement(false)} className="btn-secondary flex-1">Cancel</button>
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Generating...' : '📄 Download PDF'}
            </button>
          </div>
        </form>
      </Modal>
    </Layout>
  )
}