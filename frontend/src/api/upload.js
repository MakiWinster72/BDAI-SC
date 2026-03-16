import request from './request'

export function uploadMedia(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/api/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
