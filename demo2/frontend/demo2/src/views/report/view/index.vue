<template>
  <div class="report-view-page">
    <!-- 顶部工具栏 -->
    <div class="report-toolbar">
      <div class="toolbar-left">
        <span class="report-name">{{ reportName || '请选择报表' }}</span>
        <el-tag v-if="datasourceLabel" size="small" type="info" effect="plain">
          {{ datasourceLabel }}
        </el-tag>
      </div>
      <div class="toolbar-right">
        <ReportDrawer />
      </div>
    </div>

    <!-- 主内容区：侧边栏 + 报表主体 -->
    <div class="report-main">
      <ReportSidebar
        :current-table-key="currentTableKey"
        @select="onReportSelect"
        @edit="onReportEdit"
      />

      <div class="report-body">
        <Table
          ref="tableRef"
          v-if="tableConfig.columns.length"
          id="report-view"
          :config="tableConfig"
          :data="tableData"
          :show-admin-btn="false"
          :export="handleExport"
          @query="onPageChange"
          @change="saveConfigToStore"
          @reset="resetConfigToSystem"
        />
        <el-empty
          v-else
          description="点击左侧报表列表选择要查看的报表"
          :image-size="120"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch, ref, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import Table, { type ExportParams } from '@/components/table/index.vue'
import ReportDrawer from './components/ReportDrawer.vue'
import ReportSidebar from './components/ReportSidebar.vue'
import { useReportView } from './hooks/useReportView'
import { AReport } from '@/api/report'
import type { ReportQueryBody } from '@/api/report'

const route = useRoute()
const router = useRouter()

const {
  currentTableKey,
  reportName,
  datasourceLabel,
  tableConfig,
  tableData,
  variableMap,
  loadAndQuery,
  onPageChange,
  saveConfigToStore,
  resetConfigToSystem,
} = useReportView()

/** 侧边栏中选择报表 */
async function onReportSelect(tableKey: string) {
  if (tableKey === currentTableKey.value) return
  currentTableKey.value = tableKey
  await loadAndQuery(tableKey)
  router.replace({ query: { tableKey } })
  await nextTick()
  tableRef.value?.refresh()
}

/** 侧边栏中双击 → 跳转编辑页 */
function onReportEdit(tableKey: string) {
  router.push({
    path: '/report/design',
    query: { tableKey },
  })
}

/** 导出 */
async function handleExport(params: ExportParams) {
  if (!currentTableKey.value) return
  const body: ReportQueryBody = {
    columns: params.columns,
    filter: params.filter.map(f => {
      const isVar = variableMap.value[f.column!]
      return { column: f.column!, operator: f.operator!, value: f.value, ...(isVar ? { variable: true } : {}) }
    }),
    sort: params.sort ? { column: params.sort.column, direction: params.sort.direction as 'asc' | 'desc' } : undefined,
  }
  return AReport.export(currentTableKey.value, body)
    .then(ok => { if (!ok) ElMessage.warning('导出文件为空') })
    .catch(() => { ElMessage.error('导出失败') })
}

const isReady = ref(false)
const tableRef = ref<{ refresh: () => void }>()

onMounted(() => {
  const tk = route.query.tableKey as string
  if (tk) {
    currentTableKey.value = tk
    loadAndQuery(tk)
  }
  nextTick(() => { isReady.value = true })
})

// 监听 URL 变化（浏览器前进/后退），切换到对应报表
watch(
  () => route.query.tableKey,
  async (tk) => {
    if (!isReady.value) return
    if (tk && typeof tk === 'string' && tk !== currentTableKey.value) {
      currentTableKey.value = tk
      await loadAndQuery(tk)
      await nextTick()
      tableRef.value?.refresh()
    }
  },
)

</script>

<style lang="scss" scoped>
.report-view-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f6fa;
}

.report-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.report-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.report-main {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
}

.report-body {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #fff;

  .el-empty {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
}

// 适配 Table 组件
:deep(.u-table) {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.u-table-toolbar) {
  flex-shrink: 0;
  padding: 4px 12px;
}

:deep(.u-table-wrapper) {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

:deep(.u-table-pagination) {
  flex-shrink: 0;
}
</style>
