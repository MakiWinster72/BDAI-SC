import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8080/api/auth',
  timeout: 8000
})

export function register(data) {
  return request.post('/register', data)
}

export function login(data) {
  return request.post('/login', data)
}
