/**
 * Table Store — 表格模块初始化
 *
 * init() 统一调度：
 *   恢复表格配置缓存  → configStore 从 localStorage 恢复表格配置
 *   恢复选项缓存      → optionsStore 从 localStorage 恢复 select/remote-select 选项
 *   pullConfig()     → 分别请求系统配置 + 用户配置，合并后覆盖本地
 *
 *   查询优先级：用户配置 > 系统默认 > 代码兜底 initConfig()
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { AConfig } from '@/api/config'
import { useTableConfigStore, fromEntity } from './table-config'
import { useOptionsStore } from './options'

const DEFAULT_GROUP = 'table'

export const useTableStore = defineStore('table', () => {
  const tableSchemes = ref<string[]>([])

  // ==================== 拉取远端配置 ====================

  /** 分别请求系统配置 + 用户配置 → 合并后覆盖本地 */
  async function pullConfig() {
    const configStore = useTableConfigStore()

    try {
      const merged: Record<string, ReturnType<typeof fromEntity>> = {}
      const mergedSvs: Record<string, number> = {}

      // 第1层：系统默认配置（sys_config 表）
      try {
        const { data: sysEntities } = await AConfig.systemList({ configGroup: DEFAULT_GROUP })
        for (const e of sysEntities) {
          merged[e.configKey] = fromEntity(e)
          mergedSvs[e.configKey] = e.version ?? 0
        }
      } catch {
        console.warn('[TableStore] 系统配置加载失败')
      }

      // 第2层：用户偏好配置（user_config 表，覆盖同 key 的系统配置）
      try {
        const { data: userEntities } = await AConfig.userList({ configGroup: DEFAULT_GROUP })
        for (const e of userEntities) {
          merged[e.configKey] = fromEntity(e)
          mergedSvs[e.configKey] = 0
        }
      } catch {
        console.warn('[TableStore] 用户配置加载失败')
      }

      configStore.batchApply(DEFAULT_GROUP, merged, mergedSvs)
      tableSchemes.value = Object.keys(merged)
    } catch {
      console.warn('[TableStore] 后端配置加载失败，使用本地缓存')
    }
  }

  // ==================== 统一入口 ====================

  async function init() {
    // 1) 恢复本地缓存 → 页面秒开
    useTableConfigStore().restoreFromLocalStorage(DEFAULT_GROUP)   // 表格配置
    useOptionsStore().init()                                   // 下拉选项
    // 2) 拉取远端最新配置 → 后台覆盖
    await pullConfig()
  }

  return { tableSchemes, init }
})
