import { type Ref, computed } from 'vue'

/* ============ 类型 ============ */

export interface ColumnConfig {
  prop: string
  label: string
  fieldType?: 'text' | 'date' | 'datetime' | 'daterange' | 'datetimerange' | 'select' | 'remote-select'
  options?: ({ label: string; value: string } | string)[]
}

export type RemoteMethod = (query: string) => Promise<{ label: string; value: string }[]>

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
  if (c.operator === 'between') return Array.isArray(c.value) ? (c.value[0] !== '' || c.value[1] !== '') : c.value !== ''
  if (c.operator === 'in') return Array.isArray(c.value) ? c.value.length > 0 : (c.valueStr ?? '') !== ''
  return c.value !== ''
}

/* ============ Hook ============ */

/**
 * 搜索面板辅助函数，供 query.vue / ExposedFilter.vue 公用
 */
export function useSearchHelpers(
  columns: Ref<ColumnConfig[]>,
  loadOptions?: Ref<((type: string, keyword?: string) => Promise<{ label: string; value: string }[]>) | undefined>,
  operatorOptions?: Ref<{ label: string; value: string }[]>,
) {
  /** 根据 prop 查找列配置 */
  function getColByProp(prop: string): ColumnConfig | undefined {
    return columns.value.find((c) => c.prop === prop)
  }

  /** 获取字段类型 */
  function getFieldType(prop: string): string | undefined {
    return getColByProp(prop)?.fieldType
  }

  /** 是否为日期范围字段 */
  function isDateRangeField(prop: string): boolean {
    const ft = getFieldType(prop)
    return ft === 'daterange' || ft === 'datetimerange'
  }

  /** 日期范围选择器类型 */
  function getDateRangeType(prop: string): string {
    const ft = getFieldType(prop)
    return ft === 'datetimerange' ? 'datetimerange' : 'daterange'
  }

  /** 日期格式化字符串 */
  function getDateFormat(prop: string): string {
    const ft = getFieldType(prop)
    return ft === 'datetime' || ft === 'datetimerange' ? 'YYYY-MM-DD HH:mm:ss' : 'YYYY-MM-DD'
  }

  /** 是否为下拉（含远程搜索） */
  function isSelectField(prop: string): boolean {
    const ft = getFieldType(prop)
    return ft === 'select' || ft === 'remote-select'
  }

  /** 获取静态选项 */
  function getOptions(prop: string) {
    return getColByProp(prop)?.options
  }

  /** 获取远程搜索方法 */
  function getRemoteMethod(prop: string): RemoteMethod | undefined {
    if (!loadOptions?.value) return undefined
    return (keyword: string) => loadOptions.value!(prop, keyword)
  }

  /** 根据 fieldType 返回合法的操作符列表（computed 返回函数，支持响应式 operatorOptions） */
  const getAvailableOperators = computed(() => {
    const ops = operatorOptions?.value ?? []
    return (prop: string) => {
      const col = getColByProp(prop)
      const ft = col?.fieldType ?? 'text'
      const allowed = OPERATOR_MAP[ft] ?? OPERATOR_MAP.text
      return ops.filter((op) => allowed.includes(op.value))
    }
  })

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
