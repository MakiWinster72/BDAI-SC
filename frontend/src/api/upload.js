import request from './request'

export function uploadMedia(file, options = {}) {
  const form = new FormData()
  form.append('file', file)
  const context = options.context || options.type
  if (context) {
    form.append('context', context)
  }
  return request.post('/api/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
