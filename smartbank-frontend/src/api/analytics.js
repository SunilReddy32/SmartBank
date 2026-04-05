import api from './axios'
export const getAnalytics = (userId, months = 6) => api.get(`/analytics/${userId}`, { params: { months } })