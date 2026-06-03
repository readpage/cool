/**
 * 报告工作区 - 纯状态管理（不含 API 调用）
 * 所有 API 请求由 index.vue 统一管理
 */
import { ref, reactive, computed, watch } from 'vue'
import type { ColumnMeta, ReportQueryResult, ReportSaveRequest } from '@/types/report'
import type { TableConfig, PageResult } from '@/types/table'
import { useFilterSort, parseColumnsFromSql, hasBuiltin } from './useFilterSort'
import type { ReportExecuteBody } from '@/api/report'

export function useReportWorkspace() {
  const {
    filterConditions,
    sortConditions,
    addFilter,
    removeFilter,
    addSort,
    removeSort,
    setSort,
    clearSort,
    getAvailableColumns,
    getEffectiveFilters,
    autoInitFromColumns,
  } = useFilterSort()

  // ==================== SQL 编辑 ====================
  const sqlTemplate = ref('')

  // ==================== 查询状态 ====================
  const execLoading = ref(false)
  const resultData = ref<ReportQueryResult | null>(null)
  const lastPage = reactive({ current: 1, size: 20 })

  // ==================== 当前报告 tableKey（用于保存/查询路由） ====================
  const currentTableKey = ref('')

  // ==================== 查询参数（自动组装，直接映射后端 ReportParam） ====================
  const queryParam = computed<ReportExecuteBody>(() => ({
    sqlTemplate: sqlTemplate.value,
    filter: getEffectiveFilters(),
    sort: sortConditions.value.length > 0 ? sortConditions.value[0] : undefined,
    current: lastPage.current,
    size: lastPage.size,
  }))

  // ==================== 持久化表格配置（来自 displayConfig.tableConfig） ====================
  const persistedTableConfig = ref<TableConfig | null>(null)

  // ==================== 可用列（来自查询结果） ====================
  const availableColumns = computed(() => {
    if (!resultData.value?.columns) return []
    return getAvailableColumns(resultData.value.columns)
  })

  // 查询结果返回后，自动生成筛选/排序默认行（仅当没有从 displayConfig 加载的初始条件时）
  watch(availableColumns, (cols) => {
    if (cols && cols.length > 0 && filterConditions.value.length === 0) {
      autoInitFromColumns(cols, sqlTemplate.value)
    }
  })

  // SQL 输入变化时，从 SELECT...FROM 解析列名自动生成筛选/排序
  watch(sqlTemplate, (newSql) => {
    if (!newSql) return
    const cols = parseColumnsFromSql(newSql)
    if (cols.length > 0 && filterConditions.value.length === 0) {
      autoInitFromColumns(cols, newSql)
    }
  })

  // ==================== 面板显隐 ====================
  const showFilter = computed(() => hasBuiltin(sqlTemplate.value, 'filter'))
  const showSort = computed(() => hasBuiltin(sqlTemplate.value, 'sort'))

  // ==================== 结果表格 ====================
  function inferSqlType(sqlType: number): string {
    switch (sqlType) {
      case 4: case 5: return 'int'
      case 3: case 2: case 6: case 8: return 'decimal'
      case 91: return 'date'
      case 92: return 'time'
      case 93: return 'timestamp'
      default: return 'text'
    }
  }

  const tableData = computed<PageResult>(() => ({
    list: resultData.value?.list ?? [],
    total: resultData.value?.total ?? 0,
  }))

  /** 从查询结果自动生成兜底表格配置 */
  function autoGenTableConfig(cols: ColumnMeta[]): TableConfig {
    const columns = cols.map((col: ColumnMeta) => ({
      prop: col.prop,
      label: col.label,
      minWidth: 120,
      align: (inferSqlType(col.sqlType) === 'int' || inferSqlType(col.sqlType) === 'decimal') ? 'right' as const : 'left' as const,
      sortable: true,
    }))
    return { columns, stripe: true, size: 'small', rowKey: columns[0]?.prop }
  }

  /** 合并持久化配置与查询结果列：以持久化列顺序为准，查询结果新增列追加到末尾 */
  function mergeTableConfig(persisted: TableConfig, resultCols: ColumnMeta[]): TableConfig {
    const resultProps = new Set(resultCols.map(c => c.prop))
    const mergedColumns = [...persisted.columns]

    // 持久化列中已在结果里的，保留配置；不在结果里的，标记 hidden
    const seen = new Set<string>()
    for (const col of mergedColumns) {
      seen.add(col.prop)
      if (!resultProps.has(col.prop)) {
        col.hidden = true
      }
    }

    // 查询结果中新增的列，追加到末尾（自动推断对齐方式）
    for (const rc of resultCols) {
      if (!seen.has(rc.prop)) {
        const inferredAlign = (inferSqlType(rc.sqlType) === 'int' || inferSqlType(rc.sqlType) === 'decimal') ? 'right' as const : 'left' as const
        mergedColumns.push({ prop: rc.prop, label: rc.label, minWidth: 120, align: inferredAlign, sortable: true })
      }
    }

    return { ...persisted, columns: mergedColumns }
  }

  const tableConfig = computed<TableConfig>(() => {
    const r = resultData.value
    // 优先用持久化配置
    if (persistedTableConfig.value && r?.columns?.length) {
      return mergeTableConfig(persistedTableConfig.value, r.columns)
    }
    // 兜底：从查询结果列信息自动生成
    if (!r || !r.columns.length) return { columns: [] }
    return autoGenTableConfig(r.columns)
  })

  // ==================== 加载报告 ====================

  /**
   * 从 GET /report/{tableKey} 的结果加载所有面板状态
   * 调用时机：index.vue onMounted 或路由参数变化时
   */
  async function loadReport(def: ReportSaveRequest) {
    currentTableKey.value = def.report.tableKey

    // 1) SQL 模板
    sqlTemplate.value = def.report.sqlTemplate

    // 2) 筛选条件（从 displayConfig 恢复）
    if (def.displayConfig?.filter?.length) {
      filterConditions.value = def.displayConfig.filter.map(f => ({
        column: f.column,
        operator: f.operator,
        value: f.value ?? '',
      }))
    } else {
      filterConditions.value = []
    }

    // 3) 排序条件（从 displayConfig 恢复）
    if (def.displayConfig?.sort) {
      sortConditions.value = [def.displayConfig.sort]
    }

    // 4) 持久化表格配置
    if (def.displayConfig?.tableConfig) {
      persistedTableConfig.value = def.displayConfig.tableConfig
    }
  }

  return {
    // 查询参数（直接映射后端接口，子组件 v-model 自动同步）
    queryParam,
    // SQL
    sqlTemplate,
    // 当前报告 tableKey
    currentTableKey,
    // 查询状态
    execLoading,
    resultData,
    lastPage,
    // 可用列
    availableColumns,
    // 面板显隐
    showFilter,
    showSort,
    // 表格
    tableData,
    tableConfig,
    // 筛选/排序
    filterConditions,
    sortConditions,
    getEffectiveFilters,
    addFilter,
    removeFilter,
    addSort,
    removeSort,
    // 加载报告
    loadReport,
  }
}
