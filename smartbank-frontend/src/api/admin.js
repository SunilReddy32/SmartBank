import api from './axios'

export const getAllUsers      = ()              => api.get('/admin/users')
export const getAllAccounts   = ()              => api.get('/admin/accounts')
export const getPendingLoans = ()              => api.get('/admin/loans/pending')
export const getAllLoans      = ()              => api.get('/admin/loans')
export const approveLoan     = (loanId)        => api.put(`/admin/loans/${loanId}/approve`)
export const rejectLoan      = (loanId, reason)=> api.put(`/admin/loans/${loanId}/reject`, null, { params: { reason } })
export const updateUserRole  = (userId, role)  => api.put(`/admin/users/${userId}/role`, null, { params: { role } })