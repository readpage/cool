/**
 * 报告配置 Pinia Store — 报告定义存于 sys_config 表（configGroup='report'）
 *
 * 简化版：报告定义由设计器通过 ReportController 保存，此 Store 仅管理报告元数据缓存
 */
import { defineStore } from 'pinia'
import type { ReportSaveRequest } from '@/views/report/types/report'

export const useReportStore = defineStore('report', () => {
  /** 当前编辑/查看的报告定义 */
  let currentReport: ReportSaveRequest | null = null

  /** 设置当前报告 */
  function setCurrent(report: ReportSaveRequest | null) {
    currentReport = report
  }

  /** 获取当前报告 */
  function getCurrent(): ReportSaveRequest | null {
    return currentReport
  }

  return { currentReport, setCurrent, getCurrent }
})
