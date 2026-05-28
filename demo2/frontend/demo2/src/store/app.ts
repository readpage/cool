/**
 * App Store — 应用级初始化
 *
 * init() 统一调度各子模块初始化。
 * 新增子模块：在 init() 中按序调用即可。
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useTableStore } from './table'

export const useAppStore = defineStore('app', () => {
  const initDone = ref(false)

  async function init() {
    await useTableStore().init()
    initDone.value = true
  }

  return { initDone, init }
})
