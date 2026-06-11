<template>
  <!-- 日期 -->
  <el-date-picker
    v-if="fieldType === 'date' || fieldType === 'datetime' || fieldType === 'year' || fieldType === 'month'"
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    :type="dateType"
    :format="dateFormat"
    :value-format="dateFormat"
    :placeholder="placeholder"
    :class="[attrs.class, 'cond-value']"
    :teleported="false"
    clearable
  />

  <!-- 静态下拉 -->
  <el-select
    v-else-if="fieldType === 'select'"
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    :multiple="operator === 'in'"
    :collapse-tags="operator === 'in'"
    :collapse-tags-tooltip="operator === 'in'"
    :max-collapse-tags="3"
    :placeholder="placeholder"
    :class="[attrs.class, 'cond-value']"
    style="min-width: 150px"
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
    :collapse-tags="operator === 'in'"
    :collapse-tags-tooltip="operator === 'in'"
    :max-collapse-tags="1"
    :placeholder="placeholder"
    :class="[attrs.class, 'cond-value']"
    style="min-width: 170px"
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
    :class="[attrs.class, 'cond-value']"
    clearable
  />
</template>

<script setup lang="ts">
import { ref, computed, watch, useAttrs, onMounted } from 'vue'
import { plainOptionsCache, cacheVersion, type OptionItem } from './filterCache'

/* ============ 多根节点 class 透传 ============ */

defineOptions({ inheritAttrs: false })

const attrs = useAttrs()

/* ============ Props & Emits ============ */

const props = defineProps<{
  modelValue: string | string[]
  /** 控件类型 */
  fieldType?: 'text' | 'date' | 'datetime' | 'year' | 'month' | 'daterange' | 'datetimerange' | 'select' | 'remote-select'
  /** 日期选择器子类型（覆盖 fieldType 的默认映射） */
  pickerType?: 'date' | 'datetime' | 'year' | 'month' | 'week'
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

const dateType = computed(() => {
  if (props.pickerType) return props.pickerType
  if (props.fieldType === 'datetime') return 'datetime'
  if (props.fieldType === 'year') return 'year'
  if (props.fieldType === 'month') return 'month'
  return 'date'
})
const dateFormat = computed(() => {
  if (props.pickerType === 'year' || props.fieldType === 'year') return 'YYYY'
  if (props.pickerType === 'month' || props.fieldType === 'month') return 'YYYY-MM'
  if (props.pickerType === 'week') return 'YYYY ww'
  if (props.fieldType === 'datetime') return 'YYYY-MM-DD HH:mm:ss'
  return 'YYYY-MM-DD'
})

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

/** 挂载时预加载：若当前有值但缓存无真实 label，自动拉取选项 */
onMounted(async () => {
  if (props.fieldType !== 'remote-select') return
  if (!props.modelValue) return
  if (!props.remoteMethod) return

  const vals = Array.isArray(props.modelValue) ? props.modelValue : [props.modelValue]
  if (vals.length === 0) return

  // 检查是否所有值都已存在于缓存中（且有真实 label，非 value===label 的兜底）
  const missing = vals.filter(v => {
    if (!v) return false
    const cached = safeRemoteOptions.value
    return !cached.some(o => o.value === v && o.label !== v)
  })

  if (missing.length === 0) return  // 已有真实 label，无需加载

  try {
    remoteLoading.value = true
    const items = await props.remoteMethod('')
    remoteOptions.value = items
    if (props.column) {
      plainOptionsCache[props.column] = items
      cacheVersion.value++
    }
  } catch {
    // 静默失败，已有兜底显示
  } finally {
    remoteLoading.value = false
  }
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
