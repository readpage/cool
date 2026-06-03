/**
 * 过滤/排序条件管理（对应 SQL 模板中的 {{filter}} / {{sort}} / {{column}}）
 */
import { ref } from 'vue'
import type { FilterCondition, SortCondition, ColumnMeta } from '@/types/report'

/** 根据列 SQL 类型 + 列名标签推断默认运算符 */
function inferDefaultOperator(sqlType: number, label?: string): string {
  // 已知类型：日期/时间 → 区间，数字 → 等于
  if (sqlType === 91 || sqlType === 92 || sqlType === 93) return 'between'
  if ([2, 3, 4, 5, 6, 8].includes(sqlType)) return 'eq'
  // 未知类型(sqlType=0)，根据列名标签文本推断
  if (label) {
    if (/时间|日期|date|time|create|update|birth/i.test(label)) return 'between'
    if (/年龄|年龄|id|数量|计数|count|num|金额|price|score/i.test(label)) return 'eq'
  }
  return 'like'
}

/** 从 SQL 模板中解析 SELECT...FROM 之间的列名（不依赖后端执行） */
export function parseColumnsFromSql(sql: string): { prop: string; label: string; sqlType: number }[] {
  const match = sql.match(/SELECT\s+(.+?)\s+FROM\b/i)
  if (!match) {
    console.log('[ReportDebug] parseColumnsFromSql 未匹配到 SELECT...FROM')
    return []
  }

  // 按逗号拆分（处理函数嵌套括号）
  const raw = match[1]
  const parts: string[] = []
  let depth = 0
  let current = ''
  for (let i = 0; i < raw.length; i++) {
    const ch = raw[i]
    if (ch === '(') depth++
    else if (ch === ')') depth--
    else if (ch === ',' && depth === 0) {
      parts.push(current.trim())
      current = ''
      continue
    }
    current += ch
  }
  if (current.trim()) parts.push(current.trim())

  const result: { prop: string; label: string; sqlType: number }[] = []
  for (const part of parts) {
    // 处理 "column '标签'" 或 "column AS 标签" 或纯 "column"
    let prop = part.trim()
    let label = prop

    // 单引号别名: username '用户名'
    const sqMatch = prop.match(/^(.+?)\s+'([^']+)'\s*$/i)
    if (sqMatch) {
      prop = sqMatch[1].trim()
      label = sqMatch[2]
    } else {
      // AS 别名: username AS 用户名
      const asMatch = prop.match(/^(.+?)\s+AS\s+['"`]?([^'"`]+)['"`]?\s*$/i)
      if (asMatch) {
        prop = asMatch[1].trim()
        label = asMatch[2].trim()
      }
    }

    // 去掉表前缀 t. 或 table.
    prop = prop.replace(/^[\w_]+\./, '')

    result.push({ prop, label, sqlType: 0 })
  }

  console.log('[ReportDebug] parseColumnsFromSql 解析结果:', JSON.parse(JSON.stringify(result)))
  return result
}

export function useFilterSort() {
  const filterConditions = ref<FilterCondition[]>([])
  const sortConditions = ref<SortCondition[]>([])

  function addFilter() {
    filterConditions.value.push({ column: '', operator: 'eq', value: '' })
    console.log('[ReportDebug] useFilterSort.addFilter() 新增过滤条件, 当前:', JSON.parse(JSON.stringify(filterConditions.value)))
  }

  function removeFilter(idx: number) {
    filterConditions.value.splice(idx, 1)
    console.log('[ReportDebug] useFilterSort.removeFilter() idx:', idx, '剩余:', JSON.parse(JSON.stringify(filterConditions.value)))
  }

  function addSort() {
    sortConditions.value.push({ column: '', direction: 'asc' })
    console.log('[ReportDebug] useFilterSort.addSort() 新增排序条件, 当前:', JSON.parse(JSON.stringify(sortConditions.value)))
  }

  function setSort(column: string, direction: 'asc' | 'desc') {
    const exist = sortConditions.value.find(s => s.column === column)
    if (exist) {
      exist.direction = direction
    } else {
      sortConditions.value.push({ column, direction })
    }
  }

  function removeSort(idx: number) {
    sortConditions.value.splice(idx, 1)
  }

  function clearSort(column: string) {
    sortConditions.value = sortConditions.value.filter(s => s.column !== column)
  }

  function getAvailableColumns(columns: ColumnMeta[]) {
    return columns.map(c => ({ prop: c.prop, label: c.label, sqlType: c.sqlType }))
  }

  /** 检测 SQL 模板中是否包含内置占位符 */
  function hasBuiltin(sql: string, type: 'filter' | 'sort' | 'column'): boolean {
    const result = new RegExp(`\\{\\{${type}\\}\\}`, 'i').test(sql)
    console.log(`[ReportDebug] useFilterSort.hasBuiltin('${type}'):`, result, 'SQL:', sql?.substring(0, 80))
    return result
  }

  /** 根据查询结果列信息自动生成筛选/排序默认行 */
  function autoInitFromColumns(
    columns: { prop: string; label: string; sqlType: number }[],
    sqlTemplate: string,
  ) {
    const hasF = hasBuiltin(sqlTemplate, 'filter')
    const hasS = hasBuiltin(sqlTemplate, 'sort')
    console.log('[ReportDebug] autoInitFromColumns 入参 hasF:', hasF, 'hasS:', hasS, 'filterConditions.length:', filterConditions.value.length, 'columns.length:', columns.length)

    // 筛选：跳过第 1 列（通常是 id），为其余列全部生成
    if (hasF && filterConditions.value.length === 0 && columns.length > 0) {
      const startIdx = columns.length > 1 ? 1 : 0
      console.log('[ReportDebug] autoInitFromColumns 开始生成筛选行, startIdx:', startIdx)
      for (let i = startIdx; i < columns.length; i++) {
        const col = columns[i]
        const op = inferDefaultOperator(col.sqlType, col.label)
        console.log('[ReportDebug] autoInitFromColumns 列', i, col.prop, col.label, 'sqlType:', col.sqlType, '→ operator:', op)
        filterConditions.value.push({
          column: col.prop,
          operator: op,
          value: '',
        })
      }
      console.log('[ReportDebug] autoInitFromColumns 自动生成筛选行:', filterConditions.value.length, '条')
    } else {
      console.log('[ReportDebug] autoInitFromColumns 跳过筛选生成, hasF:', hasF, 'len:', filterConditions.value.length, 'colsLen:', columns.length)
    }

    // 排序：为第 1 列生成默认升序
    if (hasS && sortConditions.value.length === 0 && columns.length > 0) {
      sortConditions.value.push({
        column: columns[0].prop,
        direction: 'asc',
      })
      console.log('[ReportDebug] autoInitFromColumns 自动生成排序行: 1 条')
    } else {
      console.log('[ReportDebug] autoInitFromColumns 跳过排序生成, hasS:', hasS, 'len:', sortConditions.value.length, 'colsLen:', columns.length)
    }
  }

  return {
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
  }
}
