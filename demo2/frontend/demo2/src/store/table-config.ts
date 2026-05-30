/**
 * 表格配置 Pinia Store — 基于实体 CRUD
 *
 * 后端 API：
 *   GET    /config/list?configGroup&configKey
 *   POST   /config/save
 *   DELETE /config/delete/{id}
 *
 * 配置来源优先级：用户配置 > 系统默认 > 代码兜底 initConfig()
 * 全部配置统一写入 localStorage 缓存，初始化由 tableStore.init() 统一完成
 */
import { defineStore } from 'pinia'
import type { TableConfig } from '@/types/table'
import type { SysConfigEntity } from '@/api/config'
import { AConfig } from '@/api/config'
import { readCache, writeCache, removeCache, SCHEMA_VERSION } from './storage'

// ==================== 序列化工具（业务层） ====================

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

// ==================== 常量 ====================

const DEFAULT_GROUP = 'table'
const MOCK_USER_ID = 1

// ==================== localStorage 缓存 ====================

interface ConfigCacheValue {
  schemaVersion: number
  config: TableConfig
  ts: number
  serverVersion: number // 后端 SysConfig.version，用于版本比对
}

function configKey(group: string, key: string): string {
  if (typeof key !== 'string') {
    console.warn('[TableConfigStore] configKey 收到了非字符串 key:', key)
    return ''
  }
  return `cfg_${group}_${key}`
}

function readConfigCache(group: string, key: string): ConfigCacheValue | null {
  const ck = configKey(group, key)
  if (!ck) return null
  return readCache<ConfigCacheValue>(ck)
}

function writeConfigCache(group: string, key: string, config: TableConfig, serverVersion: number): void {
  const ck = configKey(group, key)
  if (!ck) return
  writeCache(ck, { schemaVersion: SCHEMA_VERSION, config, ts: Date.now(), serverVersion })
}

function removeConfigCache(group: string, key: string): void {
  const ck = configKey(group, key)
  if (ck) removeCache(ck)
}

// ==================== Store ====================

export const useTableConfigStore = defineStore('table-config', () => {
  // 方案 B：纯 Map（无 Vue 响应式），50+ key 时避免深度响应式开销
  const configs = new Map<string, TableConfig>()
  const entityIds = new Map<string, number>()       // tableId → entity.id
  const serverVersions = new Map<string, number>()   // tableId → 后端版本号

  function getConfig(tableId: string): TableConfig | null {
    return configs.get(tableId) ?? null
  }

  // ==================== 批量初始化方法（由 tableStore.init 调用） ====================

  /** 从 localStorage 恢复所有缓存配置（即时渲染） */
  function restoreFromLocalStorage(group: string = DEFAULT_GROUP) {
    const prefix = `cfg_${group}_`
    const keysToRemove: string[] = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.startsWith(prefix)) {
        const tableId = key.slice(prefix.length)
        // 清理无效 key（如 [object Object]）
        if (!tableId || tableId === '[object Object]' || tableId.includes('[object')) {
          keysToRemove.push(key)
          continue
        }
        const entry = readConfigCache(group, tableId)
        if (entry) {
          configs.set(tableId, entry.config)
          serverVersions.set(tableId, entry.serverVersion)
        }
      }
    }
    // 批量删除脏 key
    for (const k of keysToRemove) {
      localStorage.removeItem(k)
      console.warn('[TableConfigStore] 已清理无效缓存:', k)
    }
  }

  /** 获取缓存的 serverVersion（用于版本比对） */
  function getServerVersion(tableId: string): number {
    return serverVersions.get(tableId) ?? 0
  }

  /** 批量注入后端配置，同时写入 localStorage 缓存（用于用户配置） */
  function batchApply(
    group: string,
    merged: Record<string, TableConfig>,
    ids: Record<string, number>,
    svs: Record<string, number>,
  ) {
    for (const [key, cfg] of Object.entries(merged)) {
      configs.set(key, cfg)
      if (ids[key]) entityIds.set(key, ids[key])
      if (svs[key] !== undefined) serverVersions.set(key, svs[key])
      writeConfigCache(group, key, cfg, svs[key] ?? 0)
    }
  }

  // ==================== 防抖保存 ====================

  const saveTimers = new Map<string, ReturnType<typeof setTimeout>>()

  async function save(tableId: string, config: TableConfig) {
    configs.set(tableId, config)
    writeConfigCache(DEFAULT_GROUP, tableId, config, serverVersions.get(tableId) ?? 0)

    const existingTimer = saveTimers.get(tableId)
    if (existingTimer) clearTimeout(existingTimer)

    saveTimers.set(tableId, setTimeout(async () => {
      saveTimers.delete(tableId)
      try {
        const id = entityIds.get(tableId)
        const entity = toEntity(DEFAULT_GROUP, tableId, MOCK_USER_ID, config, id)
        const { data: saved } = await AConfig.save(entity)
        // 记录服务端返回的 ID
        if (saved.id) {
          entityIds.set(tableId, saved.id)
        }
        // 同步服务端版本号
        if (saved.version !== undefined) {
          serverVersions.set(tableId, saved.version)
          writeConfigCache(DEFAULT_GROUP, tableId, config, saved.version)
        }
      } catch {
        console.warn(`[TableConfigStore] 远端保存失败: ${tableId}`)
      }
    }, 800))
  }

  /** 管理员保存为系统默认 */
  async function saveAsSystem(tableId: string, config: TableConfig) {
    try {
      // 查已有系统默认（后端同时返回 userId=0 和 userId=1，前端过滤）
      const { data: entities } = await AConfig.list({ configGroup: DEFAULT_GROUP, configKey: tableId })
      const systemEntity = entities.find(e => e.userId === 0)
      const id = systemEntity?.id
      const entity = toEntity(DEFAULT_GROUP, tableId, 0, config, id)
      const { data: saved } = await AConfig.save(entity)

      // 更新内存（系统默认不缓存到 localStorage）
      configs.set(tableId, config)
      if (saved.version !== undefined) {
        serverVersions.set(tableId, saved.version)
      }
    } catch {
      console.warn(`[TableConfigStore] 系统保存失败: ${tableId}`)
    }
  }

  /** 恢复系统默认（删用户配置，回退到系统默认或代码兜底） */
  async function resetToSystem(tableId: string, codeDefault: TableConfig) {
    try {
      const id = entityIds.get(tableId)
      if (id) {
        await AConfig.remove(String(id))
        entityIds.delete(tableId)
      }
      removeConfigCache(DEFAULT_GROUP, tableId)
      serverVersions.delete(tableId)

      // 重新查系统默认（后端同时返回 userId=0 和 userId=1，前端过滤）
      const { data: entities } = await AConfig.list({ configGroup: DEFAULT_GROUP, configKey: tableId })
      const systemEntity = entities.find(e => e.userId === 0)
      if (systemEntity) {
        configs.set(tableId, fromEntity(systemEntity))
        serverVersions.set(tableId, systemEntity.version ?? 0)
      } else {
        configs.set(tableId, codeDefault)
      }
    } catch {
      console.warn(`[TableConfigStore] 恢复默认失败: ${tableId}`)
      configs.set(tableId, codeDefault)
    }
  }

  // ==================== 测试种子 ====================

  /** 种子写入：同时写内存 + localStorage，立即生效无需刷新 */
  function seedConfig(tableId: string, config: TableConfig) {
    configs.set(tableId, config)
    writeConfigCache(DEFAULT_GROUP, tableId, config, 0)
  }

  return {
    configs, entityIds, serverVersions,
    getConfig, save, saveAsSystem, resetToSystem,
    restoreFromLocalStorage, getServerVersion, batchApply, seedConfig,
  }
})
