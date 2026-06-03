/**
 * 报告相关类型定义
 */

/** 下拉选项 */
export interface OptionDef {
  label: string
  value: string
}

/** 报告查询参数定义 */
export interface ParamDef {
  /** 参数名（模板中使用 #{name}） */
  name: string
  /** 显示标签 */
  label: string
  /** 参数类型 */
  type: 'text' | 'number' | 'date' | 'daterange' | 'select' | 'remote-select'
  /** 默认值 */
  defaultValue?: string
  /** 是否必填 */
  required?: boolean
  /** type=select 时的静态选项 */
  options?: OptionDef[]
  /** type=remote-select 时的远程选项类型标识 */
  optionType?: string
  /** 内置参数类型（filter / sort / column），普通参数不设置 */
  builtin?: 'filter' | 'sort' | 'column'
}

/** 过滤条件 */
export interface FilterCondition {
  /** 列名 */
  column: string
  /** 运算符: eq, ne, gt, lt, gte, lte, like, in, between */
  operator: string
  /** 值 */
  value: any
}

/** 排序条件 */
export interface SortCondition {
  /** 列名 */
  column: string
  /** asc / desc */
  direction: 'asc' | 'desc'
}

/** 报告定义 — 与后端 ReportDefinition.java 对齐 */
export interface ReportDefinition {
  id: string
  name: string
  description?: string
  /** SQL 模板（支持 {{key}} / #{key} / [[...]] 语法） */
  sqlTemplate: string
  /** 参数定义列表 */
  parameters: ParamDef[]
}

/** 列元数据 — 与后端 ReportQueryResult.ColumnMeta 对齐 */
export interface ColumnMeta {
  prop: string         // col_0, col_1, ...
  label: string        // SQL 别名显示名
  sqlType: number      // java.sql.Types
}

/** 报告查询结果 */
export interface ReportQueryResult {
  columns: ColumnMeta[]
  list: Record<string, any>[]
  total: number
  current: number
  size: number
}
