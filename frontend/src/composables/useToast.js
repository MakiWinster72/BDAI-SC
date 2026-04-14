import { reactive, readonly } from 'vue'

const toasts = reactive([])
let nextId = 1
const DURATION = 4500

export function useToast() {
  function addToast({ type = 'info', message, duration = DURATION }) {
    const id = nextId++
    toasts.push({ id, type, message })
    if (duration > 0) {
      setTimeout(() => removeToast(id), duration)
    }
    return id
  }

  function removeToast(id) {
    const idx = toasts.findIndex((t) => t.id === id)
    if (idx !== -1) toasts.splice(idx, 1)
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
    success,
    error,
    info,
    warn,
    toast,
  }
}
