import request from './request'

export function getContacts(params = {}) {
  return request.get('/api/contacts', { params })
}

export function createContact(data) {
  return request.post('/api/contacts', data)
}

export function updateContact(id, data) {
  return request.put(`/api/contacts/${id}`, data)
}

export function deleteContact(id) {
  return request.delete(`/api/contacts/${id}`)
}
