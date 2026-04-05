import api from './axios'
export const getDashboard = (userId) => api.get(`/dashboard/${userId}`)