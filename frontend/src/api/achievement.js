import request from './request'

export function listAchievements(params) {
  return request.get('/api/achievements', { params })
}
