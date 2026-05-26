import { ref, reactive, onMounted, onBeforeUnmount, type Ref } from 'vue'
import type { TableItem } from '../index.vue'

interface UseColumnResizeOptions {
  /** 容器 ref */
  wrapperRef: Ref<HTMLElement | undefined>
  /** 列配置数组（响应式 ref） */
  columns: Ref<TableItem[]>
  /** 边缘检测范围（px），默认 8 */
  edgeThreshold?: number
  /** 最小列宽（px），默认 80 */
  minWidth?: number
  /** 拖拽完成回调 */
  onResizeEnd?: () => void
}

/**
 * 列宽拖拽 resize 逻辑
 *
 * 交互：
 * 1. 光标悬停列右边缘 → 拖拽光标
 * 2. 点击边缘 → 显示实线分割线（固定位置）
 * 3. 拖拽移动 → 显示虚线跟随线（预览新位置）
 * 4. 松开 → 应用最终列宽，隐藏两条线
 *
 * 用法：
 * const { onTableMouseDown, dragLineVisible, dragLineStyle, previewLineVisible, previewLineStyle } = useColumnResize(...)
 */
export function useColumnResize(options: UseColumnResizeOptions) {
  const {
    wrapperRef,
    columns,
    edgeThreshold = 8,
    minWidth = 80,
    onResizeEnd,
  } = options

  // 实线：点击后固定在原始边界位置
  const dragLineVisible = ref(false)
  const dragLineStyle = reactive({ left: '0px', top: '0px', height: '0px' })

  // 虚线：拖拽过程中跟随鼠标，预览新位置
  const previewLineVisible = ref(false)
  const previewLineStyle = reactive({ left: '0px', top: '0px', height: '0px' })

  // 高亮遮罩：拖拽时左侧列覆盖浅蓝底色
  const columnHighlightVisible = ref(false)
  const columnHighlightStyle = reactive({ left: '0px', top: '0px', width: '0px', height: '0px' })

  // 像素宽度提示框
  const tooltipVisible = ref(false)
  const tooltipStyle = reactive({ left: '0px', top: '0px' })
  const tooltipText = ref('')

  /** 取列的最小宽度：优先列自身 minWidth，无则用全局默认 */
  const getMin = (col: TableItem) => (typeof col.minWidth === 'number' ? col.minWidth : 0) || minWidth

  let isDragging = false
  let dragColumnIndex = -1
  let dragStartWidth = 0
  let dragStartX = 0
  let dragLineLeft = 0

  // ========================
  // 事件监听
  // ========================

  const onDragMove = (e: MouseEvent) => {
    if (!isDragging || !wrapperRef.value) return
    const diff = e.clientX - dragStartX
    previewLineStyle.left = `${dragLineLeft + diff}px`

    const currentWidth = Math.max(dragStartWidth + diff, getMin(columns.value[dragColumnIndex]))
    tooltipText.value = `${Math.round(currentWidth)}px`
    const wrapperRect = wrapperRef.value.getBoundingClientRect()
    tooltipStyle.left = `${e.clientX - wrapperRect.left}px`
    tooltipStyle.top = `${e.clientY - wrapperRect.top - 28}px`
  }

  const onDragEnd = () => {
    if (isDragging) {
      const currentLeft = parseFloat(previewLineStyle.left)
      const diff = currentLeft - dragLineLeft
      const newWidth = Math.max(dragStartWidth + diff, getMin(columns.value[dragColumnIndex]))
      if (dragColumnIndex >= 0 && dragColumnIndex < columns.value.length) {
        const diffW = newWidth - dragStartWidth
        columns.value[dragColumnIndex].width = newWidth

        // 收缩时若表格已撑满，右侧第一列同步扩大，保证不留白
        if (diffW < 0 && wrapperRef.value) {
          const total = columns.value.reduce((s, c) => s + (typeof c.width === 'number' ? c.width : 0), 0)
          if (total <= wrapperRef.value.clientWidth) {
            for (let i = dragColumnIndex + 1; i < columns.value.length; i++) {
              const col = columns.value[i]
              if (col.resizable === false) continue
              const cur = typeof col.width === 'number' ? col.width : 0
              if (cur > 0) {
                columns.value[i].width = Math.max(cur - diffW, getMin(columns.value[i]))
                break
              }
            }
          }
        }

        onResizeEnd?.()
      }
    }

    dragLineVisible.value = false
    previewLineVisible.value = false
    columnHighlightVisible.value = false
    tooltipVisible.value = false
    isDragging = false
    wrapperRef.value?.classList.remove('resizing')
    dragColumnIndex = -1
    document.removeEventListener('mousemove', onDragMove)
    document.removeEventListener('mouseup', onDragEnd)
  }

  // ========================
  // 拖拽入口
  // ========================

  const onTableMouseDown = (e: MouseEvent) => {
    if ((e as any).__reorder) return
    const th = (e.target as HTMLElement).closest('th')
    if (!th) return

    const thRect = th.getBoundingClientRect()
    if (e.clientX < thRect.right - edgeThreshold) return

    const label = (th.querySelector('.cell') as HTMLElement)?.textContent?.trim() || th.textContent?.trim() || ''
    const colIndex = columns.value.findIndex(c => !c.hidden && c.label === label)
    if (colIndex < 0 || colIndex >= columns.value.length) return

    const col = columns.value[colIndex]
    if (col.resizable === false) return

    dragColumnIndex = colIndex
    dragStartWidth = thRect.width
    dragStartX = e.clientX
    isDragging = true
    wrapperRef.value?.classList.add('resizing')

    const wrapperRect = wrapperRef.value!.getBoundingClientRect()

    // 取表格实际渲染高度
    const tableEl = wrapperRef.value!.querySelector('.el-table') as HTMLElement
    const height = tableEl
      ? `${tableEl.getBoundingClientRect().height}px`
      : `${wrapperRef.value!.offsetHeight}px`

    dragLineLeft = thRect.right - wrapperRect.left
    const colLeft = thRect.left - wrapperRect.left

    // 实线：固定在原始边界
    dragLineVisible.value = true
    dragLineStyle.left = `${dragLineLeft}px`
    dragLineStyle.top = '0px'
    dragLineStyle.height = height

    // 虚线：初始位置与实线重叠
    previewLineVisible.value = true
    previewLineStyle.left = `${dragLineLeft}px`
    previewLineStyle.top = '0px'
    previewLineStyle.height = height

    // 列高亮遮罩
    columnHighlightVisible.value = true
    columnHighlightStyle.left = `${colLeft}px`
    columnHighlightStyle.top = '0px'
    columnHighlightStyle.width = `${thRect.width}px`
    columnHighlightStyle.height = height

    // 像素宽度提示框
    tooltipVisible.value = true
    tooltipText.value = `${Math.round(dragStartWidth)}px`
    tooltipStyle.left = `${e.clientX - wrapperRect.left}px`
    tooltipStyle.top = `${e.clientY - wrapperRect.top - 28}px`

    document.addEventListener('mousemove', onDragMove)
    document.addEventListener('mouseup', onDragEnd)
  }

  // ========================
  // 绑定 / 清理
  // ========================

  onMounted(() => {
    wrapperRef.value?.addEventListener('mousedown', onTableMouseDown)
  })

  onBeforeUnmount(() => {
    wrapperRef.value?.removeEventListener('mousedown', onTableMouseDown)
    document.removeEventListener('mousemove', onDragMove)
    document.removeEventListener('mouseup', onDragEnd)
  })

  return {
    dragLineVisible,
    dragLineStyle,
    previewLineVisible,
    previewLineStyle,
    columnHighlightVisible,
    columnHighlightStyle,
    tooltipVisible,
    tooltipStyle,
    tooltipText,
  }
}


