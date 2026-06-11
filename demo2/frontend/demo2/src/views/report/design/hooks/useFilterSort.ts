/**
 * 过滤/排序条件管理（对应 SQL 模板中的 {{filter}} / {{sort}} / {{column}}）
 * 
 * 列信息（prop/label）直接从 SQL 编辑器解析生成，不再依赖后端返回的列元数据。
 * filter.column 全程使用 prop（列原名），label 仅用于前端 UI 显示文本。
 */
import { ref } from 'vue'
import type { FilterCondition, SortCondition } from '../../types/report'

// ==================== SQL 列解析 ====================

/** 按逗号分割 SELECT-FROM 之间的列表达式，处理括号嵌套 */
function splitByComma(raw: string): string[] {
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
  return parts
}

/** 剥离表前缀（如 t.column → column） */
function stripTablePrefix(name: string): string {
  return name.replace(/^[\w_]+\./, '').trim()
}

/** 从单个列表达式中提取 prop（原名，保留表前缀以免多表歧义）和 label（显示名） */
function extractColInfo(expr: string): { prop: string; label: string } {
  const trimmed = expr.trim()

  // 单引号别名: s.prod_num '编码'
  let m = trimmed.match(/^(.+?)\s+'([^']+)'\s*$/i)
  if (m) return { prop: m[1].trim(), label: m[2] }

  // 双引号别名: s.prod_num "名称"
  m = trimmed.match(/^(.+?)\s+"([^"]+)"\s*$/i)
  if (m) return { prop: m[1].trim(), label: m[2] }

  // 反引号别名
  m = trimmed.match(/^(.+?)\s+`([^`]+)`\s*$/i)
  if (m) return { prop: m[1].trim(), label: m[2] }

  // AS 别名: COUNT(*) AS total_count
  m = trimmed.match(/^(.+?)\s+AS\s+(.+)$/i)
  if (m) return { prop: m[2].trim(), label: m[2].trim() }

  // 无引号空格别名: ITM.ITMREF_0 prod_num
  m = trimmed.match(/^(.+?)\s+([a-zA-Z_]\w*)$/i)
  if (m) return { prop: m[1].trim(), label: m[2] }

  // 无别名：prop 保留表前缀（如 s.prod_num），label 去前缀用于显示
  const prop = trimmed
  return { prop, label: stripTablePrefix(prop) }
}

/**
 * 计算字符串中指定位置之前（不含 pos）的括号深度。
 */
function parenDepth(s: string, pos: number): number {
  let depth = 0
  let inSingle = false
  let inDouble = false
  for (let i = 0; i < pos; i++) {
    const c = s[i]
    if (inSingle) { if (c === "'") inSingle = false; continue }
    if (inDouble) { if (c === '"') inDouble = false; continue }
    if (c === "'") { inSingle = true; continue }
    if (c === '"') { inDouble = true; continue }
    if (c === '(') depth++
    else if (c === ')') depth--
  }
  return depth
}

/**
 * 从 SQL 模板中解析 SELECT 与 FROM 之间的列信息。
 * 取最外层（括号深度为 0）的最后一个 SELECT...FROM，跳过子查询。
 * prop 取原名，label 取别名，没有别名则取原名。
 */
export function parseSqlColumns(sql: string): { prop: string; label: string }[] {
  const allMatches = [...sql.matchAll(/\bSELECT\s+(.+?)\s+FROM\b/gis)]
  if (allMatches.length === 0) return []

  // 只保留括号深度为 0（最外层）的 SELECT...FROM
  const outerMatches = allMatches.filter(m => parenDepth(sql, m.index!) === 0)
  if (outerMatches.length === 0) return []
  const match = outerMatches[outerMatches.length - 1]

  const raw = match[1]
  const parts = splitByComma(raw)
  const result: { prop: string; label: string }[] = []

  for (const part of parts) {
    if (part === '*' || part === '') continue
    result.push(extractColInfo(part))
  }

  return result
}

// ==================== 运算符推断 ====================

/** 根据列名标签推断默认运算符 */
function inferDefaultOperator(label?: string): string {
  if (label) {
    if (/时间|日期|date|time|create|update|birth/i.test(label)) return 'between'
    if (/年龄|id|数量|计数|count|num|金额|price|score/i.test(label)) return 'eq'
  }
  return 'like'
}

/**
 * 校验 SQL 模板中是否有表达式/函数列缺少别名。
 * 返回缺少别名的列表达式列表，为空表示全部合法。
 */
export function validateSqlAliases(sql: string): string[] {
  const allMatches = [...sql.matchAll(/\bSELECT\s+(.+?)\s+FROM\b/gis)]
  if (allMatches.length === 0) return []

  // 只保留括号深度为 0（最外层）的 SELECT...FROM
  const outerMatches = allMatches.filter(m => parenDepth(sql, m.index!) === 0)
  if (outerMatches.length === 0) return []
  const match = outerMatches[outerMatches.length - 1]

  const parts = splitByComma(match[1])
  const missing: string[] = []

  for (const part of parts) {
    const expr = part.trim()
    if (expr === '*' || expr === '') continue

    // 检查是否有别名（单引号/双引号/反引号/AS 别名/无引号空格别名）
    const hasAlias = /^.+?\s+'[^']+'\s*$/i.test(expr)
      || /^.+?\s+"[^"]+"\s*$/i.test(expr)
      || /^.+?\s+`[^`]+`\s*$/i.test(expr)
      || /^.+?\s+AS\s+.+$/i.test(expr)
      || /^.+?\s+[a-zA-Z_]\w*\s*$/i.test(expr)

    if (!hasAlias) {
      const pureCol = expr.replace(/^[\w_]+\./, '').replace(/\s+/g, ' ')
      // 简单列名（纯标识符）无需别名
      if (/^[a-zA-Z_]\w*$/.test(pureCol)) continue
      // 带表前缀的简单列也无问题
      if (/^[a-zA-Z_]\w*\.[a-zA-Z_]\w*$/.test(pureCol)) continue
      missing.push(expr)
    }
  }

  return missing
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

  /** 将列信息转为筛选/排序用的列列表 */  
  function getAvailableColumns(columns: { prop: string; label: string }[]) {
    return columns.map(c => ({ prop: c.prop, label: c.label }))
  }

  /** 获取有效的过滤条件（去除空值），返回的 filter.column 均为 prop */
  function getEffectiveFilters() {
    return filterConditions.value.filter(f => {
      const v = f.value
      if (v === undefined || v === null || v === '') return false
      if (Array.isArray(v) && v.length === 0) return false
      return true
    })
  }

  /**
   * 根据列信息自动生成筛选/排序默认行。
   * filterConditions[].column 统一使用 col.prop（列原名）。
   */
  function autoInitFromColumns(
    columns: { prop: string; label: string }[],
    sqlTemplate: string,
  ) {
    const hasF = hasBuiltin(sqlTemplate, 'filter')
    const hasS = hasBuiltin(sqlTemplate, 'sort')

    if (hasF && filterConditions.value.length === 0 && columns.length > 0) {
      const startIdx = columns.length > 1 ? 1 : 0
      for (let i = startIdx; i < columns.length; i++) {
        const col = columns[i]
        const op = inferDefaultOperator(col.label)
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
