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
  >
    <el-option
      v-for="opt in normalizedOptions"
      :key="opt.value"
      :label="opt.label"
      :value="opt.value"
    />
  </el-select>

  <!-- 远程搜索下拉 -->
  <el-select
    v-else-if="fieldType === 'remote-select'"
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    :multiple="operator === 'in'"
    :placeholder="placeholder"
    class="cond-value"
    :teleported="false"
    filterable
    remote
    :remote-method="onRemoteSearch"
    :loading="remoteLoading"
  >
    <el-option
      v-for="opt in remoteOptions"
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
import { ref, computed } from 'vue'

/* ============ 类型 ============ */

interface OptionItem {
  label: string
  value: string
}

/* ============ Props & Emits ============ */

const props = defineProps<{
  modelValue: string | string[]
  /** 控件类型 */
  fieldType?: 'text' | 'date' | 'datetime' | 'daterange' | 'datetimerange' | 'select' | 'remote-select'
  /** 当前操作符（影响 select 是否多选） */
  operator?: string
  placeholder?: string
  /** 静态选项（fieldType='select' 时使用） */
  options?: (OptionItem | string)[]
  /** 远程搜索方法（fieldType='remote-select' 时使用） */
  remoteMethod?: (query: string) => Promise<OptionItem[]>
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

/* ============ 远程搜索 ============ */

const remoteOptions = ref<OptionItem[]>([])
const remoteLoading = ref(false)

async function onRemoteSearch(query: string) {
  if (!props.remoteMethod) return
  remoteLoading.value = true
  try {
    remoteOptions.value = await props.remoteMethod(query)
  } finally {
    remoteLoading.value = false
  }
}
</script>
