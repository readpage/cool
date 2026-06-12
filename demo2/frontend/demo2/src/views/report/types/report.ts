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
  /** 关联数据源名称（后端冗余返回，免去前端全量拉取数据源列表） */
  datasourceName?: string
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

/**
 * 报告摘要 — 仅用于左侧列表展示
 * 不含 sqlTemplate / permissionConfig / displayConfig 等敏感/冗余字段
 */
export interface ReportSummary {
  id: number
  tableKey: string
  name: string
  description?: string
  category?: string
  displayType?: string
  datasourceName?: string
  updateTime?: string
}

/** 报告查询结果 */
export interface ReportQueryResult {
  columns: ColumnMeta[]
  list: Record<string, any>[]
  total: number
  current: number
  size: number
}

// ==================== 权限 ====================

/** 报表权限配置 DTO — 对齐后端 ReportPermissionDto */
export interface ReportPermissionDto {
  /** 允许访问的角色 ID 列表（白名单模式，为空 = 仅创建者可访问） */
  roleIds: number[]
}

/** 角色 — 对齐后端 role 表 */
export interface Role {
  id: number
  name: string
  nickname: string
}
