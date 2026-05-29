import { ref, computed } from 'vue'

export interface PaginationState {
  current: number
  size: number
  total: number
  pageSizes?: number[]
  layout?: string
  background?: boolean
  small?: boolean
}

export function useTablePagination(options?: {
  defaultPageSize?: number
  defaultCurrentPage?: number
  pageSizes?: number[]
  layout?: string
}) {
  const current = ref(options?.defaultCurrentPage ?? 1)
  const size = ref(options?.defaultPageSize ?? 10)
  const total = ref(0)

  const pagination = computed<PaginationState>(() => ({
    current: current.value,
    size: size.value,
    total: total.value,
    pageSizes: options?.pageSizes ?? [10, 20, 50, 100],
    layout: options?.layout ?? 'total, sizes, prev, pager, next, jumper',
    background: true,
    small: false,
  }))

  /** 页码变化 */
  const handlePageChange = (page: number) => {
    current.value = page
  }

  /** 每页条数变化 → 重置到第一页 */
  const handleSizeChange = (s: number) => {
    size.value = s
    current.value = 1
  }

  /** 筛选/排序变化 → 重置到第一页 */
  const resetPage = () => {
    current.value = 1
  }

  /**
   * 数据加载完成后同步 total，处理"删完最后一页"的边界
   * @returns true 表示需要重新请求（页码回退了）
   */
  const syncTotal = (newTotal: number): boolean => {
    total.value = newTotal
    const maxPage = Math.ceil(newTotal / size.value) || 1
    if (current.value > maxPage) {
      current.value = maxPage
      return true
    }
    return false
  }

  return {
    current,
    size,
    total,
    pagination,
    handlePageChange,
    handleSizeChange,
    resetPage,
    syncTotal,
  }
}
