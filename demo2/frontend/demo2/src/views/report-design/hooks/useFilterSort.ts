/**
 * 过滤/排序条件管理（对应 SQL 模板中的 {{filter}} / {{sort}} / {{column}}）
 * 纯状态 + 纯工具函数，不包含任何 API 调用
 */
import { ref } from 'vue'
import type { FilterCondition, SortCondition, ColumnMeta } from '@/types/report'

/** 根据列 SQL 类型 + 列名标签推断默认运算符 */
function inferDefaultOperator(sqlType: number, label?: string): string {
  if (sqlType === 91 || sqlType === 92 || sqlType === 93) return 'between'
  if ([2, 3, 4, 5, 6, 8].includes(sqlType)) return 'eq'
  if (label) {
    if (/时间|日期|date|time|create|update|birth/i.test(label)) return 'between'
    if (/年龄|年龄|id|数量|计数|count|num|金额|price|score/i.test(label)) return 'eq'
  }
  return 'like'
}

/** 从 SQL 模板中解析 SELECT...FROM 之间的列名（不依赖后端执行） */
export function parseColumnsFromSql(sql: string): { prop: string; label: string; sqlType: number }[] {
  const match = sql.match(/SELECT\s+(.+?)\s+FROM\b/i)
  if (!match) return []

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
    let prop = part.trim()
    let label = prop

    const sqMatch = prop.match(/^(.+?)\s+'([^']+)'\s*$/i)
    if (sqMatch) {
      prop = sqMatch[1].trim()
      label = sqMatch[2]
    } else {
      const asMatch = prop.match(/^(.+?)\s+AS\s+['"`]?([^'"`]+)['"`]?\s*$/i)
      if (asMatch) {
        prop = asMatch[1].trim()
        label = asMatch[2].trim()
      }
    }

    prop = prop.replace(/^[\w_]+\./, '')
    result.push({ prop, label, sqlType: 0 })
  }

  return result
}

/** 检测 SQL 模板中是否包含内置占位符 */
export function hasBuiltin(sql: string, type: 'filter' | 'sort' | 'column'): boolean {
  return new RegExp(`\\{\\{${type}\\}\\}`, 'i').test(sql)
}

// ==================== 组合式 hooks ====================

export function useFilterSort() {
  const filterConditions = ref<FilterCondition[]>([])
  const sortConditions = ref<SortCondition[]>([])

  function addFilter() {
    filterConditions.value.push({ column: '', operator: 'eq', value: '' })
  }

  function removeFilter(idx: number) {
    filterConditions.value.splice(idx, 1)
  }

  function addSort() {
    sortConditions.value.push({ column: '', direction: 'asc' })
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

  /** 获取有效的过滤条件（去除空值） */
  function getEffectiveFilters() {
    return filterConditions.value.filter(f => {
      const v = f.value
      if (v === undefined || v === null || v === '') return false
      if (Array.isArray(v) && v.length === 0) return false
      return true
    })
  }

  /** 根据列信息自动生成筛选/排序默认行 */
  function autoInitFromColumns(
    columns: { prop: string; label: string; sqlType: number }[],
    sqlTemplate: string,
  ) {
    const hasF = hasBuiltin(sqlTemplate, 'filter')
    const hasS = hasBuiltin(sqlTemplate, 'sort')

    if (hasF && filterConditions.value.length === 0 && columns.length > 0) {
      const startIdx = columns.length > 1 ? 1 : 0
      for (let i = startIdx; i < columns.length; i++) {
        const col = columns[i]
        const op = inferDefaultOperator(col.sqlType, col.label)
        filterConditions.value.push({ column: col.prop, operator: op, value: '' })
      }
    }

    if (hasS && sortConditions.value.length === 0 && columns.length > 0) {
      sortConditions.value.push({ column: columns[0].prop, direction: 'asc' })
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
    getEffectiveFilters,
    autoInitFromColumns,
  }
}
