<template>
  <Sider class="report-sider" :width="340" separator>
    <div class="report-workspace">
      <SqlPanel
        v-model="sqlTemplate"
        :top-height="topHeight"
        :loading="execLoading"
        @run="execute"
      />

      <SplitDivider @mousedown="onDividerMouseDown" />

      <ResultPanel
        :result-data="resultData"
        :table-config="tableConfig"
        :table-data="tableData"
        @query="onPageChange"
      />
    </div>

    <template #aside>
      <div class="aside-wrap">
        <FilterSortPanel
          :available-columns="availableColumns"
          :filter-conditions="filterConditions"
          :sort-conditions="sortConditions"
          :show-filter="showFilter"
          :show-sort="showSort"
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
import { ElMessage } from 'element-plus'
import Sider from '@/components/sider/index.vue'
import SqlPanel from './components/SqlPanel.vue'
import SplitDivider from './components/SplitDivider.vue'
import ResultPanel from './components/ResultPanel.vue'
import FilterSortPanel from './components/FilterSortPanel.vue'
import { useReportWorkspace } from './hooks/useReportWorkspace'
import { useDragSplit } from './hooks/useDragSplit'
import { AReport } from '@/api/report'
import type { TableQuery } from '@/types/table'

// ==================== hooks（纯状态，不含 API） ====================
const {
  queryParam,
  sqlTemplate,
  currentTableKey,
  execLoading,
  resultData,
  lastPage,
  availableColumns,
  showFilter,
  showSort,
  tableData,
  tableConfig,
  filterConditions,
  sortConditions,
  addFilter,
  removeFilter,
  addSort,
  removeSort,
  loadReport,
} = useReportWorkspace()

const { topHeight, onDividerMouseDown } = useDragSplit()

// ==================== API 调用（统一在 index.vue 管理） ====================

/** 执行查询 */
async function execute() {
  if (!sqlTemplate.value.trim()) {
    ElMessage.warning('请输入 SQL')
    return
  }

  execLoading.value = true
  lastPage.current = 1
  lastPage.size = 20
  try {
    const { data } = await AReport.execute(queryParam.value)
    resultData.value = data ?? null
  } catch (err) {
    console.error('查询失败', err)
    ElMessage.error('查询失败')
  } finally {
    execLoading.value = false
  }
}

/** 翻页 */
async function onPageChange(v: TableQuery, done: () => void) {
  lastPage.current = v.current
  lastPage.size = v.size
  try {
    const { data } = await AReport.execute(queryParam.value)
    resultData.value = data ?? null
  } catch (err) {
    console.error('翻页查询失败', err)
  } finally {
    done()
  }
}

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

  :deep(.filter-sort-panel) {
    flex: 1;
    overflow-y: auto;
  }
}
</style>
