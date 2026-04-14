import { ref } from 'vue'

/**
 * useOverlayState — 统一管理叠加层(overlay/modal/dialog)的打开/关闭状态
 *
 * 使用方式:
 *   const { open, closing, openOverlay, closeOverlay } = useOverlayState()
 *
 * 关闭时会自动设置 closing 动画状态，260ms 后重置。
 */
export function useOverlayState() {
  const open = ref(false)
  const closing = ref(false)

  function openOverlay() {
    open.value = true
    closing.value = false
  }

  function closeOverlay() {
    open.value = false
    closing.value = true
    setTimeout(() => {
      closing.value = false
    }, 260)
  }

  return { open, closing, openOverlay, closeOverlay }
}
