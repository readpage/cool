<template>
  <Sider class="report-sider" :width="340" separator>
    <div class="report-workspace">
      <SqlPanel
        v-model="sqlTemplate"
        :top-height="topHeight"
        :loading="execLoading"
        @run="execute"
        @detect="detectParams"
      />

      <SplitDivider @mousedown="onDividerMouseDown" />

      <ResultPanel
        :result-data="resultData"
        :table-config="tableConfig"
        :table-data="tableData"
        :parameters="parameters"
        :param-values="paramValues"
        @query="onPageChange"
        @execute="execute"
        @reset="resetParams"
      />
    </div>

    <template #aside>
      <div class="aside-wrap">
        <ParamConfig
          :parameters="parameters"
          @update:parameters="updateParams"
        />
        <FilterSortPanel
          :available-columns="availableColumns"
          :filter-conditions="filterConditions"
          :sort-conditions="sortConditions"
          :sql-template="sqlTemplate"
          @add-filter="addFilter"
          @remove-filter="removeFilter"
          @add-sort="addSort"
          @remove-sort="removeSort"
        />
      </div>
    </template>
  </Sider>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import Sider from '@/components/sider/index.vue'
import SqlPanel from './components/SqlPanel.vue'
import SplitDivider from './components/SplitDivider.vue'
import ResultPanel from './components/ResultPanel.vue'
import ParamConfig from './components/ParamConfig.vue'
import FilterSortPanel from './components/FilterSortPanel.vue'
import { useReportWorkspace } from './composables/useReportWorkspace'
import { useDragSplit } from './composables/useDragSplit'

const {
  sqlTemplate, parameters, paramValues,
  execLoading, resultData,
  tableData, tableConfig, availableColumns,
  filterConditions, sortConditions,
  addFilter, removeFilter, addSort, removeSort,
  detectParams, resetParams, updateParams, execute, onPageChange,
} = useReportWorkspace()

const { topHeight, onDividerMouseDown } = useDragSplit()

// ==================== 快捷键 Ctrl+Enter ====================
function onKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
    e.preventDefault()
    execute()
  }
}

onMounted(() => document.addEventListener('keydown', onKeydown))
onUnmounted(() => document.removeEventListener('keydown', onKeydown))
</script>

<style lang="scss" scoped>
.report-sider {
  height: 100vh;
}

.report-workspace {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f6fa;
  overflow: hidden;
}

.aside-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;

  :deep(.param-config) {
    flex: 1;
    min-height: 0;
  }

  :deep(.filter-sort-panel) {
    flex-shrink: 0;
    max-height: 45%;
    overflow-y: auto;
    border-top: 2px solid #e8e8e8;
  }
}
</style>
