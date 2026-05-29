<template>
  <!-- 日期 -->
  <el-date-picker
    v-if="fieldType === 'date' || fieldType === 'datetime'"
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    :type="dateType"
    :format="dateFormat"
    :value-format="dateFormat"
    :placeholder="placeholder"
    class="cond-value"
    :teleported="false"
  />

  <!-- 静态下拉 -->
  <el-select
    v-else-if="fieldType === 'select'"
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    :multiple="operator === 'in'"
    :placeholder="placeholder"
    class="cond-value"
    :teleported="false"
    clearable
    value-key="value"
  >
      <el-option
              v-for="opt in safeStaticOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
  </el-select>

  <!-- 远程搜索下拉 -->
  <el-select
    v-else-if="fieldType === 'remote-select'"
    v-model="remoteModelValue"
    :multiple="operator === 'in'"
    :placeholder="placeholder"
    class="cond-value"
    :teleported="false"
    clearable
    filterable
    remote
    :remote-method="onRemoteSearch"
    :loading="remoteLoading"
    value-key="value"
  >
    <el-option
      v-for="opt in safeRemoteOptions"
      :key="opt.value"
      :label="opt.label"
      :value="opt.value"
    />
  </el-select>

  <!-- 默认文本 -->
  <el-input
    v-else
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    :placeholder="placeholder"
    class="cond-value"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { plainOptionsCache, cacheVersion, type OptionItem } from './filterCache'

/* ============ 模块级共享缓存（同一列的远程选项在所有 FilterValue 实例间共享） ============ */
/* 缓存数据定义在 filterCache.ts 中，确保跨 SFC 模块共享 */

/* ============ Props & Emits ============ */

const props = defineProps<{
  modelValue: string | string[]
  /** 控件类型 */
  fieldType?: 'text' | 'date' | 'datetime' | 'daterange' | 'datetimerange' | 'select' | 'remote-select'
  /** 当前操作符（影响 select 是否多选） */
  operator?: 'contains' | 'eq' | 'ne' | 'gt' | 'lt' | 'gte' | 'lte' | 'between' | 'in'
  placeholder?: string
  /** 静态选项（fieldType='select' 时使用） */
  options?: (OptionItem | string)[]
  /** 远程搜索方法（fieldType='remote-select' 时使用） */
  remoteMethod?: (query: string) => Promise<OptionItem[]>
  /** 列名，用于共享远程搜索缓存 */
  column?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | string[]): void
}>()

/* ============ 日期 ============ */

const dateType = computed(() => (props.fieldType === 'datetime' ? 'datetime' : 'date'))
const dateFormat = computed(() => (props.fieldType === 'datetime' ? 'YYYY-MM-DD HH:mm:ss' : 'YYYY-MM-DD'))

/* ============ 下拉选项标准化 ============ */

const normalizedOptions = computed<OptionItem[]>(() => {
  if (!props.options) return []
  return props.options.map((opt) =>
    typeof opt === 'string' ? { label: opt, value: opt } : opt,
  )
})

/** 确保当前 modelValue 始终在选项中，防止另一个组件清除后此组件显示裸 value */
const safeStaticOptions = computed<OptionItem[]>(() => {
  const opts = normalizedOptions.value
  const val = props.modelValue
  if (!val || typeof val !== 'string' || opts.some((o) => o.value === val)) return opts
  return [...opts, { label: val, value: val }]
})

/* ============ 远程搜索 ============ */

const remoteOptions = ref<OptionItem[]>([])
const remoteLoading = ref(false)

/** 合并共享缓存 + 本地结果 + 当前值，确保 label 始终可解析 */
const safeRemoteOptions = computed<OptionItem[]>(() => {
  // 触摸版本号以建立响应式依赖
  // eslint-disable-next-line @typescript-eslint/no-unused-expressions
  cacheVersion.value

  // 用 Map 实现 O(1) 去重
  const seen = new Map<string, OptionItem>()
  const cached = props.column ? (plainOptionsCache[props.column] ?? []) : []
  for (const opt of cached) seen.set(opt.value, opt)
  for (const opt of remoteOptions.value) {
    if (!seen.has(opt.value)) seen.set(opt.value, opt)
  }
  // 确保当前 modelValue 在列表中
  const val = props.modelValue
  if (val) {
    const vals = Array.isArray(val) ? val : [val]
    for (const v of vals) {
      if (v && !seen.has(v)) seen.set(v, { label: v, value: v })
    }
  }
  return [...seen.values()]
})

/** 本地 v-model 中介：通过改变引用让 Element Plus 感知 options label 变化 */
const remoteModelValue = ref<string | string[]>(props.modelValue)
let propSyncing = false

/** 值相等判断（含同内容数组），阻断引用变化导致的死循环 */
function isSameValue(a: string | string[], b: string | string[]): boolean {
  if (typeof a === 'string' && typeof b === 'string') return a === b
  if (Array.isArray(a) && Array.isArray(b)) {
    return a.length === b.length && a.every((v, i) => v === b[i])
  }
  return false
}

// prop → local
watch(() => props.modelValue, (val) => {
  if (propSyncing) return
  const isMulti = props.operator === 'in'
  let nextValue: string | string[]
  if (isMulti) {
    nextValue = Array.isArray(val) ? [...val] : (val ? [val as string] : [])
  } else {
    nextValue = Array.isArray(val) ? (val[0] ?? '') : (val as string)
  }
  // 值相同则跳过，避免创建新数组引用导致死循环
  if (isSameValue(nextValue, remoteModelValue.value)) return
  remoteModelValue.value = nextValue
})

// operator 切换时，同步值类型（多选 ↔ 单选）并通知父组件
watch(() => props.operator, (op) => {
  let normalized: string | string[]
  if (op === 'in') {
    normalized = Array.isArray(remoteModelValue.value)
      ? remoteModelValue.value
      : (remoteModelValue.value ? [remoteModelValue.value as string] : [])
  } else {
    normalized = Array.isArray(remoteModelValue.value)
      ? (remoteModelValue.value[0] ?? '')
      : (remoteModelValue.value as string)
  }
  // 阻止 remoteModelValue watcher 重复 emit
  propSyncing = true
  remoteModelValue.value = normalized
  propSyncing = false
  emit('update:modelValue', normalized as string | string[])
})

// local → prop
watch(remoteModelValue, (val) => {
  if (propSyncing) return
  // 值与 props 相同时不 emit，阻断父→子→父循环
  if (isSameValue(val, props.modelValue)) return
  emit('update:modelValue', val as string | string[])
})

// 缓存首次提供真实 label 时，创建新引用让 Element Plus 自动刷新显示
watch(() => {
  if (!props.column) return false
  const cache = plainOptionsCache[props.column]
  if (!cache || cache.length === 0) return false
  const val = props.modelValue
  if (!val) return false
  const vals = Array.isArray(val) ? val : [val]
  return vals.every((v) => cache.some((o) => o.value === v && o.label !== v))
}, (ready) => {
  if (ready) {
    propSyncing = true
    remoteModelValue.value = Array.isArray(props.modelValue)
      ? [...(props.modelValue as string[])]
      : (props.modelValue as string)
    propSyncing = false
  }
})

async function onRemoteSearch(query: string) {
  if (!props.remoteMethod) return
  remoteLoading.value = true
  try {
    const items = await props.remoteMethod(query)
    remoteOptions.value = items
    if (props.column) {
      plainOptionsCache[props.column] = items
      cacheVersion.value++
    }
  } finally {
    remoteLoading.value = false
  }
}
</script>
