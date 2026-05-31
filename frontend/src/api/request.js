import axios from 'axios'
import { useToast } from '@/composables/useToast'
import { getApiErrorMessage } from '@/utils/apiError'

const { error: toastError } = useToast()

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
    } else if (
      error?.response?.status === 403
      && error?.response?.data?.message === '请先修改初始密码'
      && window.location.pathname !== '/change-password'
    ) {
      const user = JSON.parse(localStorage.getItem('bdai_sc_user') || '{}')
      localStorage.setItem(
        'bdai_sc_user',
        JSON.stringify({ ...user, mustChangePassword: true }),
      )
      window.location.href = '/change-password'
    } else if (!error.config?.skipErrorToast) {
      const status = error?.response?.status
      if (status && status >= 400 && status < 500 && status !== 401) {
        toastError(getApiErrorMessage(error))
      }
    }
    return Promise.reject(error)
  }
)

export { API_BASE }
export default request
