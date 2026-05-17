import request from './request'

export function getAuditLogs({ page = 1, size = 20, search = '' } = {}) {
  const params = { page, size }
  if (search) params.search = search
  return request.get('/api/admin/audit-logs', { params })
}

export function createAuditLog({ action, detail }) {
  return request.post('/api/admin/audit-logs', { action, detail })
}
