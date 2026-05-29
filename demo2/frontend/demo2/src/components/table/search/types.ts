/** 列配置 */
export interface ColumnConfig {
  prop: string
  label: string
  operator?: FilterOperator
  filterMode?: 'show' | 'exposed' | 'hide'
  fieldType?: FieldType
  options?: ({ label: string; value: string } | string)[]
}

/** 字段控件类型 */
export type FieldType = 'text' | 'date' | 'datetime' | 'daterange' | 'datetimerange' | 'select' | 'remote-select'

/** 操作符 */
export type FilterOperator = 'contains' | 'eq' | 'ne' | 'gt' | 'lt' | 'gte' | 'lte' | 'between' | 'in'

/** 筛选条件（内部状态） */
export interface FilterCondition {
  column: string
  operator: FilterOperator
  value: string | string[]
  valueStr: string
  display: boolean
}

/** 筛选条件输出 */
export interface FilterResult {
  column: string
  operator: FilterOperator
  value: string | [string, string] | string[]
}

/** 选项条目 */
export interface OptionItem {
  label: string
  value: string
}

/** 操作符选项 */
export interface OperatorOption {
  label: string
  value: string
}
