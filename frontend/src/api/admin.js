import request, { API_BASE } from './request'

export function getUserList({ page = 1, size = 20, search = '', role = '', className = '' } = {}) {
  const params = { page, size }
  if (search) params.search = search
  if (role) params.role = role
  if (className) params.className = className
  return request.get('/api/admin/users', { params })
}

export function updateUser(id, data) {
  return request.put(`/api/admin/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/api/admin/users/${id}`)
}

export function createUser(data) {
  return request.post('/api/admin/users', data)
}

// Download SQL backup via native fetch (binary response)
export function downloadBackupDb() {
  const token = localStorage.getItem('gcsc_token')
  return fetch(`${API_BASE}/api/admin/backup/db`, {
    headers: { Authorization: `Bearer ${token}` }
  })
}

// Restore database from SQL file upload
export function restoreBackupDb(file) {
  const token = localStorage.getItem('gcsc_token')
  const formData = new FormData()
  formData.append('file', file)
  return fetch(`${API_BASE}/api/admin/restore/db`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: formData
  })
}

// Download attachments as ZIP
export function downloadBackupAttachments() {
  const token = localStorage.getItem('gcsc_token')
  return fetch(`${API_BASE}/api/admin/backup/attachments`, {
    headers: { Authorization: `Bearer ${token}` }
  })
}

// Restore attachments from ZIP upload
export function restoreBackupAttachments(file) {
  const token = localStorage.getItem('gcsc_token')
  const formData = new FormData()
  formData.append('file', file)
  return fetch(`${API_BASE}/api/admin/restore/attachments`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: formData
  })
}
