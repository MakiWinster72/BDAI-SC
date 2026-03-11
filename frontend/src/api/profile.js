import request from './request'

export function getStudentProfile() {
  return request.get('/api/student-profiles/me')
}

export function saveStudentProfile(payload) {
  return request.put('/api/student-profiles/me', payload)
}
