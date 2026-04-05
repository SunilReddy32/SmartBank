const config = {
  DEPOSIT:          { label: 'Deposit',        color: 'bg-green-100 text-green-700',  symbol: '+' },
  WITHDRAW:         { label: 'Withdrawal',     color: 'bg-red-100 text-red-700',      symbol: '-' },
  TRANSFER:         { label: 'Transfer',       color: 'bg-blue-100 text-blue-700',    symbol: '↔' },
  INTEREST_CREDIT:  { label: 'Interest',       color: 'bg-purple-100 text-purple-700',symbol: '+' },
  LOAN_DISBURSEMENT:{ label: 'Loan Received',  color: 'bg-teal-100 text-teal-700',    symbol: '+' },
  EMI_PAYMENT:      { label: 'EMI Payment',    color: 'bg-orange-100 text-orange-700',symbol: '-' },
}

export default function TransactionBadge({ type }) {
  const c = config[type] || { label: type, color: 'bg-gray-100 text-gray-700', symbol: '•' }
  return (
    <span className={`inline-block text-xs font-semibold px-2 py-1 rounded-full ${c.color}`}>
      {c.symbol} {c.label}
    </span>
  )
}