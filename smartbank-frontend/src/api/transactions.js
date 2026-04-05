import api from './axios'

export const getTransactions         = (accountId, params) => api.get(`/transactions/account/${accountId}`, { params })
export const deposit                 = (accountId, data)   => api.post(`/transactions/deposit/${accountId}`, data)
export const withdraw                = (accountId, data)   => api.post(`/transactions/withdraw/${accountId}`, data)
export const transferByAccountNumber = (data)              => api.post('/transactions/transfer/by-account-number', data)