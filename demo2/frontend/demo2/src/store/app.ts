/**
 * App Store — 应用级初始化
 *
 * init() 三步：
 *   1. 从 localStorage 恢复缓存 → 页面即时渲染
 *   2. 请求 GET /config/list?configGroup=table → 后端返回系统默认(userId=0)+用户配置(userId=1)
 *   3. 合并（用户覆盖系统）→ 统一写入内存 + localStorage
 *
 *   最终查询优先级：用户配置 > 系统默认 > 代码兜底 initConfig()
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { list, fromEntity } from '@/api/config'
import { useConfigStore } from './config'

const DEFAULT_GROUP = 'table'

export const useAppStore = defineStore('app', () => {
  const initDone = ref(false)
  const tableSchemes = ref<string[]>([])

  /** 应用初始化：从 localStorage + 后端预加载所有表格配置 */
  async function init() {
    const configStore = useConfigStore()

    // 第一步：从 localStorage 恢复缓存（即时渲染）
    configStore.restoreFromLocalStorage(DEFAULT_GROUP)

    // 第二步：从后端获取最新配置
    try {
      const entities = await list({ configGroup: DEFAULT_GROUP })

      // 合并：先铺系统默认，再铺用户配置（用户覆盖同名 key）
      const merged: Record<string, ReturnType<typeof fromEntity>> = {}
      const mergedIds: Record<string, number> = {}
      const mergedSvs: Record<string, number> = {}

      for (const e of entities.filter(e => e.userId === 0)) {
        merged[e.configKey] = fromEntity(e)
        mergedSvs[e.configKey] = e.version ?? 0
      }

      for (const e of entities.filter(e => e.userId !== 0)) {
        merged[e.configKey] = fromEntity(e)
        if (e.id) mergedIds[e.configKey] = e.id
        mergedSvs[e.configKey] = 0
      }

      // 统一写入内存 + localStorage（不再区分来源）
      configStore.batchApply(DEFAULT_GROUP, merged, mergedIds, mergedSvs)

      tableSchemes.value = Object.keys(merged)
    } catch {
      console.warn('[AppStore] 后端配置加载失败，使用本地缓存')
    }

    initDone.value = true
  }

  return { initDone, tableSchemes, init }
})
