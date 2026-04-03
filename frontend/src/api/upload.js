import request from './request'

export function uploadMedia(file, options = {}) {
  const form = new FormData()
  form.append('file', file)
  if (options.context) {
    form.append('context', options.context)
  }
  return request.post('/api/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
