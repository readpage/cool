/**
 * 配置管理 API — 基于实体 CRUD（仅接口层）
 */
import { apiAxios } from './src/requests'

// ==================== 后端实体类型 ====================

export interface SysConfigEntity {
  id?: number
  configGroup: string
  configKey: string
  userId: number
  configValue: string  // JSON string
  version: number
  deleted: number
  createTime?: string
  updateTime?: string
}

// ==================== CRUD API ====================

/** 查询配置列表（按 configGroup / configKey 筛选） */
export const listConfig = apiAxios<SysConfigEntity[]>('/config/list')

/** 保存配置（id=null 新增，id≠null 修改），返回实体含自增ID */
export const saveConfig = apiAxios<SysConfigEntity>('/config/save', 'post')

/** 删除配置 */
export const removeConfig = apiAxios<boolean>('/config/remove', 'delete')
