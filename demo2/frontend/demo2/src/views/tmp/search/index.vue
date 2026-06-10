<template>
  <div class="search-panel">
    <div class="toolbar">
      <Query
        ref="queryRef"
        :columns="config.filter"
        :exposed="exposedProps"
        :operator-options="operatorOptions"
        @filter="onFilter"
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
      @submit="onSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Input from './input.vue'
import Query from './query.vue'
import ExposedFilter from './ExposedFilter.vue'

/* ============ 类型 ============ */

export interface ColumnConfig {
  prop: string
  label: string
  operator?: 'contains' | 'eq' | 'ne' | 'gt' | 'lt' | 'gte' | 'lte' | 'between' | 'in'
  filterMode?: 'show' | 'exposed' | 'hide'
}

export interface SearchConfig {
  filter: ColumnConfig[]
  currentField?: string
  filterValues?: FilterItem[]
}

export type FilterItem = {
  column: string
  operator: 'contains' | 'eq' | 'ne' | 'gt' | 'lt' | 'gte' | 'lte' | 'between' | 'in'
  value: string | [string, string] | string[]
}

/* ============ Props & Emits ============ */

const props = defineProps<{ config: SearchConfig }>()

const emit = defineEmits<{
  (e: 'save-filter', value: SearchConfig): void
  (e: 'search', value: FilterItem[]): void
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

function buildFilter(values: { column: string; operator: string; value: any; valueStr: string }[]): FilterItem[] {
  return values
    .filter((c) => c.column)
    .map((c) => {
      let value: string | [string, string] | string[]
      if (c.operator === 'between') {
        value = [c.value[0] ?? '', c.value[1] ?? '']
      } else if (c.operator === 'in') {
        value = c.valueStr?.split(',').map((v: string) => v.trim()).filter(Boolean) ?? []
      } else {
        value = c.value
      }
      return { column: c.column, operator: c.operator as FilterItem['operator'], value }
    })
}

/* ============ 事件处理 ============ */

function onFilter(params: FilterItem[]) {
  props.config.filterValues = params
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
  const filter: FilterItem[] = Object.entries(params)
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
