import request from './request'

export function getUserList() {
  return request.get('/api/admin/users')
}

export function updateUser(id, data) {
  return request.put(`/api/admin/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/api/admin/users/${id}`)
}
