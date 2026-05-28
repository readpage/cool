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

/**
 * 筛选项
 */
export interface FilterItem {
  column: string
  operator: string
  value: string | [string, string] | string[]
}

/**
 * 搜索列配置
 */
export interface ColumnConfig {
  prop: string
  label: string
  operator?: string
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
}

/**
 * 服务端返回的配置响应
 */
export interface ConfigResponse {
  config: TableConfig
  systemVersion: number
}
