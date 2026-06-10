/**
 * 查询运行时状态 — 管理执行状态、翻页、结果数据、表格搜索工具栏同步
 */
import { ref, reactive, computed, type Ref } from 'vue'
import type { FilterCondition, SortCondition, ReportQueryResult } from '../../types/report'
import type { PageResult } from '../../types/table'
import type { ReportExecuteBody } from '@/api/report'

export function useQueryRuntime(
  sqlTemplate: Ref<string>,
  _filterConditions: Ref<FilterCondition[]>,
  sortConditions: Ref<SortCondition[]>,
  getEffectiveFilters: () => FilterCondition[],
  datasourceId?: Ref<number | null>,
) {
  const execLoading = ref(false)
  const resultData = ref<ReportQueryResult | null>(null)
  const lastPage = reactive({ current: 1, size: 10 })
  const tableSearchFilters = ref<FilterCondition[]>([])

  /** 重置查询状态（翻页、表格搜索筛选），不重置 loading */
  function resetQueryState() {
    lastPage.current = 1
    lastPage.size = 10
    tableSearchFilters.value = []
  }

  const tableData = computed<PageResult>(() => ({
    list: resultData.value?.list ?? [],
    total: resultData.value?.total ?? 0,
  }))

  /** 自动组装查询参数 */
  const queryParam = computed<ReportExecuteBody>(() => ({
    sqlTemplate: sqlTemplate.value,
    filter: [...getEffectiveFilters(), ...tableSearchFilters.value],
    sort: sortConditions.value.length > 0 ? sortConditions.value[0] : undefined,
    current: lastPage.current,
    size: lastPage.size,
    datasourceId: datasourceId?.value ?? null,
  }))

  return { execLoading, resultData, lastPage, tableSearchFilters, tableData, queryParam, resetQueryState }
}
