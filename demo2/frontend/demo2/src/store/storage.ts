/**
 * 通用 localStorage 缓存工具
 *
 * 提供带 schema 版本校验 + 写入时自选过期时间的读写删能力，所有 Store 模块共用。
 * 缓存值必须包含 schemaVersion 字段，版本不匹配自动清除。
 * 写入时可选传入 maxAgeMs，过期时间随数据一同存储，读取/清理时自动判定。
 */

export const SCHEMA_VERSION = 2

/** 默认过期时间 30 天 */
export const DEFAULT_MAX_AGE_MS = 30 * 24 * 60 * 60 * 1000

/** 写入时自动注入的过期字段（内部使用，业务层不需要关心） */
const EXP_KEY = '__exp' as const

/** 写入 localStorage，可选 maxAgeMs 为当前这条缓存单独设定过期时长 */
export function writeCache<T extends Record<string, any>>(key: string, value: T, maxAgeMs?: number): void {
  const entry = { ...value }
  if (maxAgeMs !== undefined) {
    ;(entry as any)[EXP_KEY] = Date.now() + maxAgeMs
  }
  localStorage.setItem(key, JSON.stringify(entry))
}

/** 从 localStorage 读取并校验 schema 版本 + 自带的过期时间（写入时设定） */
export function readCache<T extends { schemaVersion: number }>(key: string): T | null {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return null
    const entry = JSON.parse(raw)
    if (entry.schemaVersion !== SCHEMA_VERSION) {
      localStorage.removeItem(key)
      return null
    }
    const exp = entry[EXP_KEY]
    if (typeof exp === 'number' && Date.now() > exp) {
      localStorage.removeItem(key)
      return null
    }
    return entry as T
  } catch { return null }
}

/** 删除 localStorage 缓存 */
export function removeCache(key: string): void {
  localStorage.removeItem(key)
}

/**
 * 主动清理指定前缀下的过期/版本不匹配缓存。
 * 过期判定依赖写入时存储在条目内的 __exp 字段，无需额外传入参数。
 * @returns 删除的 key 数量
 */
export function cleanExpired(prefix: string): number {
  let removed = 0
  let i = 0
  while (i < localStorage.length) {
    const rawKey = localStorage.key(i)
    if (rawKey && rawKey.startsWith(prefix)) {
      if (!readCache<{ schemaVersion: number }>(rawKey)) {
        removed++
        continue
      }
    }
    i++
  }
  return removed
}

/** 获取指定前缀的所有 localStorage key（不含前缀本身） */
export function getKeysByPrefix(prefix: string): string[] {
  const keys: string[] = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key?.startsWith(prefix)) {
      keys.push(key.slice(prefix.length))
    }
  }
  return keys
}
