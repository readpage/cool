/**
 * 搜索组件内部类型（公共类型从 ../types 引入）
 */
import type { ColumnConfig, FieldType, FilterOperator, OptionItem, OptionStyle } from '../types'

export type { ColumnConfig, FieldType, FilterOperator, OptionItem, OptionStyle }

/** 筛选条件（内部状态） */
export interface FilterCondition {
  column: string
  operator: FilterOperator
  value: string | string[]
  valueStr: string
  filterMode: 'show' | 'exposed' | 'hide'
}

/** 筛选条件输出 */
export interface FilterResult {
  column: string
  operator: FilterOperator
  value: string | [string, string] | string[]
}

/** 操作符选项 */
export interface OperatorOption {
  label: string
  value: string
}
