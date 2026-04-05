import api from './axios'

export const applyLoan  = (data)   => api.post('/loans/apply', data)
export const getMyLoans = ()       => api.get('/loans/my')
export const payEmi     = (loanId, data) => api.post(`/loans/${loanId}/pay-emi`, data)