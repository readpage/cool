/**
 * 配置管理 API — 对应 ConfigController 接口
 * 
 * 拆分后：sys_config（系统默认） + user_config（用户偏好）
 */

import { apiAxios } from './src/requests'

// ==================== 后端实体类型 ====================

/** 系统默认配置实体 — 对应 sys_config 表 */
export interface SysConfigEntity {
  id?: number
  configGroup: string
  configKey: string
  configValue: string  // JSON string
  version: number
  createTime?: string
  updateTime?: string
}

/** 用户偏好配置实体 — 对应 user_config 表 */
export interface UserConfigEntity {
  id?: number
  configGroup: string
  configKey: string
  userId: number
  configValue: string  // JSON string
  deleted: number
  createTime?: string
  updateTime?: string
}

// ==================== 通用 ConfigValue 解析 ====================

/** 统一入口：从任意配置实体中解析 JSON */
export function parseConfigValue(entity: SysConfigEntity | UserConfigEntity): any {
  return JSON.parse(entity.configValue)
}

// ==================== 查询参数 ====================

export interface ConfigQuery {
  configGroup?: string
  configKey?: string
}

// ==================== API — 对齐 ConfigController ====================

export const AConfig = {
  // --- 系统默认配置 ---

  /** 保存系统默认配置 — POST /config/system/save */
  systemSave: apiAxios<SysConfigEntity>('/config/system/save', 'post'),

  /** 查询系统默认配置 — GET /config/user/system */
  system: apiAxios<SysConfigEntity>('/config/user/system', 'get'),

  /** 列出分组内所有系统默认配置 — GET /config/system/list */
  systemList: apiAxios<SysConfigEntity[]>('/config/system/list', 'get'),

  // --- 用户偏好配置 ---

  /** 保存用户偏好配置 — POST /config/user/save */
  save: apiAxios<UserConfigEntity>('/config/user/save', 'post'),

  /** 查询当前用户配置 — GET /config/user/my */
  my: apiAxios<UserConfigEntity>('/config/user/my', 'get'),

  /** 列出当前用户分组内所有偏好配置 — GET /config/user/list */
  userList: apiAxios<UserConfigEntity[]>('/config/user/list', 'get'),
}
