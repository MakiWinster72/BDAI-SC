import request from './request'

export function getAchievements() {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.get('/api/achievements', { headers })
}

export function getAchievement(id) {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.get(`/api/achievements/${id}`, { headers })
}

export function createAchievement(data) {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.post('/api/achievements', data, { headers })
}

export function updateAchievement(id, data) {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.put(`/api/achievements/${id}`, data, { headers })
}

export function deleteAchievement(id) {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.delete(`/api/achievements/${id}`, { headers })
}
