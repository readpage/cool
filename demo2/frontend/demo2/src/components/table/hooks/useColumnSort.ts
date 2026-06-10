import { ref } from 'vue'

/** 排序值 */
export interface SortState {
  column: string
  direction: 'asc' | 'desc'
}

/**
 * 单列排序 hook
 * sort(prop)       → 循环 asc→desc→none
 * sort(prop,'asc')  → 升序/已升序取消
 * sort(prop,'desc') → 降序/已降序取消
 */
export function useColumnSort(initialSort?: SortState) {
  const sortProp = ref<string | null>(initialSort?.column ?? null)
  const sortOrder = ref<'asc' | 'desc' | null>(initialSort?.direction ?? null)

  /** 从外部同步排序状态（如 localStorage 或搜索配置变更时） */
  const sync = (s?: SortState) => {
    sortProp.value = s?.column ?? null
    sortOrder.value = s?.direction ?? null
  }

  const sort = (prop: string, target?: 'asc' | 'desc') => {
    if (!target) {
      // 循环
      if (sortProp.value === prop) {
        if (sortOrder.value === 'asc') sortOrder.value = 'desc'
        else if (sortOrder.value === 'desc') { sortProp.value = null; sortOrder.value = null }
        else sortOrder.value = 'asc'
      } else {
        sortProp.value = prop; sortOrder.value = 'asc'
      }
    } else {
      // 直指方向，已同向则取消
      if (sortProp.value === prop && sortOrder.value === target) {
        sortProp.value = null; sortOrder.value = null
      } else {
        sortProp.value = prop; sortOrder.value = target
      }
    }
  }

  return { sortProp, sortOrder, sort, sync }
}
