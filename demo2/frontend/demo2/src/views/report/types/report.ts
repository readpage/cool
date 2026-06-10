/**
 * 报告相关类型定义（报告模块自包含，不依赖 @/types/）
 */
import type { TableConfig } from './table'

// ==================== 筛选 & 排序 ====================

/** 过滤条件 */
export interface FilterCondition {
  /** 列名 */
  column: string
  /** 运算符: eq, ne, gt, lt, gte, lte, like, in, between */
  operator: string
  /** 值 */
  value: any
  /** 标记为 SQL 模板变量（如 #{year}→2025），不生成 WHERE，值通过 JDBC 参数化自动防注入 */
  variable?: boolean
}

/** 排序条件 */
export interface SortCondition {
  /** 列名 */
  column: string
  /** asc / desc */
  direction: 'asc' | 'desc'
}

// ==================== 报告模型（读写复用同一个结构） ====================

/** 报告读写模型 */
export interface ReportSaveRequest {
  /** 报告实体（直接映射 report 表） */
  report: {
    /** 主键（自增），新增时为 undefined */
    id?: number
    /** 报表业务唯一标识 */
    tableKey: string
    name: string
    description?: string
    category?: string
    /** SQL 模板（支持 {{filter}} / {{sort}} / #{key} 语法） */
    sqlTemplate: string
    /** 展示类型：table | bar | line | pie | number */
    displayType?: string
    /** 权限配置 JSON */
    permissionConfig?: string
    /** 关联数据源 ID，null=默认主数据源 */
    datasourceId?: number | null
    /** 创建者 ID */
    creatorId?: number
  }
  /** 展示配置（TableConfig 扁平格式，sort/filter 值内置其中），来自 sys_config / user_config */
  displayConfig?: TableConfig
}

// ==================== 列元数据 & 查询结果 ====================

/** 列元数据 */
export interface ColumnMeta {
  /** 列原名，用于 WHERE/ORDER BY 筛选排序 */
  prop: string
  /** 显示名 */
  label: string
  /** java.sql.Types 值 */
  sqlType: number
}

/** 报告查询结果 */
export interface ReportQueryResult {
  columns: ColumnMeta[]
  list: Record<string, any>[]
  total: number
  current: number
  size: number
}
