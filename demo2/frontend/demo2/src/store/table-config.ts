/**
 * 表格配置 Pinia Store — sys_config 持久化
 *
 * 配置来源优先级：用户配置 > 系统默认 > 代码兜底 initConfig()
 * 后端按业务键 (configGroup, configKey, userId) UPSERT，deleted 作为状态字段，前端无需追踪 id
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
import type { SysConfigEntity } from '@/api/config'
import { AConfig } from '@/api/config'
import { readCache, writeCache, removeCache, SCHEMA_VERSION, DEFAULT_MAX_AGE_MS } from './storage'
import { useUserStore } from '@/store/user'

// ==================== 序列化工具 ====================

function toEntity(
  group: string,
  key: string,
  userId: number,
  config: TableConfig,
): SysConfigEntity {
  return {
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

  // ==================== 内部：后台拉取（fire & forget） ====================

  async function fetchRemote(tableId: string): Promise<void> {
    if (pending.has(tableId)) return pending.get(tableId)!

    const promise = (async () => {
      // 第1层：用户个人配置
      try {
        const { data: userEntity } = await AConfig.my({ configGroup: DEFAULT_GROUP, configKey: tableId })
        if (userEntity) {
          // 避免覆盖用户当前会话中已通过 save() 写入的本地配置
          if (!configs.has(tableId)) {
            const config = fromEntity(userEntity)
            configs.set(tableId, config)
            if (userEntity.version !== undefined) serverVersions.set(tableId, userEntity.version)
            writeConfigCache(DEFAULT_GROUP, tableId, config, userEntity.version ?? 0)
          }
          return
        }
      } catch { /* 继续尝试系统配置 */ }

      // 第2层：系统默认配置
      try {
        const { data: sysEntity } = await AConfig.system({ configGroup: DEFAULT_GROUP, configKey: tableId })
        if (sysEntity && !configs.has(tableId)) {
          const config = fromEntity(sysEntity)
          configs.set(tableId, config)
          if (sysEntity.version !== undefined) serverVersions.set(tableId, sysEntity.version)
          writeConfigCache(DEFAULT_GROUP, tableId, config, sysEntity.version ?? 0)
        }
      } catch { /* 两端都失败，组件走 ?? initTableConfig() */ }
    })()

    pending.set(tableId, promise)
    try {
      await promise
    } finally {
      pending.delete(tableId)
    }
  }

  // ==================== 公开 API ====================

  /** 同步读缓存 + 首次调用 fire & forget 触发后台拉取 */
  function getConfig(tableId: string): TableConfig | null {
    const cached = configs.get(tableId)
    if (!cached) {
      // 首次无缓存：fire & forget 不阻塞返回（后续 configs.set 触发响应式更新）
      fetchRemote(tableId)
    }
    return cached ?? null
  }

  // ==================== 初始化 ====================

  /** 从 localStorage 恢复所有缓存配置（readCache 已内置过期校验，过期时间由写入时设定） */
  function restoreFromLocalStorage(group: string = DEFAULT_GROUP) {
    const prefix = `cfg_${group}_`
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
          configs.set(tableId, entry.config)
          serverVersions.set(tableId, entry.serverVersion)
        }
      }
    }
    for (const k of keysToRemove) {
      localStorage.removeItem(k)
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
        const entity = toEntity(DEFAULT_GROUP, tableId, MOCK_USER_ID, config)
        const { data: saved } = await AConfig.save(entity)
        if (saved.version !== undefined) {
          serverVersions.set(tableId, saved.version)
          writeConfigCache(DEFAULT_GROUP, tableId, config, saved.version)
        }
      } catch {
        console.warn(`[TableConfigStore] 远端保存失败: ${tableId}`)
      }
    }, 800))
  }

  // ==================== 管理员操作 ====================

  /** 管理员保存为系统默认 */
  async function saveAsSystem(tableId: string, config: TableConfig) {
    try {
      const entity = toEntity(DEFAULT_GROUP, tableId, 0, config)
      const { data: saved } = await AConfig.save(entity)
      configs.set(tableId, config)
      if (saved.version !== undefined) {
        serverVersions.set(tableId, saved.version)
      }
    } catch {
      console.warn(`[TableConfigStore] 系统保存失败: ${tableId}`)
    }
  }

  /** 恢复系统默认（清除本地缓存 → 拉取系统默认 → 覆盖写入用户服务端槽位） */
  async function resetToSystem(tableId: string, codeDefault: TableConfig) {
    console.log('[Store] resetToSystem 开始 →', tableId)
    try {
      removeConfigCache(DEFAULT_GROUP, tableId)
      serverVersions.delete(tableId)
      console.log('[Store] 已清除本地缓存')

      const { data: systemEntity } = await AConfig.system({ configGroup: DEFAULT_GROUP, configKey: tableId })
      if (systemEntity) {
        const cfg = fromEntity(systemEntity)
        configs.set(tableId, cfg)
        serverVersions.set(tableId, systemEntity.version ?? 0)
        console.log('[Store] 使用服务端系统默认配置 →', { columns: cfg.columns.length, search: !!cfg.search })
        // 用系统配置覆盖用户服务端槽位，确保后续 my() 读到的是重置后的值
        const entity = toEntity(DEFAULT_GROUP, tableId, MOCK_USER_ID, cfg)
        entity.deleted = 0
        await AConfig.save(entity)
        console.log('[Store] 已覆盖用户服务端配置')
      } else {
        configs.set(tableId, codeDefault)
        console.log('[Store] 服务端无配置，使用代码默认 initTableConfig →', { columns: codeDefault.columns.length, search: !!codeDefault.search })
        // 同样将代码默认写入用户槽位
        const entity = toEntity(DEFAULT_GROUP, tableId, MOCK_USER_ID, codeDefault)
        entity.deleted = 0
        await AConfig.save(entity)
        console.log('[Store] 已用代码默认覆盖用户服务端配置')
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
    _ids: Record<string, number>,
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
    getConfig, save, saveAsSystem, resetToSystem,
    restoreFromLocalStorage, batchApply,
    isAdmin,
  }
})
