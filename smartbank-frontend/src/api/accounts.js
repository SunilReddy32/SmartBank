import api from './axios'

export const getAccountsByUser  = (userId)            => api.get(`/accounts/user/${userId}`)
export const createAccount      = (userId, data)      => api.post(`/accounts/create/${userId}`, data)
export const setPin             = (accountId, data)   => api.put(`/accounts/${accountId}/pin`, data)
export const setDailyLimits     = (accountId, data)   => api.put(`/accounts/${accountId}/limits`, data)
export const downloadStatement  = (accountId, params) =>
  api.get(`/accounts/${accountId}/statement`, { params, responseType: 'blob' })