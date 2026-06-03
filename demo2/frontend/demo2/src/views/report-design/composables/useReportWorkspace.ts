/**
 * 报告工作区核心状态与业务逻辑
 */
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { ParamDef, ColumnMeta, ReportQueryResult } from '@/types/report'
import type { TableConfig, TableQuery, PageResult } from '@/types/table'
import { AReport } from '@/api/report'
import { useFilterSort, parseColumnsFromSql } from './useFilterSort'

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
    hasBuiltin,
    autoInitFromColumns,
  } = useFilterSort()

  // ==================== SQL 编辑 ====================
  const sqlTemplate = ref('')
  const parameters = ref<ParamDef[]>([])
  const paramValues = reactive<Record<string, any>>({})

  function detectParams() {
    const sql = sqlTemplate.value
    if (!sql) return

    console.log('[ReportDebug] detectParams() 开始检测, SQL:', sql)

    // 1. 检测 #{name} 普通参数
    const hashRegex = /#\{(\w+)\}/g
    const existing = new Set(parameters.value.map(p => p.name))
    let match: RegExpExecArray | null
    while ((match = hashRegex.exec(sql)) !== null) {
      const name = match[1]
      if (!existing.has(name)) {
        parameters.value.push({ name, label: name, type: 'text', required: false })
        existing.add(name)
        console.log('[ReportDebug] 检测到普通参数:', name)
      }
    }

    // 2. 检测 {{filter}} {{sort}} {{column}} 内置参数
    const braceRegex = /\{\{(filter|sort|column)\}\}/gi
    while ((match = braceRegex.exec(sql)) !== null) {
      const name = match[1].toLowerCase() as 'filter' | 'sort' | 'column'
      const builtinKey = `__builtin_${name}`
      if (!existing.has(builtinKey)) {
        parameters.value.push({
          name: builtinKey,
          label: name === 'filter' ? '筛选条件' : name === 'sort' ? '排序' : '动态列',
          type: 'text',
          required: false,
          builtin: name,
        })
        existing.add(builtinKey)
        console.log('[ReportDebug] 检测到内置参数:', builtinKey, '-> builtin:', name)
      }
    }

    console.log('[ReportDebug] detectParams() 结束, parameters:', JSON.parse(JSON.stringify(parameters.value)))
    if (parameters.value.length === 0) {
      ElMessage.info('未检测到任何参数占位符')
    }
  }

  function resetParams() {
    for (const p of parameters.value) delete paramValues[p.name]
    filterConditions.value = []
    sortConditions.value = []
  }

  function updateParams(newParams: ParamDef[]) {
    // 清理已删除参数的 paramValues
    const keep = new Set(newParams.map(p => p.name))
    for (const key of Object.keys(paramValues)) {
      if (!keep.has(key)) delete paramValues[key]
    }
    parameters.value = newParams
  }

  // ==================== 执行查询 ====================
  const execLoading = ref(false)
  const resultData = ref<ReportQueryResult | null>(null)
  const lastPage = reactive({ current: 1, size: 20 })

  // ==================== 可用列（来自查询结果） ====================
  const availableColumns = computed(() => {
    if (!resultData.value?.columns) return []
    const cols = getAvailableColumns(resultData.value.columns)
    console.log('[ReportDebug] availableColumns computed 触发, 返回:', cols.length, '列, 详情:', JSON.parse(JSON.stringify(cols)))
    return cols
  })

  // 查询结果返回后，自动生成筛选/排序默认行
  watch(availableColumns, (cols) => {
    console.log('[ReportDebug] watch availableColumns 触发, cols长度:', cols?.length, 'cols:', JSON.parse(JSON.stringify(cols)))
    console.log('[ReportDebug] watch 当前 filterConditions.length:', filterConditions.value.length)
    if (cols && cols.length > 0) {
      console.log('[ReportDebug] watch 调用 autoInitFromColumns, sqlTemplate:', sqlTemplate.value?.substring(0, 80))
      autoInitFromColumns(cols, sqlTemplate.value)
      console.log('[ReportDebug] watch autoInit后 filterConditions.length:', filterConditions.value.length)
    }
  })

  // SQL 输入变化时，从 SELECT...FROM 解析列名自动生成（无需等待执行）
  watch(sqlTemplate, (newSql) => {
    if (!newSql) return
    console.log('[ReportDebug] watch sqlTemplate 变化, 尝试解析列...')
    const cols = parseColumnsFromSql(newSql)
    if (cols.length > 0) {
      console.log('[ReportDebug] watch sqlTemplate 解析到', cols.length, '列，调用 autoInitFromColumns')
      autoInitFromColumns(cols, newSql)
    }
  })

  function getCleanParams(): Record<string, any> {
    const cleaned: Record<string, any> = {}
    for (const key of Object.keys(paramValues)) {
      const v = paramValues[key]
      if (v !== undefined && v !== null && v !== '' && !(Array.isArray(v) && v.length === 0)) {
        cleaned[key] = v
      }
    }
    return cleaned
  }

  /** 合并过滤/排序条件到请求参数中 */
  function buildRequestParams(): Record<string, any> {
    const params: Record<string, any> = getCleanParams()
    const sql = sqlTemplate.value

    const hasF = hasBuiltin(sql, 'filter')
    const hasS = hasBuiltin(sql, 'sort')
    console.log('[ReportDebug] buildRequestParams() hasBuiltin filter:', hasF, 'sort:', hasS)
    console.log('[ReportDebug] buildRequestParams() filterConditions:', JSON.parse(JSON.stringify(filterConditions.value)))
    console.log('[ReportDebug] buildRequestParams() sortConditions:', JSON.parse(JSON.stringify(sortConditions.value)))
    console.log('[ReportDebug] buildRequestParams() cleanParams:', JSON.parse(JSON.stringify(params)))

    if (hasF && filterConditions.value.length > 0) {
      params['__filter'] = filterConditions.value
      console.log('[ReportDebug] buildRequestParams() 已追加 __filter')
    }
    if (hasS && sortConditions.value.length > 0) {
      params['__sort'] = sortConditions.value
      console.log('[ReportDebug] buildRequestParams() 已追加 __sort')
    }

    console.log('[ReportDebug] buildRequestParams() 最终 params:', JSON.parse(JSON.stringify(params)))
    return params
  }

  async function execute() {
    const sql = sqlTemplate.value.trim()
    if (!sql) { ElMessage.warning('请输入 SQL'); return }

    execLoading.value = true
    lastPage.current = 1
    lastPage.size = 20
    try {
      const params = buildRequestParams()
      const requestBody = {
        sqlTemplate: sql,
        parameters: parameters.value,
        params,
        current: lastPage.current,
        size: lastPage.size,
      }
      console.log('[ReportDebug] execute() 发送请求体:', JSON.parse(JSON.stringify(requestBody)))
      const { data } = await AReport.execute(sql, parameters.value, params, lastPage.current, lastPage.size)
      console.log('[ReportDebug] execute() 响应数据:', data)
      resultData.value = data ?? null
      console.log('[ReportDebug] execute() resultData 已设置, columns:', JSON.parse(JSON.stringify(data?.columns)), 'total:', data?.total)
    } catch (err) {
      console.error('[ReportDebug] execute() 查询失败', err)
      ElMessage.error('查询失败')
    } finally {
      execLoading.value = false
    }
  }

  async function onPageChange(v: TableQuery, done: () => void) {
    lastPage.current = v.current
    lastPage.size = v.size
    try {
      const params = buildRequestParams()
      const { data } = await AReport.execute(sqlTemplate.value, parameters.value, params, v.current, v.size)
      resultData.value = data ?? null
    } catch (err) {
      console.error('翻页查询失败', err)
    } finally {
      done()
    }
  }

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

  const tableConfig = computed<TableConfig>(() => {
    const r = resultData.value
    console.log('[ReportDebug] tableConfig computed 触发, resultData:', r ? `columns:${r.columns?.length} total:${r.total}` : 'null')
    if (!r || !r.columns.length) return { columns: [] }
    const cols = r.columns.map((col: ColumnMeta) => {
      const kind = inferSqlType(col.sqlType)
      console.log('[ReportDebug] tableConfig 列:', col.prop, col.label, 'sqlType:', col.sqlType, 'kind:', kind)
      return {
        prop: col.prop,
        label: col.label,
        minWidth: 120,
        align: (kind === 'int' || kind === 'decimal') ? 'right' as const : 'left' as const,
        sortable: true,
      }
    })
    console.log('[ReportDebug] tableConfig 最终 columns:', cols.length, '条')
    return { columns: cols, stripe: true, size: 'small', rowKey: cols[0]?.prop }
  })

  return {
    sqlTemplate,
    parameters,
    paramValues,
    execLoading,
    resultData,
    tableData,
    tableConfig,
    availableColumns,
    filterConditions,
    sortConditions,
    hasBuiltin,
    addFilter,
    removeFilter,
    addSort,
    removeSort,
    setSort,
    clearSort,
    detectParams,
    resetParams,
    updateParams,
    execute,
    onPageChange,
  }
}
