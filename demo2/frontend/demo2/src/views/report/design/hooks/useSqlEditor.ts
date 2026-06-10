/**
 * SQL 编辑器状态 — 管理 SQL 文本、实时解析列信息、判断占位符、提取 #{key} 变量
 */
import { ref, computed, watch } from 'vue'
import { hasBuiltin, parseSqlColumns } from './useFilterSort'

/** 从 SQL 模板中提取 #{key} 变量名列表（去重） */
export function parseVariableKeys(sql: string): string[] {
  const re = /#\{(\w+)\}/g
  const keys = new Set<string>()
  let m: RegExpExecArray | null
  while ((m = re.exec(sql)) !== null) {
    keys.add(m[1])
  }
  return [...keys]
}

/**
 * 区分必填变量（不在 [[...]] 可选块内）与可选变量（在 [[...]] 内）。
 * 返回 { requiredKeys, optionalKeys }
 */
export function parseVariableCategories(sql: string): { requiredKeys: string[]; optionalKeys: string[] } {
  const required = new Set<string>()
  const optional = new Set<string>()
  let depth = 0
  let i = 0

  while (i < sql.length) {
    if (sql.startsWith('[[')) {
      depth++
      i += 2
    } else if (sql.startsWith(']]') && depth > 0) {
      depth--
      i += 2
    } else if (sql.startsWith('#{')) {
      const end = sql.indexOf('}', i + 2)
      if (end > i) {
        const key = sql.substring(i + 2, end).trim()
        if (depth > 0) {
          optional.add(key)
        } else {
          required.add(key)
          // 如果也在可选块出现过，从 required 移除（不重复提示）
          if (optional.has(key)) optional.delete(key)
        }
        i = end + 1
      } else {
        i++
      }
    } else {
      i++
    }
  }

  // required 中的 key 不应同时出现在 optional 中
  for (const k of required) optional.delete(k)

  return {
    requiredKeys: [...required],
    optionalKeys: [...optional],
  }
}

export function useSqlEditor() {
  const sqlTemplate = ref('')

  /** 带防抖的列解析，避免每次按键都触发 computed 连锁更新 */
  const availableColumns = ref<string[]>([])
  const _rawColumns = computed(() => parseSqlColumns(sqlTemplate.value))
  let debounceTimer: ReturnType<typeof setTimeout>
  watch(_rawColumns, (cols) => {
    clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
      availableColumns.value = cols
    }, 200)
  }, { immediate: true })

  const showFilter = computed(() => hasBuiltin(sqlTemplate.value, 'filter'))
  const showSort = computed(() => hasBuiltin(sqlTemplate.value, 'sort'))

  /** #{key} 变量名列表 */
  const variableKeys = computed(() => parseVariableKeys(sqlTemplate.value))

  return { sqlTemplate, availableColumns, showFilter, showSort, variableKeys }
}
