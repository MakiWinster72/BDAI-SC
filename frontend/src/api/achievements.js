import request from './request'

export function getAchievements() {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.get('/api/achievements', { headers })
}

export function getAchievement(category, id) {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.get(`/api/achievements/${category}/${id}`, { headers })
}

export function createAchievement(category, data) {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.post(`/api/achievements/${category}`, data, { headers })
}

export function updateAchievement(category, id, data) {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.put(`/api/achievements/${category}/${id}`, data, { headers })
}

export function deleteAchievement(category, id) {
  const token = localStorage.getItem('gcsc_token')
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  return request.delete(`/api/achievements/${category}/${id}`, { headers })
}
