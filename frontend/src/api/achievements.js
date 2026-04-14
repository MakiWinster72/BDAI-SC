import request from './request'

export function getAchievements(params = {}) {
  return request.get('/api/achievements', { params })
}

export function getAchievement(category, id) {
  return request.get(`/api/achievements/${category}/${id}`)
}

export function createAchievement(category, data) {
  return request.post(`/api/achievements/${category}`, data)
}

export function updateAchievement(category, id, data) {
  return request.put(`/api/achievements/${category}/${id}`, data)
}

export function deleteAchievement(category, id) {
  return request.delete(`/api/achievements/${category}/${id}`)
}
