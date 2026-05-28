/**
 * 配置 Pinia Store — 基于实体 CRUD
 *
 * 后端 API：
 *   GET    /config/list?configGroup&configKey
 *   POST   /config/save
 *   DELETE /config/delete/{id}
 *
 * 配置来源优先级：用户配置 > 系统默认 > 代码兜底 initConfig()
 * 全部配置统一写入 localStorage 缓存，初始化由 appStore.init() 统一完成
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { TableConfig } from '@/types/table'
import { list, save as apiSave, remove, toEntity, fromEntity } from '@/api/config'
import type { SysConfigEntity } from '@/api/config'

// ==================== 常量 ====================

const SCHEMA_VERSION = 2
const DEFAULT_GROUP = 'table'
const MOCK_USER_ID = 1

// ==================== localStorage 缓存 ====================

interface CacheEntry {
  config: TableConfig
  ts: number
  schemaVersion: number
  serverVersion: number // 后端 SysConfig.version，用于版本比对
}

function cacheKey(group: string, key: string): string {
  // 防御：防止对象被当成 key 传入（如 Import 遮蔽导致递归）
  if (typeof key !== 'string') {
    console.warn('[ConfigStore] cacheKey 收到了非字符串 key:', key)
    return ''
  }
  return `cfg_${group}_${key}`
}

function lsSet(key: string, value: string): void {
  if (!key) return
  localStorage.setItem(key, value)
}

function lsRemove(key: string): void {
  if (!key) return
  localStorage.removeItem(key)
}

function lsGet(key: string): string | null {
  if (!key) return null
  return localStorage.getItem(key)
}

function readCache(group: string, key: string): CacheEntry | null {
  try {
    const raw = lsGet(cacheKey(group, key))
    if (!raw) return null
    const entry: CacheEntry = JSON.parse(raw)
    if (entry.schemaVersion !== SCHEMA_VERSION) {
      lsRemove(cacheKey(group, key))
      return null
    }
    return entry
  } catch { return null }
}

function writeCache(group: string, key: string, config: TableConfig, serverVersion: number): void {
  const entry: CacheEntry = { config, schemaVersion: SCHEMA_VERSION, ts: Date.now(), serverVersion }
  lsSet(cacheKey(group, key), JSON.stringify(entry))
}

function removeCache(group: string, key: string): void {
  lsRemove(cacheKey(group, key))
}

// ==================== Store ====================

export const useConfigStore = defineStore('config', () => {
  const configs = ref<Record<string, TableConfig>>({})
  const entityIds = ref<Record<string, number>>({})       // tableId → entity.id
  const serverVersions = ref<Record<string, number>>({})   // tableId → 后端版本号

  function getConfig(tableId: string): TableConfig | null {
    return configs.value[tableId] ?? null
  }

  // ==================== 批量初始化方法（由 appStore.init 调用） ====================

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
        const entry = readCache(group, tableId)
        if (entry) {
          configs.value[tableId] = entry.config
          serverVersions.value[tableId] = entry.serverVersion
        }
      }
    }
    // 批量删除脏 key
    for (const k of keysToRemove) {
      localStorage.removeItem(k)
      console.warn('[ConfigStore] 已清理无效缓存:', k)
    }
  }

  /** 获取缓存的 serverVersion（用于版本比对） */
  function getServerVersion(tableId: string): number {
    return serverVersions.value[tableId] ?? 0
  }

  /** 批量注入后端配置，同时写入 localStorage 缓存（用于用户配置） */
  function batchApply(
    group: string,
    merged: Record<string, TableConfig>,
    ids: Record<string, number>,
    svs: Record<string, number>,
  ) {
    for (const [key, cfg] of Object.entries(merged)) {
      configs.value[key] = cfg
      if (ids[key]) entityIds.value[key] = ids[key]
      if (svs[key] !== undefined) serverVersions.value[key] = svs[key]
      writeCache(group, key, cfg, svs[key] ?? 0)
    }
  }

  // ==================== 防抖保存 ====================

  const saveTimers = new Map<string, ReturnType<typeof setTimeout>>()

  async function save(tableId: string, config: TableConfig) {
    configs.value[tableId] = config
    writeCache(DEFAULT_GROUP, tableId, config, serverVersions.value[tableId] ?? 0)

    const existingTimer = saveTimers.get(tableId)
    if (existingTimer) clearTimeout(existingTimer)

    saveTimers.set(tableId, setTimeout(async () => {
      saveTimers.delete(tableId)
      try {
        const id = entityIds.value[tableId]
        const entity = toEntity(DEFAULT_GROUP, tableId, MOCK_USER_ID, config, id)
        const saved = await apiSave(entity)
        // 记录服务端返回的 ID
        if (saved.id) {
          entityIds.value[tableId] = saved.id
        }
        // 同步服务端版本号
        if (saved.version !== undefined) {
          serverVersions.value[tableId] = saved.version
          writeCache(DEFAULT_GROUP, tableId, config, saved.version)
        }
      } catch (err: any) {
        if (err.name !== 'SilentError') {
          console.warn(`[ConfigStore] 远端保存失败: ${tableId}`)
        }
      }
    }, 800))
  }

  /** 管理员保存为系统默认 */
  async function saveAsSystem(tableId: string, config: TableConfig) {
    try {
      // 查已有系统默认（后端同时返回 userId=0 和 userId=1，前端过滤）
      const entities = await list({ configGroup: DEFAULT_GROUP, configKey: tableId })
      const systemEntity = entities.find(e => e.userId === 0)
      const id = systemEntity?.id
      const entity = toEntity(DEFAULT_GROUP, tableId, 0, config, id)
      const saved = await apiSave(entity)

      // 更新内存（系统默认不缓存到 localStorage）
      configs.value[tableId] = config
      if (saved.version !== undefined) {
        serverVersions.value[tableId] = saved.version
      }
    } catch (err: any) {
      if (err.name !== 'SilentError') {
        console.warn(`[ConfigStore] 系统保存失败: ${tableId}`, err.message)
      }
    }
  }

  /** 恢复系统默认（删用户配置，回退到系统默认或代码兜底） */
  async function resetToSystem(tableId: string, codeDefault: TableConfig) {
    try {
      const id = entityIds.value[tableId]
      if (id) {
        await remove(id)
        delete entityIds.value[tableId]
      }
      removeCache(DEFAULT_GROUP, tableId)
      delete serverVersions.value[tableId]

      // 重新查系统默认（后端同时返回 userId=0 和 userId=1，前端过滤）
      const entities = await list({ configGroup: DEFAULT_GROUP, configKey: tableId })
      const systemEntity = entities.find(e => e.userId === 0)
      if (systemEntity) {
        configs.value[tableId] = fromEntity(systemEntity)
        serverVersions.value[tableId] = systemEntity.version ?? 0
      } else {
        configs.value[tableId] = codeDefault
      }
    } catch (err: any) {
      if (err.name !== 'SilentError') {
        console.warn(`[ConfigStore] 恢复默认失败: ${tableId}`, err.message)
      }
      configs.value[tableId] = codeDefault
    }
  }

  return {
    configs, entityIds, serverVersions,
    getConfig, save, saveAsSystem, resetToSystem,
    restoreFromLocalStorage, getServerVersion, batchApply,
  }
})
