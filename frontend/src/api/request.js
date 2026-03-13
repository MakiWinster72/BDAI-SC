import axios from 'axios'

const API_BASE =
  import.meta.env.VITE_API_BASE || `http://${window.location.hostname}:8080`

const request = axios.create({
  baseURL: API_BASE,
  timeout: 12000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('gcsc_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem('gcsc_token')
      localStorage.removeItem('gcsc_user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export { API_BASE }
export default request
