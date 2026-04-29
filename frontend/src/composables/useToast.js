import { reactive, readonly } from 'vue'

const toasts = reactive([])
let nextId = 1
const DURATION = 4500

export function useToast() {
  function addToast({ type = 'info', message, duration = DURATION, progress = null }) {
    const id = nextId++
    toasts.push({ id, type, message, progress })
    if (progress === null && duration > 0) {
      setTimeout(() => removeToast(id), duration)
    }
    return id
  }

  function removeToast(id) {
    const idx = toasts.findIndex((t) => t.id === id)
    if (idx !== -1) toasts.splice(idx, 1)
  }

  function updateToast(id, patch) {
    const item = toasts.find((t) => t.id === id)
    if (item) Object.assign(item, patch)
  }

  function success(message, duration) {
    return addToast({ type: 'success', message, duration })
  }

  function error(message, duration) {
    return addToast({ type: 'error', message, duration })
  }

  function info(message, duration) {
    return addToast({ type: 'info', message, duration })
  }

  function warn(message, duration) {
    return addToast({ type: 'warn', message, duration })
  }

  function progress(message) {
    return addToast({ type: 'progress', message, duration: 0, progress: 0 })
  }

  // Global toast function attached to window for cross-component usage
  function toast({ type = 'info', message, duration } = {}) {
    if (typeof type === 'string' && !message) {
      message = type
      type = 'info'
    }
    return addToast({ type, message, duration })
  }

  return {
    toasts: readonly(toasts),
    addToast,
    removeToast,
    updateToast,
    success,
    error,
    info,
    warn,
    progress,
    toast,
  }
}
