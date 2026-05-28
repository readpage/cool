/**
 * 表格列配置
 */
export interface TableItem {
  label?: string
  prop?: string
  width?: number | string
  minWidth?: number | string
  align?: 'left' | 'center' | 'right'
  type?: 'index'
  fixed?: 'left' | 'right' | boolean
  resizable?: boolean
  sortable?: boolean | 'custom'
  hidden?: boolean
}

/** 筛选操作符 */
export type OperatorType = 'contains' | 'eq' | 'ne' | 'gt' | 'lt' | 'gte' | 'lte' | 'between' | 'in'

/**
 * 筛选项
 */
export interface FilterItem {
  column: string
  operator: OperatorType
  value: string | [string, string] | string[]
}

/**
 * 搜索列配置
 */
export interface ColumnConfig {
  prop: string
  label: string
  operator?: OperatorType
  filterMode?: 'show' | 'exposed' | 'hide'

  /** 控件类型，缺省为 text（el-input） */
  fieldType?: 'text' | 'date' | 'datetime' | 'daterange' | 'datetimerange' | 'select' | 'remote-select'

  /** 下拉选项（fieldType='select' 时使用）。支持 { label, value } 或纯字符串 */
  options?: ({ label: string; value: string } | string)[]
}

/**
 * 搜索配置
 */
export interface SearchConfig {
  filter: ColumnConfig[]
  currentField?: string
  filterValues?: FilterItem[]
}

/**
 * 表格完整配置
 */
export interface TableConfig {
  columns: TableItem[]
  stripe?: boolean
  size?: 'large' | 'default' | 'small'
  height?: number | string
  maxHeight?: number | string
  rowKey?: string
  sort?: { column: string; direction: 'asc' | 'desc' }
  search?: SearchConfig

  /** 全局选项映射表：{ prop: [{ label, value }] }，select / remote-select 共用。
   * 由 Table 挂载时注入 optionsStore，表格翻译和筛选面板都从此读取。
   * 静态 select 选项可直接写死在此；remote-select 由 loadOptions 异步填充。 */
  optionsMap?: Record<string, { label: string; value: string }[]>
}

/**
 * 服务端返回的配置响应
 */
export interface ConfigResponse {
  config: TableConfig
  systemVersion: number
}
