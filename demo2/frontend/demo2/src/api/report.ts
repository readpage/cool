/**
 * 报告管理 API — 对应 ReportController 接口
 */
import { apiAxios, download } from './src/requests'
import type { ResApi } from './src/requests'
import type { ReportSaveRequest, ReportQueryResult, FilterCondition, SortCondition } from '@/views/report/types/report'
import type { TableItem } from '@/views/report/types/table'

// ==================== 参数接口（对齐后端 FilterParam / ReportParam） ====================

/** 筛选 + 排序 + 分页参数，对齐后端 {@code FilterParam} */
export interface ReportQueryBody {
  filter?: FilterCondition[]
  sort?: SortCondition
  current?: number
  size?: number
  columns?: TableItem[]
}

/** 即时执行参数，对齐后端 {@code ReportParam}，继承筛选参数 + SQL模板 */
export interface ReportExecuteBody extends ReportQueryBody {
  sqlTemplate: string
  /** 数据源 ID（仅 /execute 即时查询时有效，null=主数据源） */
  datasourceId?: number | null
}

// ==================== API ====================

export const AReport = {
  /** 报告列表 — GET /report/list */
  list: apiAxios<ReportSaveRequest[]>('/report/list', 'get'),

  /** 获取报告定义 — GET /report/get?tableKey=xxx（管理端，纯 sys_config） */
  get: (tableKey: string) =>
    apiAxios<ReportSaveRequest>('/report/get', 'get')({ tableKey }),

  /** 获取报告定义 — GET /report/user/get?tableKey=xxx（用户端，user_config 优先） */
  getUserReport: (tableKey: string) =>
    apiAxios<ReportSaveRequest>('/report/user/get', 'get')({ tableKey }),

  /** 保存报告定义 — POST /report/save（body: ReportSaveRequest） */
  save: apiAxios<string, ReportSaveRequest>('/report/save', 'post'),

  /** 删除报告 — DELETE /report/remove/{tableKey} */
  remove: (tableKey: string) =>
    apiAxios<string>(`/report/remove/${tableKey}`, 'delete')(),

  /** 执行报告查询 — POST /report/{tableKey}/query（管理端预览） */
  query: (tableKey: string, body?: ReportQueryBody) =>
    apiAxios<ReportQueryResult>(
      `/report/${tableKey}/query`,
      'post',
    )(body ?? {}),

  /** 执行报告查询 — POST /report/user/query?tableKey=xxx（用户端，tableKey 不放在 body） */
  userQuery: (tableKey: string, body?: ReportQueryBody) =>
    apiAxios<ReportQueryResult>(`/report/user/query?tableKey=${encodeURIComponent(tableKey)}`, 'post')(body ?? {}),

  /** 即时执行 SQL — POST /report/execute */
  execute: (body: ReportExecuteBody) =>
    apiAxios<ReportQueryResult>(
      `/report/execute`,
      'post',
    )(body),

  /** 导出已保存报告 Excel — POST /report/{tableKey}/export */
  export: (tableKey: string, body: ReportQueryBody) =>
    download(`/report/${tableKey}/export`, 'post')(body),

  /** 导出即时 SQL 结果 Excel — POST /report/execute/export */
  executeExport: (body: ReportExecuteBody) =>
    download(`/report/execute/export`, 'post')(body),
}
