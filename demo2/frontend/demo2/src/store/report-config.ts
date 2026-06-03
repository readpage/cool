/**
 * 报告配置 Pinia Store — sys_config 持久化
 *
 * 与 table-config.ts 完全相同的模式：
 *   配置来源优先级：用户配置 > 系统默认
 *   后端按 (configGroup='report', configKey=reportId, userId, deleted) UPSERT
 *
 * 简化版：报告定义由设计器保存，此 Store 仅管理报告元数据缓存
 */
import { defineStore } from 'pinia'
import type { ReportDefinition } from '@/types/report'

export const useReportStore = defineStore('report', () => {
  /** 当前编辑/查看的报告定义 */
  let currentReport: ReportDefinition | null = null

  /** 设置当前报告 */
  function setCurrent(report: ReportDefinition | null) {
    currentReport = report
  }

  /** 获取当前报告 */
  function getCurrent(): ReportDefinition | null {
    return currentReport
  }

  return { currentReport, setCurrent, getCurrent }
})
