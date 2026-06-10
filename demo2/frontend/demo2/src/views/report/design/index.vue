<template>
  <Sider class="report-sider" :width="340" separator>
    <div class="report-workspace">
      <SqlPanel
        v-model="sqlTemplate"
        :top-height="topHeight"
        :loading="execLoading"
        :report-name="reportName"
        :is-saved="isSaved"
        :last-saved-time="lastSavedTime"
        :datasource-id="datasourceId"
        :category="category"
        @update:report-name="reportName = $event"
        @update:datasource-id="datasourceId = $event"
        @update:category="category = $event"
        @run="execute"
        @save="saveReport"
        @back="goBack"
      />

      <SplitDivider @mousedown="onDividerMouseDown" />

      <ResultPanel
        :result-data="resultData"
        :table-config="tableConfig"
        :table-data="tableData"
        :export="handleExport"
        @query="onPageChange"
      />
    </div>

    <template #aside>
      <div class="aside-wrap">
        <div class="aside-header">
          <span class="aside-title">
            配置 JSON
            <span v-if="jsonDirty" class="unsaved-dot" title="JSON 有未应用的修改"></span>
          </span>
          <div class="aside-header-actions">
            <el-tooltip content="从当前状态重新生成 JSON" placement="bottom">
              <el-button size="small" text @click="handleRefresh">刷新</el-button>
            </el-tooltip>
            <el-tooltip content="将 JSON 编辑应用到预览" placement="bottom">
              <el-button size="small" type="primary" @click="applySidebarJson">应用</el-button>
            </el-tooltip>
          </div>
        </div>
        <CodeEditor ref="jsonEditorRef" language="json" v-model="sidebarJsonText" class="aside-editor" />
      </div>
    </template>
  </Sider>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, computed, watch, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Sider from '@/components/sider/index.vue'
import SqlPanel from './components/SqlPanel.vue'
import SplitDivider from './components/SplitDivider.vue'
import ResultPanel from './components/ResultPanel.vue'
import CodeEditor from '@/components/code-editor/CodeEditor.vue'
import { useSqlEditor } from './hooks/useSqlEditor'
import { useQueryRuntime } from './hooks/useQueryRuntime'
import { useReportPersistence } from './hooks/useReportPersistence'
import { useFilterSort, validateSqlAliases, parseSqlColumns } from './hooks/useFilterSort'
import { useDragSplit } from './hooks/useDragSplit'
import { throttlePromise } from '@/utils/throttle'
import { AReport } from '@/api/report'
import type { ReportExecuteBody } from '@/api/report'
import type { TableQuery } from '../types/table'
import type { FilterCondition } from '../types/report'
import type { ExportParams } from '@/components/table/index.vue'

// ==================== 组合 hooks（按职责拆分，显式组装） ====================

const { sqlTemplate, availableColumns, variableKeys } = useSqlEditor()

const {
  filterConditions,
  sortConditions,
  getEffectiveFilters,
  autoInitFromColumns,
} = useFilterSort()

const {
  currentTableKey,
  currentReportId,
  reportName,
  datasourceId,
  category,
  tableConfig,
  loadReport,
  buildSaveRequest,
  refreshTableConfig,
  updateTableConfig,
} = useReportPersistence(sqlTemplate, filterConditions, sortConditions, getEffectiveFilters, variableKeys)

const {
  execLoading,
  resultData,
  lastPage,
  tableSearchFilters,
  tableData,
  queryParam,
  resetQueryState,
} = useQueryRuntime(sqlTemplate, filterConditions, sortConditions, getEffectiveFilters, datasourceId)

const { topHeight, onDividerMouseDown } = useDragSplit()

const route = useRoute()
const router = useRouter()

// ==================== 侧边栏 JSON 配置编辑器 ====================

const sidebarJsonText = ref('')

/** 从当前响应式状态构建 JSON 配置文本 */
function buildSidebarJson(): string {
  const displayConfig = JSON.parse(JSON.stringify(tableConfig.value)) as any
  if (sortConditions.value.length > 0) {
    displayConfig.sort = { ...sortConditions.value[0] }
  }
  return JSON.stringify({ displayConfig }, null, 2)
}

const jsonEditorRef = ref<InstanceType<typeof CodeEditor>>()
const jsonDirty = computed(() => jsonEditorRef.value?.isDirty ?? false)

/** 手动刷新 JSON 编辑器（从当前状态重新生成） */
function refreshSidebarJson() {
  sidebarJsonText.value = buildSidebarJson()
  jsonEditorRef.value?.markClean()
}

/** 刷新按钮，二次确认已丢弃手写 JSON 修改 */
function handleRefresh() {
  if (jsonDirty.value) {
    ElMessageBox.confirm('刷新会丢弃当前 JSON 编辑，是否继续？', '提示', {
      confirmButtonText: '继续刷新',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      refreshSidebarJson()
    }).catch(() => {})
  } else {
    refreshSidebarJson()
  }
}

/** 将 JSON 编辑器中的修改解析并应用到响应式状态，返回是否成功 */
function applySidebarJson(): boolean {
  try {
    const parsed = JSON.parse(sidebarJsonText.value)

    console.log('[applySidebarJson] === 开始应用 JSON 配置 ===')

    // 1) Apply displayConfig（合并保留用户自定义样式 + SQL 新列）
    if (parsed.displayConfig) {
      // 日志：应用前的 filter 配置
      console.log('[applySidebarJson] 应用前 filter:', JSON.stringify(
        tableConfig.value.search?.filter?.map((f: any) => ({ prop: f.prop, label: f.label, value: f.value, filterMode: f.filterMode, variable: f.variable }))
      ))
      console.log('[applySidebarJson] 即将应用的 displayConfig.filter:', JSON.stringify(
        parsed.displayConfig.search?.filter?.map((f: any) => ({ prop: f.prop, label: f.label, value: f.value, filterMode: f.filterMode, variable: f.variable }))
      ))
      console.log('[applySidebarJson] variableKeys:', JSON.stringify(variableKeys.value))

      updateTableConfig(parsed.displayConfig)

      // 日志：应用后的 filter 配置
      console.log('[applySidebarJson] 应用后 filter:', JSON.stringify(
        tableConfig.value.search?.filter?.map((f: any) => ({ prop: f.prop, label: f.label, value: f.value, filterMode: f.filterMode, variable: f.variable }))
      ))

      // sort 内嵌于 displayConfig.sort
      if (parsed.displayConfig.sort?.column && parsed.displayConfig.sort?.direction) {
        sortConditions.value = [{ column: parsed.displayConfig.sort.column, direction: parsed.displayConfig.sort.direction }]
      } else {
        sortConditions.value = []
      }
    }

    jsonEditorRef.value?.markClean()
    // 应用成功后同步回 JSON 编辑器，保持一致
    sidebarJsonText.value = buildSidebarJson()
    console.log('[applySidebarJson] === 应用完成，同步回 JSON 编辑器 ===')
    ElMessage.success('配置已应用')
    return true
  } catch (e) {
    console.error('JSON parse error', e)
    ElMessage.error('JSON 格式错误，请检查语法')
    return false
  }
}

// SQL 列变化时自动初始化筛选/排序默认行
watch(availableColumns, (cols) => {
  if (cols && cols.length > 0 && filterConditions.value.length === 0) {
    autoInitFromColumns(cols, sqlTemplate.value)
  }
})

// 页面初始化 & 加载报告后侧边栏 JSON 同步
// 后续不再自动覆盖 JSON 手写内容，由用户手动点"刷新"或"应用"触发
watch([sqlTemplate, tableConfig, filterConditions, sortConditions], () => {
  console.log('[watch tableConfig] watcher 触发, jsonDirty:', jsonDirty.value, 'filter:', JSON.stringify(
    tableConfig.value.search?.filter?.map((f: any) => ({ prop: f.prop, filterMode: f.filterMode }))
  ))
  if (!jsonDirty.value) {
    sidebarJsonText.value = buildSidebarJson()
  }
}, { deep: true })

// ==================== 报告元信息 ====================

const lastSavedTime = ref('')

function hasUnsavedChange(): boolean {
  return sqlTemplate.value.trim().length > 0
}

const isSaved = computed(() => currentTableKey.value !== '' && !hasUnsavedChange())

/** 返回列表页 */
function goBack() {
  router.back()
}

/** SQL 变更后标记未保存 */
watch(sqlTemplate, () => {
  if (currentTableKey.value) {
    lastSavedTime.value = ''
  }
})

// ==================== API 调用（统一在 index.vue 管理） ====================

/** 执行查询（Promise 安全节流，不会丢弃调用） */
const execute = throttlePromise(async () => {
  if (!sqlTemplate.value.trim()) {
    ElMessage.warning('请输入 SQL')
    return
  }

  // 检查 #{key} 变量是否与 SELECT 列别名重名
  const sqlCols = parseSqlColumns(sqlTemplate.value)
  const colProps = new Set(sqlCols.map(c => c.prop))
  const conflicts = variableKeys.value.filter(k => colProps.has(k))
  if (conflicts.length > 0) {
    ElMessage.warning(`模板变量 #{${conflicts.join('}, #{')}} 与 SELECT 列重名，请修改列别名或变量名`)
    return
  }

  // 点击运行按钮 → 刷新表格列配置（新增列按指定位置插入）
  refreshTableConfig()

  execLoading.value = true
  resetQueryState()
  try {
    const { data } = await AReport.execute(queryParam.value)
    resultData.value = data ?? null
  } catch (err: any) {
    console.error('查询失败', err)
    const msg = err?.response?.data?.msg || err?.msg || err?.message || '查询失败'
    ElMessage.error(msg)
  } finally {
    execLoading.value = false
  }
}, 300)

/** 翻页 / 筛选 / 排序（Promise 安全节流，done 回调正常结束） */
const onPageChange = throttlePromise(async (v: TableQuery, done: () => void) => {
  lastPage.current = v.current
  lastPage.size = v.size
  // 同步表格搜索工具栏的筛选值
  const filters: FilterCondition[] = []

  // 从 tableConfig.search.filter 获取 variable 标记
  const variableProps = new Set(
    (tableConfig.value.search?.filter ?? [])
      .filter(col => col.variable)
      .map(col => col.prop),
  )

  for (const f of v.filter) {
    if (f.column) {
      filters.push({
        column: f.column,
        operator: f.operator,
        value: f.value,
        ...(variableProps.has(f.column) ? { variable: true } : {}),
      })
    }
  }
  tableSearchFilters.value = filters
  // 🔑 同步 filterConditions：Table 搜索栏"拥有" search.filter 中所有列，无论 Table 当前筛选是否为空
  // 将这些列从 filterConditions 全部清除，防止已清空的列从 filterConditions 泄露旧值
  const searchColProps = new Set(
    (tableConfig.value.search?.filter ?? []).map(col => col.prop),
  )
  filterConditions.value = filterConditions.value.filter(f => !searchColProps.has(f.column))
  // 同步表格排序
  if (v.sort) {
    sortConditions.value = [{ column: v.sort.column, direction: v.sort.direction as 'asc' | 'desc' }]
  } else {
    sortConditions.value = []
  }

  try {
    const { data } = await AReport.execute(queryParam.value)
    resultData.value = data ?? null
  } finally {
    done()
  }
}, 300)

/** 导出 */
async function handleExport(params: ExportParams) {
  const body: ReportExecuteBody = {
    sqlTemplate: sqlTemplate.value,
    columns: params.columns,
    filter: params.filter.map(f => ({
      column: f.column!,
      operator: f.operator!,
      value: f.value,
      ...(f.variable ? { variable: true } : {}),
    })),
    sort: params.sort ? { column: params.sort.column, direction: params.sort.direction as 'asc' | 'desc' } : undefined,
    datasourceId: datasourceId.value,
  }
  return AReport.executeExport(body)
    .then(ok => { if (!ok) ElMessage.warning('导出文件为空') })
    .catch(() => { ElMessage.error('导出失败') })
}

/** 保存报告 */
async function saveReport() {
  // 如果 JSON 有未应用的修改，弹窗确认
  if (jsonDirty.value) {
    try {
      await ElMessageBox.confirm(
        '侧边栏 JSON 配置有未应用的修改，是否先应用再保存？',
        '提示',
        { confirmButtonText: '应用并保存', cancelButtonText: '仅保存（丢弃 JSON 修改）', type: 'warning' },
      )
      if (!applySidebarJson()) return
    } catch {
      // 用户选择"仅保存"，继续保存但不应用 JSON
    }
  }

  // 报告名称必填
  if (!reportName.value.trim()) {
    ElMessage.warning('请输入报告名称')
    return
  }

  // SQL 模板必填
  if (!sqlTemplate.value.trim()) {
    ElMessage.warning('请输入 SQL 语句')
    return
  }

  const missingAliases = validateSqlAliases(sqlTemplate.value)
  if (missingAliases.length > 0) {
    ElMessage.warning({
      message: `以下 SELECT 列缺少别名，建议添加别名后再保存：${missingAliases.join('、')}`,
      duration: 5000,
    })
    return
  }

  const body = buildSaveRequest(reportName.value, category.value)
  try {
    console.log('body', body)
    const { data } = await AReport.save(body)
    // 新建时 body.tableKey 为空，后端生成真实值（如 "rpt_5"）返回在 data 字段
    const finalKey = data || body.report.tableKey
    if (!finalKey) {
      ElMessage.error('保存失败：未获取到报表标识')
      return
    }
    currentTableKey.value = finalKey
    currentReportId.value = body.report.id

    // 同步 URL（replace 不产生历史记录，支持分享和刷新）
    router.replace({ query: { tableKey: finalKey } })

    lastSavedTime.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    ElMessage.success('报告已保存')
  } catch (err) {
    console.error('保存失败', err)
    ElMessage.error('保存失败')
  }
}

// ==================== 未保存提示 & 快捷键 ====================

function onBeforeUnload(e: BeforeUnloadEvent) {
  if (hasUnsavedChange()) e.preventDefault()
}

function onKeydown(e: KeyboardEvent) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
    e.preventDefault()
    execute()
  }
}

onMounted(async () => {
  document.addEventListener('keydown', onKeydown)
  window.addEventListener('beforeunload', onBeforeUnload)

  // URL 参数加载已有报告
  const tk = route.query.tableKey as string
  if (tk) {
    try {
      const { data } = await AReport.get(tk)
      if (data) {
        loadReport(data)
        lastSavedTime.value = new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
      }
    } catch (err) {
      console.error('加载报告失败', err)
      ElMessage.error('加载报告失败')
    }
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  window.removeEventListener('beforeunload', onBeforeUnload)
})
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
}

.aside-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.aside-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
  flex-shrink: 0;
}

.aside-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.unsaved-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e6a23c;
  flex-shrink: 0;
}

.aside-header-actions {
  display: flex;
  gap: 4px;
}

.aside-editor {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
</style>
