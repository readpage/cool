/** 选项样式 */ export interface OptionStyle { tagType?: 'primary' | 'success' | 'warning' | 'danger' | 'info'; dotColor?: string }

/** 选项条目 */ export interface OptionItem { label: string; value: string; style?: OptionStyle }

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

  /** 列数据类型：声明后自动启用选项翻译。select=静态选项，remote-select=动态加载 */
  fieldType?: 'text' | 'select' | 'remote-select'

  /** 静态选项（fieldType='select' 时使用），支持 { label, value, style? } 或纯字符串 */
  options?: (OptionItem | string)[]

  /**
   * 远程选项加载标识（fieldType='remote-select' 时有效）
   * 存在则优先作为 loadOptions(type) 的 type 参数，解决不同表同名字段的选项冲突
   * 例如用户表 sex → optionType: 'user_sex'，宠物表 sex → optionType: 'pet_sex'
   * 不填则 fallback 到 prop
   */
  optionType?: string

  /** 单元格显示格式：text=纯文本（默认），tag=标签，dot=圆点+文本 */
  format?: 'text' | 'tag' | 'dot'
}

/** 选项样式映射表：{ prop: { value: OptionStyle } } */
export type OptionStyles = Record<string, Record<string, OptionStyle>>

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

  /**
   * 远程选项加载标识（fieldType='remote-select' 时有效）
   * 存在则优先作为 loadOptions(type) 的 type 参数，解决不同表同名字段的选项冲突
   * 不填则 fallback 到 prop
   */
  optionType?: string
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
 * 分页查询响应体，{ list: 数据, total: 总数 }
 * 传给 Table 组件的 data prop，组件自动显示分页器
 */
export interface PageResult {
  list: Record<string, any>[]
  total: number
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

  /** 全局选项映射表：{ prop: OptionItem[] }，select / remote-select 共用。
   * 表格翻译和筛选面板都从此读取。静态 select 选项可直接写死在此；
   * OptionItem.style 可为每条选项指定 tag 颜色 / dot 颜色。 */
  optionsMap?: Record<string, OptionItem[]>
}

/** 表格查询参数 */
export interface TableQuery {
  current: number
  size: number
  filter: FilterItem[]
  sort?: { column: string; direction: string }
  /** 可见列配置（导出用：已过滤 hidden=true 的列） */
  columns?: TableItem[]
}

/**
 * 服务端返回的配置响应
 */
export interface ConfigResponse {
  config: TableConfig
  systemVersion: number
}
