import { ref, reactive, onMounted, onBeforeUnmount, type Ref } from 'vue'
import type { TableItem } from '../index.vue'

interface UseColumnReorderOptions {
  /** 表格容器 ref */
  wrapperRef: Ref<HTMLElement | undefined>
  /** 列配置数组（响应式 ref） */
  columns: Ref<TableItem[]>
  /** 判断某列是否固定不可排序 */
  isFixed?: (col: TableItem) => boolean
  /** 排序完成回调 */
  onReorder?: () => void
  /** 右侧 resize 热区宽度，该区域内不触发排序拖拽 */
  resizeEdge?: number
}

/**
 * 列拖拽排序 hook（mouse 事件方案，与 resize 共存）
 *
 * 规则：
 *   1. mousedown 在 th 中间区域 → 排序拖拽
 *   2. mousedown 在 th 右边缘 → 列宽拖拽（由 useColumnResize 处理）
 *   3. 移动超过 5px 阈值才判定为"拖拽"
 */
/** 全局锁：固定列 mousedown 后上锁，鼠标释放后解锁 */
let lockedByFixed = false
const unlock = () => { lockedByFixed = false }

export { lockedByFixed as _lockedByFixed }

export function useColumnReorder(options: UseColumnReorderOptions) {
  const { wrapperRef, columns, isFixed, onReorder, resizeEdge = 12 } = options

  const dropLineVisible = ref(false)
  const dropLineStyle = reactive({ left: '0px', top: '0px', height: '0px' })

  const colHighlightVisible = ref(false)
  const colHighlightStyle = reactive({ left: '0px', top: '0px', width: '0px', height: '0px' })

  // 拖拽列标题提示
  const reorderTooltipVisible = ref(false)
  const reorderTooltipLabel = ref('')
  const reorderTooltipStyle = reactive({ left: '0px', top: '0px' })

  const DRAG_THRESHOLD = 5 // 移动超过 5px 才算拖拽

  let isDragging = false
  let dragSrcIndex = -1
  let dragStartX = 0
  let hasMoved = false
  let hasLeftSourceCol = false
  let lastTargetIdx = -1

  /** 通过 th 文本匹配查找列索引 */
  const findColByTh = (th: HTMLElement): number => {
    const label = th.querySelector('.cell')?.textContent?.trim() || th.textContent?.trim() || ''
    return columns.value.findIndex(c => !c.hidden && c.label === label)
  }

  /** 获取 target index（实际 columns 索引，非 DOM 索引） */
  const getTargetIndex = (e: MouseEvent): number => {
    const elem = document.elementFromPoint(e.clientX, e.clientY)
    const hoverTh = (elem as HTMLElement)?.closest('th')
    if (!hoverTh) return -1
    const colIdx = findColByTh(hoverTh as HTMLElement)
    if (colIdx < 0) return -1
    const rect = hoverTh.getBoundingClientRect()
    return e.clientX > rect.left + rect.width / 2 ? colIdx + 1 : colIdx
  }

  /** 通过列索引查找对应的 th 元素 */
  const findThByColIdx = (colIdx: number): HTMLElement | null => {
    const col = columns.value[colIdx]
    if (!col || col.hidden) return null
    const allThs = Array.from(wrapperRef.value?.querySelectorAll('th') ?? []) as HTMLElement[]
    for (const th of allThs) {
      const label = (th.querySelector('.cell') as HTMLElement)?.textContent?.trim() || th.textContent?.trim() || ''
      if (label === col.label) return th as HTMLElement
    }
    return null
  }

  const onMouseDown = (e: MouseEvent) => {
    // 排序按钮区域不触发拖拽
    if ((e.target as HTMLElement).closest('.u-sort-header')) return
    const thPreview = (e.target as HTMLElement).closest('th')
    if (!thPreview) return

    // 检测点击是否在固定列区域
    const elem = document.elementFromPoint(e.clientX, e.clientY)
    const inFixed = elem?.closest('.el-table__fixed, .el-table-fixed-column--left, .el-table-fixed-column--right')
    if (inFixed) {
      lockedByFixed = true
      return
    }

    const th = thPreview as HTMLElement
    if (lockedByFixed) return

    const thRect = th.getBoundingClientRect()
    // 右边缘留给 resize，不触发排序
    if (e.clientX > thRect.right - resizeEdge) return

    const colIdx = findColByTh(th)
    if (colIdx < 0 || colIdx >= columns.value.length) return

    const col = columns.value[colIdx]

    // 固定列：上锁，不启动拖拽
    if (isFixed?.(col)) {
      lockedByFixed = true
      ;(e as any).__reorder = true
      return
    }

    dragSrcIndex = colIdx
    dragStartX = e.clientX
    isDragging = true
    reorderTooltipVisible.value = true
    reorderTooltipLabel.value = col.label || ''
    reorderTooltipStyle.left = `${e.clientX + 12}px`
    reorderTooltipStyle.top = `${e.clientY - 10}px`
    hasMoved = false
    hasLeftSourceCol = false
    ;(e as any).__reorder = true

    // 列背景高亮：初始标记源列，move 时跟随目标
    const wrapperRect = wrapperRef.value!.getBoundingClientRect()
    const tableEl = wrapperRef.value!.querySelector('.el-table') as HTMLElement
    const height = tableEl ? `${tableEl.getBoundingClientRect().height}px` : '0px'
    colHighlightVisible.value = true
    colHighlightStyle.left = `${thRect.left - wrapperRect.left}px`
    colHighlightStyle.top = '0px'
    colHighlightStyle.width = `${thRect.width}px`
    colHighlightStyle.height = height

    wrapperRef.value?.classList.add('reordering')
    document.addEventListener('mousemove', onMouseMove)
    document.addEventListener('mouseup', onMouseUp)
  }

  /** 鼠标是否贴近任意列边缘 20px */
  const nearEdge = (e: MouseEvent) => {
    const headerRow = wrapperRef.value?.querySelector('.el-table__header-wrapper tr')
    if (!headerRow) return false
    for (const th of Array.from(headerRow.querySelectorAll('th'))) {
      const r = th.getBoundingClientRect()
      if (Math.abs(e.clientX - r.left) < 20 || Math.abs(e.clientX - r.right) < 20) return true
    }
    return false
  }

  const onMouseMove = (e: MouseEvent) => {
    if (!isDragging) return

    if (!hasMoved) {
      if (Math.abs(e.clientX - dragStartX) < DRAG_THRESHOLD) return
      hasMoved = true
    }
    reorderTooltipStyle.left = `${e.clientX + 12}px`
    reorderTooltipStyle.top = `${e.clientY - 10}px`

    lastTargetIdx = getTargetIndex(e)
    if (lastTargetIdx < 0) {
      dropLineVisible.value = false
      return
    }

    // 多个固定列之间不显示红线，固定列→非固定列边界可显示
    const firstDraggable = columns.value.findIndex(c => !c.hidden && !isFixed?.(c))
    if (firstDraggable >= 0 && lastTargetIdx < firstDraggable) {
      dropLineVisible.value = false
      return
    }

    // 首次未离开源列 → 不显线；离开后再回 → 边缘 20px 显线
    const targetActual = lastTargetIdx
    const atSource = targetActual === dragSrcIndex || targetActual === dragSrcIndex + 1
    if (atSource) {
      if (!hasLeftSourceCol) {
        dropLineVisible.value = false
        return
      }
      if (!nearEdge(e)) {
        dropLineVisible.value = false
        return
      }
    } else {
      hasLeftSourceCol = true
    }

    // 定位 drop 线 + 高亮（通过列索引查找 th）
    const targetIdx = lastTargetIdx
    let refTh: HTMLElement | null = null
    if (targetIdx < columns.value.length) {
      refTh = findThByColIdx(targetIdx)
    }
    // 超出末尾 → 定位到最后列的右边缘
    if (!refTh && columns.value.length > 0) {
      const lastNonHidden = [...columns.value].reverse().find(c => !c.hidden)
      if (lastNonHidden) {
        const lastIdx = columns.value.indexOf(lastNonHidden)
        refTh = findThByColIdx(lastIdx)
      }
    }
    if (refTh) {
      const wrapperRect = wrapperRef.value!.getBoundingClientRect()
      const refRect = refTh.getBoundingClientRect()
      const isAfterLast = targetIdx >= columns.value.length
      dropLineStyle.left = isAfterLast
        ? `${refRect.right - wrapperRect.left}px`
        : `${refRect.left - wrapperRect.left}px`
      dropLineStyle.top = '0px'
      colHighlightStyle.left = `${refRect.left - wrapperRect.left}px`
      colHighlightStyle.width = `${refRect.width}px`
      const tableEl = wrapperRef.value!.querySelector('.el-table') as HTMLElement
      const h = tableEl ? `${tableEl.getBoundingClientRect().height}px` : '0px'
      dropLineStyle.height = h
      colHighlightStyle.height = h
      dropLineVisible.value = true
    }
  }

  const onMouseUp = (e: MouseEvent) => {
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)

    wrapperRef.value?.classList.remove('reordering')
    dropLineVisible.value = false
    colHighlightVisible.value = false
    reorderTooltipVisible.value = false

    if (!hasMoved) {
      isDragging = false
      dragSrcIndex = -1
      return
    }

    // 移动列，目标至少要在非固定列区域
    const firstDraggable = columns.value.findIndex(c => !c.hidden && !isFixed?.(c))
    if (firstDraggable >= 0 && lastTargetIdx < firstDraggable) {
      isDragging = false; dragSrcIndex = -1; lastTargetIdx = -1; return
    }
    const targetActual = lastTargetIdx
    if (targetActual < 0 || targetActual === dragSrcIndex) {
      isDragging = false
      dragSrcIndex = -1
      lastTargetIdx = -1
      return
    }

    const adjustedTarget = targetActual > dragSrcIndex ? targetActual - 1 : targetActual
    const moved = columns.value.splice(dragSrcIndex, 1)[0]
    columns.value.splice(adjustedTarget, 0, moved)

    onReorder?.()
    isDragging = false
    dragSrcIndex = -1
  }

  const bind = () => {
    document.addEventListener('mousedown', onMouseDown, true)
    document.addEventListener('mouseup', unlock)
  }

  onMounted(() => {
    bind()
  })

  onBeforeUnmount(() => {
    document.removeEventListener('mousedown', onMouseDown, true)
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
    document.removeEventListener('mouseup', unlock)
  })

  return {
    dropLineVisible,
    dropLineStyle,
    colHighlightVisible,
    colHighlightStyle,
    reorderTooltipVisible,
    reorderTooltipLabel,
    reorderTooltipStyle,
  }
}



