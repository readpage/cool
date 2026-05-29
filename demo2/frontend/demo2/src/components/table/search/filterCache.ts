/**
 * 独立模块级共享缓存（非 SFC 文件，绕过 Vite SFC 编译的模块隔离）
 * 所有 FilterValue 实例通过 import 此文件共享同一份缓存数据
 */
import { ref } from 'vue'
import type { OptionItem } from './types'

export type { OptionItem }

/** 纯 JS 对象存储远程选项缓存 */
export const plainOptionsCache: Record<string, OptionItem[]> = {}

/** 版本计数器：每次缓存写入自增，驱动所有 computed 重算 */
export const cacheVersion = ref(0)
