<template>
  <div class="search-panel">
    <div class="toolbar">
      <Query ref="queryRef" :columns="config.filter" :exposed="exposedProps" @filter="onFilter" />
      <Input :options="searchOptions" :init-field="config.currentField" @search="onSearch" @change="onInputChange" />
    </div>
    <ExposedFilter
      v-if="queryRef"
      :conditions="exposedConditions"
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

interface ColumnConfig {
  prop: string
  label: string
  operator?: string
  exposed?: boolean
}

export interface SearchConfig {
  filter: ColumnConfig[]
  currentField?: string
}

export type FilterItem = {
  column: string
  operator: string
  value: string | [string, string] | string[]
}

/* ============ Props & Emits ============ */

const props = defineProps<{
  config: SearchConfig
}>()

const emit = defineEmits<{
  (e: 'save-filter', value: FilterItem[]): void
}>()

/* ============ 操作符选项 ============ */

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

const searchOptions = computed(() =>
  props.config.filter.map((c) => ({ label: c.label, value: c.prop }))
)

const exposedProps = computed(() =>
  props.config.filter.filter((c) => c.exposed).map((c) => c.prop)
)

const queryRef = ref()

const exposedConditions = computed(() => queryRef.value?.conditions ?? [])

/* ============ 事件处理 ============ */

function onFilter(params: FilterItem[]) {
  emit('save-filter', params)
}

function onSubmit() {
  const conds = exposedConditions.value
  const filter: FilterItem[] = conds
    .filter((c: any) => c.column)
    .map((c: any) => {
      let value: string | [string, string] | string[] = c.value
      if (c.operator === 'in') {
        value = c.valueStr?.split(',').map((v: string) => v.trim()).filter(Boolean) ?? []
      }
      if (c.operator === 'between') {
        value = [c.value[0] ?? '', c.value[1] ?? '']
      }
      return { column: c.column, operator: c.operator, value }
    })
  emit('save-filter', filter)
}

function onInputChange(payload: { field: string; keyword: string }) {
  props.config.currentField = payload.field
}

function onSearch(params: Record<string, string>) {
  const filter: FilterItem[] = Object.entries(params)
    .filter(([, v]) => v)
    .map(([column, value]) => ({ column, operator: 'contains', value }))
  emit('save-filter', filter)
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
