import type { FormItemRule } from 'element-plus'
import type { OptionItem } from '@/types/table'

/** 异步操作完成回调 */
export type DoneCallback = (success?: boolean) => void

// ==================== 表单字段类型 ====================

/** 表单字段控件类型 */
export type FormFieldType =
  | 'input'         // el-input
  | 'number'        // el-input-number
  | 'select'        // el-select（静态选项）
  | 'remote-select' // el-select（远程搜索，走 loadOptions）
  | 'date'          // el-date-picker (type=date)
  | 'daterange'     // el-date-picker (type=daterange)
  | 'textarea'      // el-input (type=textarea)
  | 'switch'        // el-switch

/** 表单字段配置 */
export interface FormItemConfig {
  prop: string
  label: string
  fieldType?: FormFieldType
  placeholder?: string
  required?: boolean
  rules?: FormItemRule | FormItemRule[]
  /** 静态选项（fieldType='select'） */
  options?: (OptionItem | string)[]
  defaultValue?: any
  /** 透传给 el-xxx 的额外属性 */
  componentProps?: Record<string, any>
  /** 事件监听，如 { change: fn, focus: fn } */
  event?: Record<string, (...args: any[]) => void>
  /** 表单行宽度 */
  width?: number | string
  /** 标签宽度（覆盖全局 label-width） */
  labelWidth?: number | string
  /** 是否可清空，默认 true（仅 input/textarea/select 生效） */
  clearable?: boolean
  /** 新增时隐藏 */
  hideAdd?: boolean
  /** 修改时隐藏 */
  hideEdit?: boolean
  disabled?: boolean

  /**
   * 远程选项加载标识（fieldType='remote-select' 时有效）
   * 存在则优先作为 loadOptions(type) 的 type 参数，解决不同表同名字段的选项冲突
   * 不填则 fallback 到 prop
   */
  optionType?: string
}

// ==================== CRUD API ====================

/** CRUD API 配置 */
export interface CrudApi {
  /** 新增方法：定义了则显示"新增"按钮 */
  save?: (data: any, done: DoneCallback) => void
  /** 修改方法：定义了则显示"修改"按钮 */
  update?: (data: any, done: DoneCallback) => void
  /** 删除方法：定义了则显示"删除"按钮（顶部工具栏 + selection 栏） */
  remove?: (ids: any[], done: DoneCallback) => void
  /** 导入方法：定义了则在"更多"中显示"导入"选项 */
  import?: () => void
  /** 导出方法：定义了则在"更多"中显示"导出"选项 */
  export?: () => void

  /** 新增前置钩子 */
  beforeSave?: (data: any) => void
  /** 修改前置钩子 */
  beforeUpdate?: (data: any) => void
  /** 删除前置钩子 */
  beforeRemove?: (ids: any[]) => void

  /** 禁用控制 */
  disable?: ('save' | 'update' | 'remove')[]
}
