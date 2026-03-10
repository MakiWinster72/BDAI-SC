import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 12000
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('gcsc_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default request
