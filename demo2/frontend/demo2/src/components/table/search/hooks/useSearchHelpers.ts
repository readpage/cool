import { type Ref, computed } from 'vue'
import type { ColumnConfig, FieldType, FilterOperator, FilterCondition, FilterResult, OptionItem, OperatorOption } from '../types'

export type { ColumnConfig, FieldType, FilterOperator, FilterCondition, FilterResult, OptionItem, OperatorOption }
export type RemoteMethod = (query: string) => Promise<OptionItem[]>

/* ============ 常量 ============ */

/** 每个 fieldType 允许的操作符 */
export const OPERATOR_MAP: Record<string, string[]> = {
  text:           ['contains', 'eq', 'ne', 'gt', 'lt', 'gte', 'lte', 'between', 'in'],
  date:           ['eq', 'ne', 'gt', 'lt', 'gte', 'lte'],
  datetime:       ['eq', 'ne', 'gt', 'lt', 'gte', 'lte'],
  daterange:      ['between'],
  datetimerange:  ['between'],
  select:         ['eq', 'ne', 'in'],
  'remote-select':['eq', 'ne', 'in'],
}

/* ============ 工具函数 ============ */

/** 判断筛选条件是否填写了有效值（为 true 表示有值，应保留） */
export function isEmptyValue(c: { operator: string; value: any; valueStr?: string }): boolean {
  if (c.operator === 'between') {
    if (c.value == null) return false
    return Array.isArray(c.value) ? (c.value[0] !== '' || c.value[1] !== '') : false
  }
  if (c.operator === 'in') return Array.isArray(c.value) ? c.value.length > 0 : (c.valueStr ?? '') !== ''
  return c.value !== ''
}

/** 根据 fieldType 返回默认操作符 */
export function defaultOperator(col?: { fieldType?: string }): FilterOperator {
  const ft = col?.fieldType
  if (ft === 'daterange' || ft === 'datetimerange') return 'between'
  if (ft === 'select' || ft === 'remote-select') return 'eq'
  return 'contains'
}

/** 将内部条件数组转为 FilterResult[]，供 query.vue 和 index.vue 共用 */
export function buildFilter(values: FilterCondition[]): FilterResult[] {
  return values
    .filter((c) => c.column && isEmptyValue(c))
    .map((c): FilterResult => ({
      column: c.column,
      operator: c.operator,
      value:
        c.operator === 'between' ? [c.value[0] ?? '', c.value[1] ?? ''] :
        c.operator === 'in' ? (Array.isArray(c.value) ? c.value : c.valueStr.split(',').map((v) => v.trim()).filter(Boolean)) :
        c.value,
    }))
}

/** 清除符合条件的值，供 query.vue 和 ExposedFilter.vue 共用 */
export function clearConditionValue(c: FilterCondition) {
  if (c.operator === 'between') {
    c.value = ['', '']
  } else if (c.operator === 'in') {
    c.valueStr = ''
    c.value = ''
  } else {
    c.value = ''
  }
}

/* ============ Hook ============ */

/**
 * 搜索面板辅助函数，供 query.vue / ExposedFilter.vue 公用
 */
export function useSearchHelpers(
  columns: Ref<ColumnConfig[]>,
  loadOptions?: Ref<((type: string, keyword?: string) => Promise<OptionItem[]>) | undefined>,
  operatorOptions?: Ref<OperatorOption[]>,
) {
  const colMap = computed(() => {
    const map: Record<string, ColumnConfig> = {}
    for (const c of columns.value) map[c.prop] = c
    return map
  })

  function getColByProp(prop: string): ColumnConfig | undefined {
    return colMap.value[prop]
  }

  function getFieldType(prop: string): FieldType | undefined {
    return getColByProp(prop)?.fieldType
  }

  function isDateRangeField(prop: string): boolean {
    const ft = getFieldType(prop)
    return ft === 'daterange' || ft === 'datetimerange'
  }

  function getDateRangeType(prop: string): string {
    const ft = getFieldType(prop)
    return ft === 'datetimerange' ? 'datetimerange' : 'daterange'
  }

  function getDateFormat(prop: string): string {
    const ft = getFieldType(prop)
    return ft === 'datetime' || ft === 'datetimerange' ? 'YYYY-MM-DD HH:mm:ss' : 'YYYY-MM-DD'
  }

  function isSelectField(prop: string): boolean {
    const ft = getFieldType(prop)
    return ft === 'select' || ft === 'remote-select'
  }

  function getOptions(prop: string) {
    return getColByProp(prop)?.options
  }

  function getRemoteMethod(prop: string): RemoteMethod | undefined {
    if (!loadOptions?.value) return undefined
    // optionType 优先，fallback 到 prop（解决不同表同名字段选项冲突）
    const col = getColByProp(prop)
    const optionType = (col as any)?.optionType || prop
    return (keyword: string) => loadOptions.value!(optionType, keyword)
  }

  function getAvailableOperators(prop: string) {
    const ops = operatorOptions?.value ?? []
    const col = getColByProp(prop)
    const ft = col?.fieldType ?? 'text'
    const allowed = OPERATOR_MAP[ft] ?? OPERATOR_MAP.text
    return ops.filter((op) => allowed.includes(op.value))
  }

  return {
    getColByProp,
    getFieldType,
    isDateRangeField,
    getDateRangeType,
    getDateFormat,
    isSelectField,
    getOptions,
    getRemoteMethod,
    getAvailableOperators,
  }
}
