/**
 * 配置管理 API — 基于实体 CRUD
 */
import type { TableConfig } from '@/types/table'

const BASE = '/api/config'

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

// ==================== 通用 fetch ====================

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  let res: Response
  try {
    res = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    })
  } catch {
    throw new SilentError('后端未连接')
  }
  if (!res.ok) throw new SilentError('后端未连接')
  const ct = res.headers.get('content-type') || ''
  if (!ct.includes('application/json')) throw new SilentError('后端响应异常')
  const json = await res.json()
  if (json.code !== 200) throw new Error(json.msg || '请求失败')
  return json.data as T
}

class SilentError extends Error {
  constructor(msg: string) {
    super(msg)
    this.name = 'SilentError'
  }
}

// ==================== CRUD API ====================

/** 查询配置列表（按 configGroup / configKey 筛选，后端自动返回系统默认+当前用户配置） */
export async function list(params: {
  configGroup?: string
  configKey?: string
}): Promise<SysConfigEntity[]> {
  const qs = Object.entries(params)
    .filter(([, v]) => v !== undefined)
    .map(([k, v]) => `${k}=${encodeURIComponent(v!)}`)
    .join('&')
  return request<SysConfigEntity[]>(`${BASE}/list${qs ? '?' + qs : ''}`)
}

/** 保存配置（id=null 新增，id≠null 修改），返回实体含自增ID */
export async function save(config: SysConfigEntity): Promise<SysConfigEntity> {
  return request<SysConfigEntity>(`${BASE}/save`, {
    method: 'POST',
    body: JSON.stringify(config),
  })
}

/** 删除配置 */
export async function remove(id: number): Promise<boolean> {
  return request<boolean>(`${BASE}/delete/${id}`, { method: 'DELETE' })
}

// ==================== 序列化工具 ====================

export function toEntity(
  group: string,
  key: string,
  userId: number,
  config: TableConfig,
  id?: number,
): SysConfigEntity {
  return {
    ...(id ? { id } : {}),
    configGroup: group,
    configKey: key,
    userId,
    configValue: JSON.stringify(config),
    version: 0,
    deleted: 0,
  } as SysConfigEntity
}

export function fromEntity(entity: SysConfigEntity): TableConfig {
  return JSON.parse(entity.configValue) as TableConfig
}
