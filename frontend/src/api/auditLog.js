import request from './request'

export function getAuditLogs({ page = 1, size = 20, search = '' } = {}) {
  const params = { page, size }
  if (search) params.search = search
  return request.get('/api/admin/audit-logs', { params })
}

/** 上报白名单内的客户端审计事件（导出等），教师/管理员/干部可用 */
export function recordAuditEvent({ action, detail }) {
  return request.post('/api/audit-logs/events', { action, detail })
}

/** @deprecated 使用 recordAuditEvent */
export function createAuditLog(payload) {
  return recordAuditEvent(payload)
}
