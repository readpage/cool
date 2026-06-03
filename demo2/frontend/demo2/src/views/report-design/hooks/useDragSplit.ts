/**
 * 拖拽分屏逻辑
 */
import { ref, onUnmounted } from 'vue'

const MIN_TOP = 100
const MIN_BOTTOM = 150
const DEFAULT_TOP = 380

export function useDragSplit() {
  const topHeight = ref(DEFAULT_TOP)
  let dragging = false
  let startY = 0
  let startHeight = 0

  function onDividerMouseDown(e: MouseEvent) {
    dragging = true
    startY = e.clientY
    startHeight = topHeight.value
    document.body.style.cursor = 'row-resize'
    document.body.style.userSelect = 'none'
    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('mouseup', onMouseUp)
  }

  function onMouseMove(e: MouseEvent) {
    if (!dragging) return
    const delta = e.clientY - startY
    const maxTop = window.innerHeight - MIN_BOTTOM
    const newHeight = Math.min(Math.max(startHeight + delta, MIN_TOP), maxTop)
    topHeight.value = newHeight
  }

  function onMouseUp() {
    dragging = false
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }

  onUnmounted(() => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  })

  return { topHeight, onDividerMouseDown }
}
