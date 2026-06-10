<template>
  <div class="search-panel">
    <div class="toolbar">
      <Query
        ref="queryRef"
        :columns="config.filter"
        :initial-values="initialFilterValues"
        :operator-options="operatorOptions"
        :load-options="loadOptions"
        :show-admin-btn="showAdminBtn"
        @filter="onFilter"
        @admin-confirm="emit('admin-confirm')"
      />
      <Input
        :options="searchOptions"
        :init-field="config.currentField"
        @search="onSearch"
        @change="onInputChange"
      />
    </div>
    <ExposedFilter
      v-if="queryRef"
      :conditions="queryRef.conditions"
      :columns="config.filter"
      :operator-options="operatorOptions"
      :load-options="loadOptions"
      @submit="onSubmit"
    />
  </div>
</template>

<script lang="ts">
// 模块级导出（<script setup> 不支持 export 语法）
export type { ColumnConfig, FilterResult } from './types'
export { buildFilter } from './hooks/useSearchHelpers'
// SearchConfig 是此文件特有，在此定义并导出
export interface SearchConfig {
  filter: import('./types').ColumnConfig[]
  currentField?: string
}
</script>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Input from './input.vue'
import Query from './query.vue'
import ExposedFilter from './ExposedFilter.vue'
import { buildFilter } from './hooks/useSearchHelpers'
import type { ColumnConfig, FilterResult, FilterOperator } from './types'

/* ============ Props & Emits ============ */


const props = defineProps<{
  config: SearchConfig
  loadOptions?: (type: string, keyword?: string) => Promise<{ label: string; value: string }[]>
  showAdminBtn?: boolean
}>()

const emit = defineEmits<{
  (e: 'save-filter', value: SearchConfig): void
  (e: 'search', value: FilterResult[]): void
  (e: 'admin-confirm'): void
}>()

/* ============ 常量 ============ */

const operatorOptions = [
  { label: '包含', value: 'contains' },
  { label: '等于', value: 'eq' },
  { label: '不等于', value: 'ne' },
  { label: '大于', value: 'gt' },
  { label: '小于', value: 'lt' },
  { label: '大于等于', value: 'gte' },
  { label: '小于等于', value: 'lte' },
  { label: '区间', value: 'between' },
  { label: '属于', value: 'in' },
]

/* ============ 计算属性 ============ */

const searchOptions = computed(() =>
  props.config.filter.map((c) => ({ label: c.label, value: c.prop })),
)

/** 从蓝图提取默认筛选值，供 query.vue JSON 深拷贝初始化 */
const initialFilterValues = computed<FilterResult[]>(() => {
  const result = props.config.filter
    .map(c => {
      const v = c.value
      if (v === undefined || v === null) return null
      if (typeof v === 'string' && v === '') return null
      if (Array.isArray(v) && v.length === 0) return null
      return {
        column: c.prop,
        operator: (c.operator || 'contains') as FilterOperator,
        value: v as FilterResult['value'],
      }
    })
    .filter(Boolean) as FilterResult[]
  console.log('[search/index] initialFilterValues computed →', result.map(r => `${r.column}=${Array.isArray(r.value) ? `[${r.value}]` : r.value}`))
  return result
})

const queryRef = ref<InstanceType<typeof Query>>()

/* ============ 事件处理 ============ */

function onFilter(params: FilterResult[]) {
  // 🔑 根据 FilterResult[] + 原有列元数据，重建 filter 数组（响应式整体替换）
  const paramMap = new Map(params.filter(p => p.column).map(p => [p.column, p]))

  // 🔑 从 conditions 读取 filterMode（统一口径，FilterResult 不含 filterMode）
  const filterModeMap = new Map<string, string>()
  queryRef.value?.conditions.forEach(cond => {
    if (cond.column) filterModeMap.set(cond.column, cond.filterMode)
  })

  props.config.filter = props.config.filter.map(col => {
    const pv = paramMap.get(col.prop)
    const fm = filterModeMap.get(col.prop)
    const next: typeof col = { ...col }
    if (pv) {
      next.value = pv.value
      next.operator = pv.operator
    } else {
      next.value = ''
    }
    // 直接从 conditions.filterMode 写回 config.filter.filterMode
    if (fm !== undefined) {
      next.filterMode = fm as typeof col['filterMode']
    }
    return next
  })
  console.log('[search/index] onFilter →', params.map(p => `${p.column}=${p.value}`))
  emit('save-filter', props.config)
  emit('search', params)
}

function onSubmit() {
  const filter = buildFilter(queryRef.value!.conditions)
  emit('search', filter)
}

function onInputChange(payload: { field: string; keyword: string }) {
  props.config.currentField = payload.field
  emit('save-filter', props.config)
}

function onSearch(params: Record<string, string>) {
  const filter: FilterResult[] = Object.entries(params)
    .filter(([, v]) => v)
    .map(([column, value]) => ({ column, operator: 'contains', value }))
  emit('search', filter)
}
</script>

<style lang="scss" scoped>
.search-panel {
  .toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}
</style>
