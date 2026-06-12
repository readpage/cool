/**
 * 报告持久化 — 管理加载/保存、表格配置（自动生成 / 持久化合并）
 */
import { ref, type Ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { ReportSaveRequest, FilterCondition, SortCondition } from '../../types/report'
import type { TableConfig, SearchConfig, ColumnConfig } from '../../types/table'
import type { TableItem } from '@/components/table/index.vue'
import { parseSqlColumns } from './useFilterSort'

/** 自动生成筛选器时，最多在筛选栏可见的列数，其余默认隐藏 */
const MAX_VISIBLE_FILTERS = 5

export function useReportPersistence(
  sqlTemplate: Ref<string>,
  filterConditions: Ref<FilterCondition[]>,
  sortConditions: Ref<SortCondition[]>,
  getEffectiveFilters: () => FilterCondition[],
  variableKeys: Ref<string[]>,
) {
  const currentTableKey = ref('')
  const currentReportId = ref<number | undefined>(undefined)
  const reportName = ref('')
  const datasourceId = ref<number | null>(null)
  const category = ref('')
  const persistedTableConfig = ref<TableConfig | null>(null)
  const tableConfig = ref<TableConfig>({ columns: [] })

  // ==================== 表格配置自动生成 ====================

  function autoGenTableConfig(cols: { prop: string; label: string }[]): TableConfig {
    const columns = cols.map(col => ({
      prop: col.prop,
      label: col.label,
      minWidth: 120,
      align: 'left' as const,
    }))

    // SELECT 列 filter 项
    const filter: ColumnConfig[] = cols.map((col, idx) => ({
      prop: col.prop,
      label: col.label,
      fieldType: 'text' as const,
      filterMode: (idx < MAX_VISIBLE_FILTERS ? 'show' : 'hide') as const,
    }))

    // 追加 #{key} 变量项（不与 SELECT 列重名时新建；重名时标记现有列）
    const colProps = new Set(cols.map(c => c.prop))
    for (const key of variableKeys.value) {
      if (!colProps.has(key)) {
        filter.push({
          prop: key,
          label: key,
          fieldType: 'text' as const,
          filterMode: 'exposed' as const,
          variable: true,
        })
      } else {
        ElMessage.warning(`变量 #{${key}} 与 SELECT 列重名，将标记现有列为模板变量`)
        const existing = filter.find(f => f.prop === key)
        if (existing) {
          existing.variable = true
          existing.filterMode = 'exposed'
        }
      }
    }

    const search: SearchConfig = { filter, currentField: 'all' }
    // 不设置 rowKey：SQL 查询结果无可靠主键，任意列都可能重复，el-table 默认用行索引即可
    return { columns, search, stripe: true }
  }

  /**
   * 合并持久化配置与当前列：以 SQL 列为源，保留持久化顺序/样式，同步增删改。
   * 新列按 SQL 中的相邻已持久化列自动定位。
   */
  function mergeTableConfig(
    persisted: TableConfig,
    resultCols: { prop: string; label: string }[],
  ): TableConfig {
    const labelMap = new Map<string, string>(resultCols.map(c => [c.prop, c.label]))
    const columns: TableItem[] = []
    const seen = new Set<string>()

    // 保留仍在 SQL 中的持久化列，保持用户自定义顺序
    for (const col of persisted.columns) {
      const label = labelMap.get(col.prop)
      if (label !== undefined) {
        seen.add(col.prop)
        columns.push({ ...col, label })
      }
    }

    // 单次遍历收集新增列（resultCols 已按 SQL 顺序排列）
    const newCols: TableItem[] = []
    for (const rc of resultCols) {
      if (!seen.has(rc.prop)) {
        newCols.push({ prop: rc.prop, label: rc.label, minWidth: 120, align: 'left' })
      }
    }

    // 按 SQL 相邻列自动定位：每列向其最近的已持久化"邻居"靠拢
    if (newCols.length) {
      const sqlIndex = new Map(resultCols.map((c, i) => [c.prop, i]))
      for (const nc of newCols) {
        const si = sqlIndex.get(nc.prop)!
        let insertAt = -1

        // 在 SQL 中向后找最近的已持久化邻居 → 插在其后
        for (let i = si - 1; i >= 0; i--) {
          const pos = columns.findIndex(c => c.prop === resultCols[i].prop)
          if (pos >= 0) { insertAt = pos + 1; break }
        }
        // 没找到前置邻居 → 在 SQL 中向前找 → 插在其前
        if (insertAt < 0) {
          for (let i = si + 1; i < resultCols.length; i++) {
            const pos = columns.findIndex(c => c.prop === resultCols[i].prop)
            if (pos >= 0) { insertAt = pos; break }
          }
        }

        if (insertAt >= 0) {
          columns.splice(insertAt, 0, nc)
        } else {
          columns.push(nc)
        }
      }
    }

    // 筛选器以 SQL 列为源重建
    const srcSearch = persisted.search
    const resultProps = new Set(resultCols.map(c => c.prop))

    // 辅助：追加 #{key} 变量项（不与 SELECT 列重名时新建；重名时标记现有列）
    function appendVariables(filters: ColumnConfig[]): ColumnConfig[] {
      for (const key of variableKeys.value) {
        if (!resultProps.has(key)) {
          filters.push({ prop: key, label: key, fieldType: 'text' as const, filterMode: 'exposed' as const, variable: true })
        } else {
          ElMessage.warning(`变量 #{${key}} 与 SELECT 列重名，将标记现有列为模板变量`)
          const existing = filters.find(f => f.prop === key)
          if (existing) {
            existing.variable = true
            existing.filterMode = 'exposed'
          }
        }
      }
      return filters
    }

    if (!srcSearch) {
      return {
        ...persisted,
        columns,
        search: {
          filter: appendVariables(resultCols.map((c, i) => ({
            prop: c.prop, label: c.label,
            fieldType: 'text' as const,
            filterMode: (i < MAX_VISIBLE_FILTERS ? 'show' : 'hide') as const,
          }))),
          currentField: 'all',
        },
      }
    }

    const filterMap = new Map(srcSearch.filter.map(f => [f.prop, f]))

    const newFilter = resultCols.map(rc => {
      const f = filterMap.get(rc.prop)
      return f ? { ...f }
        : { prop: rc.prop, label: rc.label, fieldType: 'text' as const, filterMode: 'hide' as const }
    })

    // 追加 #{key} 变量项：优先保留用户已改过的配置，否则新建；重名时标记现有列
    for (const key of variableKeys.value) {
      if (!resultProps.has(key)) {
        const existing = filterMap.get(key)
        newFilter.push(existing
          ? { ...existing, variable: true }
          : { prop: key, label: key, fieldType: 'text' as const, filterMode: 'exposed' as const, variable: true }
        )
      } else {
        ElMessage.warning(`变量 #{${key}} 与 SELECT 列重名，将标记现有列为模板变量`)
        const idx = newFilter.findIndex(f => f.prop === key && !f.variable)
        if (idx >= 0) {
          newFilter[idx] = { ...newFilter[idx], variable: true, filterMode: 'exposed' as const }
        }
      }
    }

    return { ...persisted, columns, search: { ...srcSearch, filter: newFilter } }
  }

  /** 同步计算 tableConfig（仅在手动调用时执行） */
  function syncTableConfig() {
    if (persistedTableConfig.value) {
      const parsedCols = parseSqlColumns(sqlTemplate.value)
      if (parsedCols.length) {
        const merged = mergeTableConfig(persistedTableConfig.value, parsedCols)
        tableConfig.value = merged
        return
      }
      tableConfig.value = persistedTableConfig.value
      return
    }
    const cols = parseSqlColumns(sqlTemplate.value)
    tableConfig.value = cols.length ? autoGenTableConfig(cols) : { columns: [] }
  }

  // ==================== 加载 / 保存 ====================

  /** 从 GET /report/{tableKey} 的结果恢复所有面板状态（displayConfig 即 TableConfig 扁平格式） */
  function loadReport(def: ReportSaveRequest) {
    currentTableKey.value = def.report.tableKey
    currentReportId.value = def.report.id
    reportName.value = def.report.name ?? ''
    datasourceId.value = def.report.datasourceId ?? null
    category.value = def.report.category ?? ''
    sqlTemplate.value = def.report.sqlTemplate

    const cfg = def.displayConfig
    if (cfg) {
      // 清理自动生成的 rowKey：SQL 列可能含 `.` 且无法保证唯一性
      if (cfg.rowKey && typeof cfg.rowKey === 'string' && cfg.rowKey.includes('.')) {
        delete cfg.rowKey
      }
      // 校验 sort.column 是否仍在当前 SQL 列中，不匹配则清除（避免 tableConfig 携带旧值）
      if (cfg.sort) {
        const parsedCols = parseSqlColumns(sqlTemplate.value)
        const validProps = new Set(parsedCols.map(c => c.prop))
        if (!validProps.has(cfg.sort.column)) {
          delete cfg.sort
        }
      }
      persistedTableConfig.value = JSON.parse(JSON.stringify(cfg)) as TableConfig
      syncTableConfig()
      // ★ filterConditions（SQL 模板 {{filter}}）与 Table 搜索栏的 config.search.filter[].value 彻底解绑
      // config.search.filter[].value 仅供 Table 组件初始化，不应污染 filterConditions
      // filterConditions 由 autoInitFromColumns / 用户手动操作管理
      // 恢复排序
      sortConditions.value = cfg.sort ? [cfg.sort] : []
    }
  }

  /** 将当前面板状态打包为保存请求体（扁平 TableConfig 格式） */
  function buildSaveRequest(name?: string, cat?: string): ReportSaveRequest {
    const cfg = JSON.parse(JSON.stringify(tableConfig.value)) as TableConfig

    // sort 写入 TableConfig.sort
    if (sortConditions.value.length > 0) {
      cfg.sort = { ...sortConditions.value[0] }
    } else {
      delete (cfg as any).sort
    }

    // filter 值写入 search.filter[].value、operator、variable（按列匹配）
    const effectiveFilters = getEffectiveFilters()
    if (cfg.search?.filter) {
      for (const col of cfg.search.filter) {
        const match = effectiveFilters.find(f => f.column === col.prop)
        if (match) {
          // 筛选值（清空时同步移除旧值）
          if (match.value !== '' && match.value !== null && match.value !== undefined) {
            col.value = match.value
          } else {
            delete (col as any).value
          }
          // 同步运算符（用户可能修改过默认算子）
          if (match.operator) {
            col.operator = match.operator as any
          }
          // 同步模板变量标记（#{key} 类型）
          if (match.variable) {
            col.variable = true
          }
        }
      }
    }

    return {
      report: {
        id: currentReportId.value,
        tableKey: currentTableKey.value,
        name: name || reportName.value || '',
        sqlTemplate: sqlTemplate.value,
        datasourceId: datasourceId.value,
        category: cat ?? category.value ?? undefined,
      },
      displayConfig: cfg,
    }
  }

  /** 手动刷新表格配置（列合并、新增列位置等）——在点击"运行"时调用 */
  function refreshTableConfig() {
    syncTableConfig()
  }

  // 初始同步一次（页面加载后）
  syncTableConfig()

  return { currentTableKey, currentReportId, reportName, datasourceId, category, tableConfig, loadReport, buildSaveRequest, autoGenTableConfig, refreshTableConfig, updateTableConfig: (v: TableConfig) => { persistedTableConfig.value = v; syncTableConfig() } }
}
