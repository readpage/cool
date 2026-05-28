/**
 * 通用 localStorage 缓存工具
 *
 * 提供带 schema 版本校验的读写删能力，所有 Store 模块共用。
 * 缓存值必须包含 schemaVersion 字段，版本不匹配自动清除。
 */

export const SCHEMA_VERSION = 2

/** 从 localStorage 读取并校验 schema 版本 */
export function readCache<T extends { schemaVersion: number }>(key: string): T | null {
  try {
    const raw = localStorage.getItem(key)
    if (!raw) return null
    const entry: T = JSON.parse(raw)
    if (entry.schemaVersion !== SCHEMA_VERSION) {
      localStorage.removeItem(key)
      return null
    }
    return entry
  } catch { return null }
}

/** 写入 localStorage */
export function writeCache<T>(key: string, value: T): void {
  localStorage.setItem(key, JSON.stringify(value))
}

/** 删除 localStorage 缓存 */
export function removeCache(key: string): void {
  localStorage.removeItem(key)
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
