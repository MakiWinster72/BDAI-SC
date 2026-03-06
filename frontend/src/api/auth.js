import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 8000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('gcsc_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function register(data) {
  return request.post('/api/auth/register', data)
}

export function login(data) {
  return request.post('/api/auth/login', data)
}

export function getMe() {
  return request.get('/api/auth/me')
}
