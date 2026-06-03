/**
 * 报告管理 API — 对应 ReportController 接口
 */
import { apiAxios } from './src/requests'
import type { ResApi } from './src/requests'
import type { ReportDefinition, ReportQueryResult, ParamDef } from '@/types/report'

// ==================== API ====================

export const AReport = {
  /** 报告列表 — GET /report/list */
  list: apiAxios<ReportDefinition[]>('/report/list', 'get'),

  /** 获取报告定义 — GET /report/{reportId} */
  get: (reportId: string) =>
    apiAxios<ReportDefinition>(`/report/${reportId}`, 'get')(),

  /** 保存报告定义 — POST /report/save */
  save: apiAxios<string>('/report/save', 'post'),

  /** 删除报告 — DELETE /report/remove/{reportId} */
  remove: (reportId: string) =>
    apiAxios<string>(`/report/remove/${reportId}`, 'delete')(),

  /** 执行报告查询 — POST /report/{reportId}/query，所有参数在 body 中 */
  query: (
    reportId: string,
    params: Record<string, any>,
    current: number = 1,
    size: number = 20,
  ) =>
    apiAxios<ReportQueryResult>(
      `/report/${reportId}/query`,
      'post',
    )({ params, current, size }),

  /** 即时执行 SQL（不保存报告）— POST /report/execute，所有参数在 body 中 */
  execute: (
    sqlTemplate: string,
    parameters: ParamDef[],
    params: Record<string, any>,
    current: number = 1,
    size: number = 20,
  ) =>
    apiAxios<ReportQueryResult>(
      `/report/execute`,
      'post',
    )({ sqlTemplate, parameters, params, current, size }),
}
