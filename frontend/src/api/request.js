import axios from 'axios'

const backendPort = import.meta.env.VITE_BACKEND_PORT || '8080'
const apiBaseFromEnv = import.meta.env.VITE_API_BASE?.trim()
const API_BASE = apiBaseFromEnv || `http://${window.location.hostname}:${backendPort}`

const request = axios.create({
  baseURL: API_BASE,
  timeout: 12000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('bdai_sc_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem('bdai_sc_token')
      localStorage.removeItem('bdai_sc_user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export { API_BASE }
export default request
