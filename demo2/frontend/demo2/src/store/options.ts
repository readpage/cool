import { defineStore } from 'pinia'
import { reactive } from 'vue'
import type { OptionItem } from '@/components/table/types'

export interface Option {
  label: string
  value: string
  style?: OptionItem['style']
}

/**
 * 选项 Store — API + localStorage 缓存一体
 *
 * F5 刷新 → init() 恢复缓存 → getOptions() 优先缓存秒现 + 后台覆盖
 * 翻页/筛选  → onMounted 不触发 → 零请求
 */
export const useOptionsStore = defineStore('options', () => {
  const cache = reactive<Record<string, Option[]>>({})

  // ==================== 缓存读写 ====================

  function get(prop: string): Option[] {
    return cache[prop] ?? []
  }

  function has(prop: string): boolean {
    return (cache[prop]?.length ?? 0) > 0
  }

  function set(prop: string, options: Option[]) {
    cache[prop] = options
    localStorage.setItem(`opt_${prop}`, JSON.stringify(options))
  }

  function remove(prop: string) {
    delete cache[prop]
    localStorage.removeItem(`opt_${prop}`)
  }

  /** 从 localStorage 恢复所有缓存（appStore.init 中调用） */
  function init() {
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key?.startsWith('opt_')) {
        try {
          cache[key.slice(4)] = JSON.parse(localStorage.getItem(key)!)
        } catch {
          localStorage.removeItem(key)
        }
      }
    }
  }

  // ==================== API 调用 ====================

  /** 请求后端选项接口 */
  async function fetchApi(type: string, keyword?: string): Promise<Option[]> {
    const params = new URLSearchParams({ type, limit: keyword ? '20' : '200' })
    if (keyword) params.set('keyword', keyword)
    const res = await fetch(keyword ? `/api/option/search?${params}` : `/api/option/list?${params}`)
    const json = await res.json()
    return (json.data ?? []).map((item: any) => ({ label: item.label, value: item.value }))
  }

  // ==================== 对外接口 ====================

  /**
   * 统一选项入口：有 keyword → 远程搜索（不缓存），无 keyword → 全量加载（stale-while-revalidate）
   */
  async function getOptions(type: string, keyword?: string): Promise<Option[]> {
    // 远程搜索：直接查 API，不缓存
    if (keyword) {
      return fetchApi(type, keyword)
    }
    // 全量加载：stale-while-revalidate
    const cached = get(type)
    if (cached.length > 0) {
      fetchApi(type).then(items => { if (items.length > 0) set(type, items) })
      return cached
    }
    try {
      const items = await fetchApi(type)
      if (items.length > 0) set(type, items)
      return items
    } catch (e) {
      console.warn(`[options] getOptions 失败: ${type}`, e)
      return []
    }
  }

  return { cache, get, has, set, remove, init, getOptions }
})
