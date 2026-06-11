import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { AReport } from '@/api/report'
import { ADatasource } from '@/api/datasource'
import type { ReportQueryResult, FilterCondition, SortCondition } from '../../types/report'
import type { TableConfig, PageResult, TableQuery } from '../../types/table'
import { throttlePromise } from '@/utils/throttle'
import { useTableConfigStore } from '@/store/table-config'

const REPORT_GROUP = 'report'

export function useReportView() {
  const currentTableKey = ref('')
  const reportName = ref('')
  const currentDatasourceId = ref<number | null>(null)
  const datasourceLabel = ref('')
  const resultData = ref<ReportQueryResult | null>(null)

  const $store = useTableConfigStore()

  /** tableConfig 直接由 API 的 displayConfig 赋值，实时读取，不经过 localStorage 缓存 */
  const tableConfig = ref<TableConfig>({ columns: [] })

  const filterConditions = ref<FilterCondition[]>([])
  const sortConditions = ref<SortCondition[]>([])

  /** 持久保存 column→variable 映射（来自 displayConfig），不会被 onPageChange 覆盖 */
  const variableMap = ref<Record<string, boolean>>({})

  const lastPage = reactive({ current: 1, size: 10 })

  /** 对外暴露的 tableData，符合 Table 组件的 PageResult 接口 */
  const tableData = computed<PageResult>(() => ({
    list: resultData.value?.list ?? [],
    total: resultData.value?.total ?? 0,
  }))

  /** 执行查询（使用 Promise 安全节流，避免悬挂） */
  const doQuery = throttlePromise(async () => {
    if (!currentTableKey.value) return

    const body: any = {
      current: lastPage.current,
      size: lastPage.size,
    }

    const effectiveFilters = filterConditions.value.filter(
      f => f.column && f.operator,
    ).map(f => {
      // 兜底：如果 filterConditions 里没有 variable，从 variableMap 补上
      if (!f.variable && variableMap.value[f.column]) {
        return { ...f, variable: true }
      }
      return f
    })
    if (effectiveFilters.length > 0) body.filter = effectiveFilters

    if (sortConditions.value.length > 0 && sortConditions.value[0].column) {
      body.sort = sortConditions.value[0]
    }

    body.columns = tableConfig.value.columns

    try {
      const { data } = await AReport.userQuery(currentTableKey.value, body)
      resultData.value = data ?? null
    } catch (err) {
      console.error('查询失败', err)
    }
  }, 300)

  /** 加载报表定义并执行首次查询 */
  async function loadAndQuery(tableKey: string) {
    if (!tableKey) return

    try {
      const { data } = await AReport.getUserReport(tableKey)
      if (!data) {
        ElMessage.warning('报表不存在')
        currentTableKey.value = ''
        reportName.value = ''
        return
      }

      reportName.value = data.report.name
      currentDatasourceId.value = data.report.datasourceId ?? null

      // 解析数据源标签
      if (data.report.datasourceId) {
        try {
          const { data: dsList } = await ADatasource.list({})
          const ds = (dsList ?? []).find((d: any) => d.id === data.report.datasourceId)
          datasourceLabel.value = ds ? ds.name : `数据源 #${data.report.datasourceId}`
        } catch {
          datasourceLabel.value = ''
        }
      } else {
        datasourceLabel.value = ''
      }

      // ★ 先清空筛选和排序，防止旧报表数据污染新报表
      filterConditions.value = []
      sortConditions.value = []
      variableMap.value = {}

      // 从 API 返回的 displayConfig 直接设置 tableConfig（API 是唯一数据源，不写 localStorage）
      if (data.displayConfig) {
        const clone = JSON.parse(JSON.stringify(data.displayConfig)) as TableConfig
        // 清理自动生成的 rowKey：SQL 列可能含 `.` 且无法保证唯一性，由 el-table 默认行索引兜底
        if (clone.rowKey && typeof clone.rowKey === 'string' && clone.rowKey.includes('.')) {
          delete clone.rowKey
        }
        tableConfig.value = clone

        // 恢复筛选条件（用于发送 API 查询）+ 建立 variableMap
        filterConditions.value = []
        for (const col of clone.search?.filter ?? []) {
          if (col.variable) variableMap.value[col.prop] = true
          if (col.value !== undefined && col.value !== null && col.value !== '') {
            filterConditions.value.push({ column: col.prop, operator: col.operator || 'contains', value: col.value, variable: col.variable })
          }
        }
        sortConditions.value = clone.sort ? [clone.sort] : []
      }

      // 重置分页
      lastPage.current = 1
      lastPage.size = 10

      // ★ 查询由 Table 组件统一触发（首次渲染 → useTableInit.onMounted，后续切换 → watch）
    } catch (err) {
      console.error('加载报表失败', err)
      ElMessage.error('加载报表失败')
    }
  }

  /** 翻页 / 筛选 / 排序（Promise 安全节流，done 回调正常结束） */
  const onPageChange = throttlePromise(async (v: TableQuery, done: () => void) => {
    lastPage.current = v.current
    lastPage.size = v.size

    // 同步筛选条件 — 从 variableMap 恢复 SQL 模板变量标记
    const filters: FilterCondition[] = []
    for (const f of v.filter) {
      if (f.column) {
        const isVar = variableMap.value[f.column]
        filters.push({ column: f.column, operator: f.operator, value: f.value, ...(isVar ? { variable: true } : {}) })
      }
    }
    filterConditions.value = filters

    // 同步排序条件
    if (v.sort) {
      sortConditions.value = [
        { column: v.sort.column, direction: v.sort.direction as 'asc' | 'desc' },
      ]
    } else {
      sortConditions.value = []
    }

    try {
      await doQuery()
    } finally {
      done()
    }
  }, 300)

  /** 配置变更 → 持久化到用户配置（管理员可保存为系统默认） */
  function saveConfigToStore(config: TableConfig, isAdmin?: boolean) {
    if (!currentTableKey.value) return
    if (isAdmin) {
      $store.saveAsSystem(currentTableKey.value, config, REPORT_GROUP)
    } else {
      $store.save(currentTableKey.value, config, REPORT_GROUP)
    }
  }

  /** 恢复系统默认配置，然后重新加载 */
  async function resetConfigToSystem() {
    const key = currentTableKey.value
    if (!key) return
    await $store.resetToSystem(key, { columns: [] }, REPORT_GROUP)
    // 重新从 API 加载最新配置并查询
    await loadAndQuery(key)
  }

  return {
    currentTableKey,
    reportName,
    currentDatasourceId,
    datasourceLabel,
    resultData,
    tableConfig,
    tableData,
    filterConditions,
    sortConditions,
    variableMap,
    lastPage,
    loadAndQuery,
    onPageChange,
    saveConfigToStore,
    resetConfigToSystem,
  }
}
