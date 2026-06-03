/**
 * 配置管理 API — 对应 ConfigController 接口
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

// ==================== 查询参数 ====================

export interface ConfigQuery {
  configGroup?: string
  configKey?: string
}

// ==================== API — 对齐 ConfigController ====================

export const AConfig = {
  /** 保存配置 — POST /config/user/save，按业务键 (configGroup, configKey, userId, deleted) UPSERT */
  save: apiAxios<SysConfigEntity>('/config/user/save', 'post'),

  /** 查询系统默认配置（userId=0）— GET /config/user/system */
  system: apiAxios<SysConfigEntity>('/config/user/system', 'get'),

  /** 查询当前用户配置 — GET /config/user/my */
  my: apiAxios<SysConfigEntity>('/config/user/my', 'get'),
}

