import request from './request'

export function getStudentProfile() {
  return request.get('/api/student-profiles/me')
}

export function saveStudentProfile(payload) {
  return request.put('/api/student-profiles/me', payload)
}

export function saveStudentProfileById(id, payload) {
  return request.put(`/api/student-profiles/${id}`, payload)
}

export function searchStudentProfiles(params) {
  return request.get('/api/student-profiles/search', { params })
}

export function getStudentProfileById(id) {
  return request.get(`/api/student-profiles/${id}`)
}
