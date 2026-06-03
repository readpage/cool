/**
 * 报告管理 API — 对应 ReportController 接口
 */
import { apiAxios } from './src/requests'
import type { ResApi } from './src/requests'
import type { ReportSaveRequest, ReportQueryResult, FilterCondition, SortCondition } from '@/types/report'
import type { TableItem } from '@/types/table'

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
}

// ==================== API ====================

export const AReport = {
  /** 报告列表 — GET /report/list */
  list: apiAxios<ReportSaveRequest[]>('/report/list', 'get'),

  /** 获取报告定义 — GET /report/{tableKey} */
  get: (tableKey: string) =>
    apiAxios<ReportSaveRequest>(`/report/${tableKey}`, 'get')(),

  /** 保存报告定义 — POST /report/save（body: ReportSaveRequest） */
  save: apiAxios<string, ReportSaveRequest>('/report/save', 'post'),

  /** 删除报告 — DELETE /report/remove/{tableKey} */
  remove: (tableKey: string) =>
    apiAxios<string>(`/report/remove/${tableKey}`, 'delete')(),

  /** 执行报告查询 — POST /report/{tableKey}/query */
  query: (tableKey: string, body?: ReportQueryBody) =>
    apiAxios<ReportQueryResult>(
      `/report/${tableKey}/query`,
      'post',
    )(body ?? {}),

  /** 即时执行 SQL — POST /report/execute */
  execute: (body: ReportExecuteBody) =>
    apiAxios<ReportQueryResult>(
      `/report/execute`,
      'post',
    )(body),
}
