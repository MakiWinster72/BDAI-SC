import axios from 'axios'

const apiBaseFromEnv = import.meta.env.VITE_API_BASE?.trim()
// 默认走当前站点同源 /api（dev 经 Vite 反代，生产经 Nginx），才能带上真实客户端 IP
const API_BASE = apiBaseFromEnv || ''

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
    if (error?.response?.status === 401 && !error.config?.skipAuthRedirect) {
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
