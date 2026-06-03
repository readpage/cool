<template>
  <div class="search-panel">
    <div class="toolbar">
      <Query
        ref="queryRef"
        :columns="config.filter"
        :exposed="exposedProps"
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
  filterValues?: import('./types').FilterResult[]
}
</script>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Input from './input.vue'
import Query from './query.vue'
import ExposedFilter from './ExposedFilter.vue'
import { buildFilter } from './hooks/useSearchHelpers'
import type { ColumnConfig, FilterResult } from './types'

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

const exposedProps = computed(() =>
  props.config.filter
    .filter((c) => c.filterMode === 'show' || c.filterMode === 'exposed')
    .map((c) => c.prop),
)

const queryRef = ref<InstanceType<typeof Query>>()

/* ============ 辅助 ============ */

function syncFilterMode() {
  const conds = queryRef.value?.conditions ?? []
  props.config.filter.forEach((col) => {
    const cond = conds.find((c) => c.column === col.prop)
    if (!cond || !cond.column) {
      col.filterMode = 'hide'
    } else {
      col.operator = cond.operator
      col.filterMode = cond.display ? 'exposed' : 'show'
    }
  })
}

/* ============ 事件处理 ============ */

function onFilter(params: FilterResult[]) {
  props.config.filterValues = params
  syncFilterMode()
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
