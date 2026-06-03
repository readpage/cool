/**
 * 报告相关类型定义 — 与后端 ReportSaveRequest / ReportDisplayConfig 对齐
 */
import type { TableConfig } from '@/types/table'

// ==================== 筛选 & 排序 ====================

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

// ==================== 报告模型（读写复用同一个结构） ====================

/** 报告读写模型 — 与后端 ReportSaveRequest.java 对齐 */
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
    /** 创建者 ID */
    creatorId?: number
  }
  /** 展示配置（filter + sort + tableConfig），来自 sys_config / user_config */
  displayConfig?: ReportDisplayConfig
}

// ==================== 展示配置 ====================

/** 报表展示配置 — 与后端 ReportDisplayConfig.java 对齐 */
export interface ReportDisplayConfig {
  /** 筛选条件列表 */
  filter?: FilterCondition[]
  /** 排序 */
  sort?: SortCondition
  /** 表格配置 */
  tableConfig?: TableConfig
}

// ==================== 列元数据 & 查询结果 ====================

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
