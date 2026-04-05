import { useEffect, useState, useCallback } from 'react'
import { useAuth } from '../context/AuthContext'
import { getAccountsByUser } from '../api/accounts'
import { applyLoan, getMyLoans, payEmi } from '../api/loans'
import Layout from '../components/Layout'
import Modal from '../components/Modal'
import toast from 'react-hot-toast'

const statusBadge = (s) => {
  const m = { PENDING:'badge-pending', ACTIVE:'badge-active', CLOSED:'badge-closed', REJECTED:'badge-rejected' }
  return <span className={m[s] || 'badge-pending'}>{s}</span>
}

export default function LoansPage() {
  const { user } = useAuth()
  const [accounts, setAccounts]   = useState([])
  const [loans, setLoans]         = useState([])
  const [loading, setLoading]     = useState(true)
  const [showApply, setShowApply] = useState(false)
  const [showPay, setShowPay]     = useState(false)
  const [showDetail, setShowDetail] = useState(false)
  const [selectedLoan, setSelectedLoan] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [applyForm, setApplyForm] = useState({ loanAmount: '', tenureMonths: 12, disbursementAccountId: '' })
  const [pin, setPin]             = useState('')

const load = useCallback(async () => {
  try {
    const [accRes, loanRes] = await Promise.all([
      getAccountsByUser(user.userId),
      getMyLoans()
    ])
    setAccounts(accRes.data)
    setLoans(loanRes.data)

    // Set default disbursement account only if not already set
    if (accRes.data.length > 0) {
      setApplyForm(f =>
        f.disbursementAccountId ? f : { ...f, disbursementAccountId: accRes.data[0].id }
      )
    }

  } catch { toast.error('Failed to load data') }
  finally { setLoading(false) }
}, [user])

useEffect(() => {
  if (user?.userId) load()
}, [load, user])

  const handleApply = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      await applyLoan({ ...applyForm, loanAmount: Number(applyForm.loanAmount), tenureMonths: Number(applyForm.tenureMonths) })
      toast.success('Loan application submitted!')
      setShowApply(false)
      load()
    } catch (err) { toast.error(err.response?.data?.message || 'Application failed') }
    finally { setSubmitting(false) }
  }

  const handlePayEmi = async (e) => {
    e.preventDefault(); setSubmitting(true)
    try {
      const res = await payEmi(selectedLoan.loanId, { pin })
      toast.success(`EMI #${res.data.emiNumber} paid! Remaining: ${res.data.emisRemaining}`)
      setShowPay(false)
      setPin('')
      load()
    } catch (err) { toast.error(err.response?.data?.message || 'EMI payment failed') }
    finally { setSubmitting(false) }
  }

  // EMI preview calculation
  const emiPreview = () => {
    const P = Number(applyForm.loanAmount)
    const n = Number(applyForm.tenureMonths)
    const r = 10 / 100 / 12
    if (!P || !n) return null
    const emi = P * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1)
    return { emi: emi.toFixed(2), total: (emi * n).toFixed(2), interest: (emi * n - P).toFixed(2) }
  }
  const preview = emiPreview()

  return (
    <Layout>
      <div className="max-w-4xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">My Loans</h1>
            <p className="text-gray-500 text-sm">Apply and manage your loans</p>
          </div>
          <button onClick={() => setShowApply(true)} className="btn-primary">+ Apply for Loan</button>
        </div>

        {loading ? (
          <div className="flex justify-center py-16">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600" />
          </div>
        ) : loans.length === 0 ? (
          <div className="card text-center py-16">
            <div className="text-5xl mb-4">📄</div>
            <p className="text-gray-500 mb-4">You haven't applied for any loans yet</p>
            <button onClick={() => setShowApply(true)} className="btn-primary">Apply for a Loan</button>
          </div>
        ) : (
          <div className="space-y-4">
            {loans.map(loan => (
              <div key={loan.loanId} className="card">
                <div className="flex flex-wrap justify-between items-start gap-3 mb-3">
                  <div>
                    <div className="flex items-center gap-3 mb-1">
                      <span className="font-bold text-gray-800">Loan #{loan.loanId}</span>
                      {statusBadge(loan.status)}
                    </div>
                    <div className="text-sm text-gray-500">
                      Account: <span className="font-mono">{loan.disbursementAccountNumber}</span>
                    </div>
                  </div>
                  <div className="text-right">
                    <div className="text-2xl font-bold text-blue-600">
                      ₹{loan.loanAmount?.toLocaleString('en-IN')}
                    </div>
                    <div className="text-xs text-gray-400">{loan.annualInterestRate}% p.a. · {loan.tenureMonths} months</div>
                  </div>
                </div>

                {loan.status === 'ACTIVE' && (
                  <div className="grid grid-cols-3 gap-3 my-3 p-3 bg-blue-50 rounded-lg text-sm">
                    <div className="text-center">
                      <div className="font-bold text-blue-700">₹{loan.emiAmount?.toFixed(2)}</div>
                      <div className="text-xs text-gray-500">Monthly EMI</div>
                    </div>
                    <div className="text-center">
                      <div className="font-bold text-green-600">{loan.emisPaid}</div>
                      <div className="text-xs text-gray-500">EMIs Paid</div>
                    </div>
                    <div className="text-center">
                      <div className="font-bold text-orange-600">{loan.emisRemaining}</div>
                      <div className="text-xs text-gray-500">EMIs Left</div>
                    </div>
                  </div>
                )}

                {loan.status === 'REJECTED' && loan.rejectionReason && (
                  <div className="mt-2 p-2 bg-red-50 text-red-600 text-sm rounded">
                    Reason: {loan.rejectionReason}
                  </div>
                )}

                <div className="flex gap-2 mt-3">
                  <button onClick={() => { setSelectedLoan(loan); setShowDetail(true) }}
                    className="btn-secondary text-sm py-1.5">View Schedule</button>
                  {loan.status === 'ACTIVE' && (
                    <button onClick={() => { setSelectedLoan(loan); setShowPay(true) }}
                      className="btn-primary text-sm py-1.5">💳 Pay EMI</button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Apply Loan Modal */}
      <Modal isOpen={showApply} onClose={() => setShowApply(false)} title="Apply for a Loan">
        <form onSubmit={handleApply} className="space-y-4">
          <div>
            <label className="label">Loan Amount (₹)</label>
            <input type="number" className="input" min="1000" placeholder="e.g. 100000"
              value={applyForm.loanAmount}
              onChange={e => setApplyForm({ ...applyForm, loanAmount: e.target.value })} required />
          </div>
          <div>
            <label className="label">Tenure (months)</label>
            <select className="input" value={applyForm.tenureMonths}
              onChange={e => setApplyForm({ ...applyForm, tenureMonths: Number(e.target.value) })}>
              {[3,6,12,24,36,48,60].map(m => (
                <option key={m} value={m}>{m} months ({(m/12).toFixed(1)} years)</option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Disbursement Account</label>
            <select className="input" value={applyForm.disbursementAccountId}
              onChange={e => setApplyForm({ ...applyForm, disbursementAccountId: Number(e.target.value) })}>
              {accounts.map(a => (
                <option key={a.id} value={a.id}>{a.accountNumber} — ₹{a.balance?.toLocaleString('en-IN')}</option>
              ))}
            </select>
          </div>

          {/* EMI preview */}
          {preview && (
            <div className="bg-blue-50 p-3 rounded-lg text-sm space-y-1">
              <div className="font-semibold text-blue-700">EMI Preview (10% p.a.)</div>
              <div className="flex justify-between"><span>Monthly EMI</span><span className="font-bold">₹{preview.emi}</span></div>
              <div className="flex justify-between"><span>Total Payable</span><span>₹{preview.total}</span></div>
              <div className="flex justify-between"><span>Total Interest</span><span className="text-orange-600">₹{preview.interest}</span></div>
            </div>
          )}

          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => setShowApply(false)} className="btn-secondary flex-1">Cancel</button>
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Submitting...' : 'Apply Now'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Pay EMI Modal */}
      <Modal isOpen={showPay} onClose={() => setShowPay(false)} title={`Pay EMI — Loan #${selectedLoan?.loanId}`}>
        <form onSubmit={handlePayEmi} className="space-y-4">
          <div className="bg-blue-50 p-3 rounded-lg text-sm space-y-1">
            <div className="flex justify-between"><span>EMI Amount</span><span className="font-bold">₹{selectedLoan?.emiAmount?.toFixed(2)}</span></div>
            <div className="flex justify-between"><span>EMI Number</span><span>{(selectedLoan?.emisPaid || 0) + 1} of {selectedLoan?.tenureMonths}</span></div>
          </div>
          <div>
            <label className="label">Transaction PIN</label>
            <input type="password" className="input" maxLength={4} placeholder="••••"
              value={pin} onChange={e => setPin(e.target.value)} required />
          </div>
          <div className="flex gap-3">
            <button type="button" onClick={() => setShowPay(false)} className="btn-secondary flex-1">Cancel</button>
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Processing...' : 'Pay EMI'}
            </button>
          </div>
        </form>
      </Modal>

      {/* EMI Schedule Modal */}
      <Modal isOpen={showDetail} onClose={() => setShowDetail(false)} title={`EMI Schedule — Loan #${selectedLoan?.loanId}`}>
        <div className="max-h-96 overflow-y-auto">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 sticky top-0">
              <tr>
                <th className="text-left p-2 font-semibold text-gray-600">EMI #</th>
                <th className="text-left p-2 font-semibold text-gray-600">Due Date</th>
                <th className="text-right p-2 font-semibold text-gray-600">Amount</th>
                <th className="text-center p-2 font-semibold text-gray-600">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {selectedLoan?.emiSchedule?.map(emi => (
                <tr key={emi.emiNumber}>
                  <td className="p-2">{emi.emiNumber}</td>
                  <td className="p-2 text-gray-500">{emi.dueDate || '—'}</td>
                  <td className="p-2 text-right font-mono">₹{emi.emiAmount?.toFixed(2)}</td>
                  <td className="p-2 text-center">
                    <span className={emi.status === 'PAID' ? 'badge-active' : 'badge-pending'}>
                      {emi.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Modal>
    </Layout>
  )
}