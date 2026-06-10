/**
 * App Store — 应用级初始化
 *
 * init() 统一调度各子模块初始化。
 * 新增子模块：在 init() 中按序调用即可。
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useTableConfigStore } from './table-config'
import { useOptionsStore } from './options'

export const useAppStore = defineStore('app', () => {
  const initDone = ref(false)

  async function init() {
    useTableConfigStore().restoreFromLocalStorage()  // 表格配置缓存
    useOptionsStore().init()                         // 下拉选项缓存
    initDone.value = true
  }

  return { initDone, init }
})
