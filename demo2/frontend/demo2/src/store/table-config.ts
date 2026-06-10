/**
 * 表格配置 Pinia Store — 拆分为 sys_config（系统默认） + user_config（用户偏好）
 *
 * 配置来源优先级：用户配置 > 系统默认 > 代码兜底 initConfig()
 * 系统配置走 /config/system/*，用户配置走 /config/user/*
 *
 * 数据流：
 *   localStorage ──restoreFromLocalStorage──→ reactive Map ←──computed── 组件
 *                                                      ↑
 *                                                fetchRemote
 *                                                  (后台)
 */
import { computed, reactive } from 'vue'
import { defineStore, storeToRefs } from 'pinia'
import type { TableConfig } from '@/components/table/index.vue'
import type { SysConfigEntity, UserConfigEntity, ConfigResult } from '@/api/config'
import { AConfig } from '@/api/config'
import { readCache, writeCache, removeCache, SCHEMA_VERSION, DEFAULT_MAX_AGE_MS } from './storage'
import { useUserStore } from '@/store/user'

// ==================== 序列化工具 ====================

/** 构建用户偏好配置实体 */
function toUserEntity(
  group: string,
  key: string,
  userId: number,
  config: TableConfig,
): UserConfigEntity {
  return {
    configGroup: group,
    configKey: key,
    userId,
    configValue: JSON.stringify(config),
    deleted: 0,
  } as UserConfigEntity
}

/** 构建系统默认配置实体 */
function toSystemEntity(
  group: string,
  key: string,
  config: TableConfig,
): SysConfigEntity {
  return {
    configGroup: group,
    configKey: key,
    configValue: JSON.stringify(config),
    version: 0,
  } as SysConfigEntity
}

/** 通用解析：从配置实体的 configValue JSON 中还原 TableConfig */
export function fromEntity(entity: SysConfigEntity | UserConfigEntity): TableConfig {
  return JSON.parse(entity.configValue) as TableConfig
}

// ==================== 常量 ====================

const DEFAULT_GROUP = 'table'
const MOCK_USER_ID = 1

// ==================== localStorage 缓存 ====================

interface ConfigCacheValue {
  schemaVersion: number
  config: TableConfig
  serverVersion: number
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

function writeConfigCache(group: string, key: string, config: TableConfig, serverVersion: number, maxAgeMs: number = DEFAULT_MAX_AGE_MS): void {
  const ck = configKey(group, key)
  if (!ck) return
  writeCache(ck, { schemaVersion: SCHEMA_VERSION, config, serverVersion }, maxAgeMs)
}

function removeConfigCache(group: string, key: string): void {
  const ck = configKey(group, key)
  if (ck) removeCache(ck)
}

// ==================== Store ====================

export const useTableConfigStore = defineStore('table-config', () => {
  // reactive Map — computed 能自动追踪 .get() 依赖
  const configs = reactive(new Map<string, TableConfig>())
  const serverVersions = new Map<string, number>()

  // 用户角色权限
  const userStore = useUserStore()
  const { info } = storeToRefs(userStore)

  /** 当前用户是否为管理员 (auths 包含 root 或 admin) */
  const isAdmin = computed(() => {
    const auths = info.value?.auths
    return !!(auths && (auths.includes('root') || auths.includes('admin')))
  })

  // 防止同一 tableId 并发重复请求
  const pending = new Map<string, Promise<void>>()

  // 已触发过远端拉取的 ID（防止 computed 反复调用 fetchRemote 导致死循环）
  const fetched = new Set<string>()

  // ==================== 内部：后台拉取（fire & forget） ====================

  async function fetchRemote(tableId: string): Promise<void> {
    if (pending.has(tableId)) return pending.get(tableId)!

    const promise = (async () => {
      try {
        // 只获取用户自己的配置（user_config），不再走 copy-on-read 合并逻辑
        // 版本更新通知：后续可通过 AConfig.checkVersion() 周期性检查 sys_config 版本，引导用户调用 reset
        const { data: result } = await AConfig.my({ configGroup: DEFAULT_GROUP, configKey: tableId })
        if (result && result.configValue) {
          const config = JSON.parse(result.configValue) as TableConfig
          configs.set(tableId, config)
          serverVersions.set(tableId, 0) // user_config 无版本号
          writeConfigCache(DEFAULT_GROUP, tableId, config, 0)
        }
      } catch { /* 远端失败，保持本地缓存不动 */ }
    })()

    pending.set(tableId, promise)
    try {
      await promise
    } finally {
      pending.delete(tableId)
    }
  }

  // ==================== 公开 API ====================

  /** 同步返回缓存（如有），同时 fire-and-forget 远端拉取最新配置 */
  function getConfig(tableId: string): TableConfig | null {
    if (!fetched.has(tableId)) {
      fetched.add(tableId)
      fetchRemote(tableId)
    }
    return configs.get(tableId) ?? null
  }

  /** 显式加载远端配置：拉取并写入 reactive Map，返回最终配置 */
  async function loadConfig(tableId: string): Promise<TableConfig | null> {
    await fetchRemote(tableId)
    return configs.get(tableId) ?? null
  }

  // ==================== 初始化 ====================

  /** 从 localStorage 恢复所有缓存配置（readCache 已内置过期校验，过期时间由写入时设定） */
  function restoreFromLocalStorage(group: string = DEFAULT_GROUP) {
    const prefix = `cfg_${group}_`
    let restoredCount = 0
    const keysToRemove: string[] = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && key.startsWith(prefix)) {
        const tableId = key.slice(prefix.length)
        if (!tableId || tableId === '[object Object]' || tableId.includes('[object')) {
          keysToRemove.push(key)
          continue
        }
        const entry = readConfigCache(group, tableId)
        if (entry) {
          console.log(`[Store.restore] tableId=${tableId}, search.filter:`, (entry.config as any).search?.filter?.map((c: any) => ({ prop: c.prop, value: c.value })))
          configs.set(tableId, entry.config)
          serverVersions.set(tableId, entry.serverVersion)
          restoredCount++
        }
      }
    }
    for (const k of keysToRemove) {
      localStorage.removeItem(k)
    }
    console.log(`[TableConfig] restoreFromLocalStorage: ${restoredCount} 条缓存恢复，${keysToRemove.length} 条脏数据清除`)
  }

  // ==================== 仅写本地（不发请求） ====================

  /** 仅写入本地缓存（Map + localStorage），不触发远程保存。用于加载/回显场景建立基线。 */
  function setLocal(tableId: string, config: TableConfig, group: string = DEFAULT_GROUP) {
    configs.set(tableId, config)
    writeConfigCache(group, tableId, config, serverVersions.get(tableId) ?? 0)
  }

  // ==================== 防抖保存 ====================

  const saveTimers = new Map<string, ReturnType<typeof setTimeout>>()

  /** 保存当前用户的表格配置（写入 user_config 表） */
  async function save(tableId: string, config: TableConfig, group: string = DEFAULT_GROUP) {
    console.log('[Store.save] 入参 tableId:', tableId, 'config.search?.filter:', (config as any).search?.filter?.map((c: any) => ({ prop: c.prop, value: c.value, operator: c.operator })))
    configs.set(tableId, config)
    writeConfigCache(group, tableId, config, serverVersions.get(tableId) ?? 0)
    // 验证写入 localStorage 的内容
    const verifed = readConfigCache(group, tableId)
    console.log('[Store.save] localStorage 回读验证:', (verifed as any)?.config?.search?.filter?.map((c: any) => ({ prop: c.prop, value: c.value })))

    const timerKey = `${group}::${tableId}`
    const existingTimer = saveTimers.get(timerKey)
    if (existingTimer) clearTimeout(existingTimer)

    saveTimers.set(timerKey, setTimeout(async () => {
      saveTimers.delete(timerKey)
      try {
        const entity = toUserEntity(group, tableId, MOCK_USER_ID, config)
        await AConfig.save(entity)
        serverVersions.set(tableId, 0)
        writeConfigCache(group, tableId, config, 0)
      } catch {
        console.warn(`[TableConfigStore] 远端保存失败: ${tableId}`)
      }
    }, 800))
  }

  // ==================== 管理员操作 ====================

  /** 管理员保存为系统默认（写入 sys_config 表） */
  async function saveAsSystem(tableId: string, config: TableConfig, group: string = DEFAULT_GROUP) {
    try {
      const entity = toSystemEntity(group, tableId, config)
      const { data: saved } = await AConfig.systemSave(entity)
      configs.set(tableId, config)
      serverVersions.set(tableId, saved.version ?? 0)
    } catch {
      console.warn(`[TableConfigStore] 系统保存失败: ${tableId}`)
    }
  }

  /** 恢复系统默认 — 调后端 POST /config/reset：sys_config 覆盖 user_config，返回系统配置 */
  async function resetToSystem(tableId: string, codeDefault: TableConfig, group: string = DEFAULT_GROUP) {
    console.log('[Store] resetToSystem 开始 →', tableId)
    try {
      const { data: result } = await AConfig.reset({ configGroup: group, configKey: tableId })
      if (result && result.configValue) {
        const cfg = JSON.parse(result.configValue) as TableConfig
        configs.set(tableId, cfg)
        serverVersions.set(tableId, result.version ?? 0)
        writeConfigCache(group, tableId, cfg, result.version ?? 0)
        console.log('[Store] 后端已用 sys_config 覆盖 user_config →', { columns: cfg.columns.length, search: !!cfg.search, source: result.source })
      } else {
        configs.set(tableId, codeDefault)
        console.log('[Store] 服务端无配置，使用代码默认 initTableConfig →', { columns: codeDefault.columns.length, search: !!codeDefault.search })
      }
    } catch {
      console.warn(`[TableConfigStore] 恢复默认失败: ${tableId}`)
      configs.set(tableId, codeDefault)
      console.log('[Store] 异常兜底，使用代码默认')
    }
  }

  // ==================== 批量导入 ====================

  /** 批量应用远端配置（由 tableStore.pullConfig 调用） */
  function batchApply(
    group: string,
    merged: Record<string, TableConfig>,
    versions: Record<string, number>,
  ) {
    for (const [key, config] of Object.entries(merged)) {
      configs.set(key, config)
      serverVersions.set(key, versions[key] ?? 0)
      writeConfigCache(group, key, config, versions[key] ?? 0)
    }
  }

  return {
    configs, serverVersions,
    getConfig, loadConfig, setLocal, save, saveAsSystem, resetToSystem,
    restoreFromLocalStorage, batchApply,
    isAdmin,
  }
})
