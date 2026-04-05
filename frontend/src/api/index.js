import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const message =
      err.response?.data?.error ||
      err.response?.data?.message ||
      err.message ||
      'Unknown error'
    return Promise.reject(new Error(message))
  }
)

export const getAppointments = () => api.get('/appointments')
export const bookAppointment  = (data) => api.post('/appointments/book', data)
export const updateAppointmentStatus = (id, status) =>
  api.patch(`/appointments/${id}/status`, null, { params: { status } })

export const getCallLogs = () => api.get('/call-logs')

export const triggerOutboundCall = (data) => api.post('/calls/outbound', data)

export const getDoctors  = () => api.get('/doctors')
export const getPatients = () => api.get('/patients')
export const getAvailability = (doctorId, date) =>
  api.get('/availability', { params: { doctorId, ...(date && { date }) } })
