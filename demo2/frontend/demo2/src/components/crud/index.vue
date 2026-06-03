<template>
  <div class="u-crud">
    <!-- 顶部工具栏 — 右上角布局 -->
    <div class="u-crud-toolbar">
      <div class="u-crud-toolbar__actions">
        <!-- 新增 -->
        <el-button
          v-if="crud.save"
          class="u-small"
          type="primary"
          :disabled="crud.disable?.includes('save')"
          @click="openForm()"
        >
          <el-icon><Plus /></el-icon>
          新增
        </el-button>

        <!-- 更多操作（三点按钮 → Popover） -->
        <el-popover
          v-if="crud.import || crud.export"
          trigger="click"
          placement="bottom-end"
          :width="140"
          :offset="4"
          popper-class="u-crud-popover"
        >
          <template #reference>
            <el-button class="u-small">
              <el-icon><MoreFilled /></el-icon>
            </el-button>
          </template>

          <div class="u-crud-popover-menu">
            <div
              v-if="crud.import"
              class="u-crud-popover-menu__item"
              @click="openImport"
            >
              <el-icon><Upload /></el-icon>
              <span>导入</span>
            </div>
            <div
              v-if="crud.export"
              class="u-crud-popover-menu__item"
              :class="{ 'is-disabled': exporting }"
              @click="handleExport"
            >
              <el-icon v-if="!exporting"><Download /></el-icon>
              <el-icon v-else class="is-loading"><Loading /></el-icon>
              <span>{{ exporting ? '导出中...' : '导出' }}</span>
            </div>
          </div>
        </el-popover>

        <!-- 自定义扩展按钮 -->
        <slot name="toolbar-actions" />
      </div>
    </div>

    <!-- 表格卡槽 -->
    <div ref="bodyRef" class="u-crud-body">
      <slot />
    </div>

    <!-- 表单抽屉 -->
    <FormDrawer
      v-model:visible="formVisible"
      :row="formRow"
      :crud="crud"
      :form-items="formItems"
      :load-options="loadOptions"
      :width="formWidth"
      @saved="onSaved"
    />

    <!-- 导入对话框 -->
    <ImportDialog
      v-model:visible="importVisible"
      :template-api="handleDownloadTemplate"
      :upload-api="handleImportUpload"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, provide, nextTick } from 'vue'
import { Plus, MoreFilled, Upload, Download, Loading } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import type { CrudApi, FormItemConfig } from './types'
import type { TableQuery } from '@/types/table'
import FormDrawer from './FormDrawer.vue'
import ImportDialog from './ImportDialog.vue'

const props = defineProps<{
  /** CRUD API 配置 - reactive 对象，方法定义即显示对应按钮 */
  crud: CrudApi
  /** 表单字段配置 */
  formItems?: FormItemConfig[]
  /** 远程选项加载器，复用 optionsStore */
  loadOptions?: (type: string, keyword?: string) => Promise<{ label: string; value: string }[]>
  /** 表单抽屉宽度 */
  formWidth?: string | number
}>()

defineSlots<{
  default(): any
  'toolbar-actions'?(): any
}>()

const emit = defineEmits<{
  saved: []
}>()

// ==================== Selection 状态 ====================

const selectedRows = ref<any[]>([])
const selectedCount = ref(0)
let barEl: HTMLDivElement | null = null

/** table 注入此函数，selection-change 时同步通知 crud */
provide('crud:updateSelection', (rows: any[]) => {
  selectedRows.value = rows
  selectedCount.value = rows.length
  updateBar()
})

// ==================== 操作 table 实例 ====================

const bodyRef = ref<HTMLElement>()
let _tableRef: any = null

/** table 注入此函数，注册自己的 tableRef 供 crud 调用 clearSelection 等方法 */
provide('crud:registerTableRef', (ref: any) => {
  _tableRef = ref
})

/** Table 组件实例（含 reload 方法），用于操作完成后刷新列表 */
let _tableInstance: any = null

provide('crud:registerTableInstance', (instance: any) => {
  _tableInstance = instance
})

/** 统一刷新方法：触发 Table 的 reload → emitQuery → 父组件 @query → fetchList */
function refresh() {
  _tableInstance?.reload?.()
}

/** table 注入此函数，双击行时触发 → crud 打开修改对话框（仅当 crud.update 定义时注入） */
if (props.crud.update) {
  provide('crud:editRow', (row: Record<string, any>) => {
    openForm(row)
  })
}

function getTableElRef() {
  return _tableRef
}

function clearTableSelection() {
  _tableRef?.value?.clearSelection()
}

// ==================== 批量删除 ====================

async function handleBatchDelete() {
  const count = selectedCount.value
  if (count === 0) {
    ElMessage.warning('请先选择要删除的数据')
    return
  }

  const ids = selectedRows.value.map((r: any) => r.id)

  try {
    await ElMessageBox.confirm(
      `确定删除选中的 ${count} 条数据？`,
      '批量删除',
      { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' },
    )
  } catch {
    return // 用户取消
  }

  // 使用 crud.remove
  props.crud.beforeRemove?.(ids)
  props.crud.remove!(ids, (success = true) => {
    if (success) {
      ElMessage.success('删除成功')
      clearTableSelection()
      refresh()
    } else {
      ElMessage.error('删除失败')
    }
  })
}

// ==================== 表单抽屉 ====================

const formVisible = ref(false)
const formRow = ref<Record<string, any> | null>(null)

function openForm(row?: Record<string, any>) {
  formRow.value = row ?? null
  formVisible.value = true
}

/** FormDrawer 保存成功后 → 刷新列表 + 通知父组件 */
function onSaved() {
  refresh()
  emit('saved')
}

// ==================== Query 缓存（Table 通过 inject 上报） ====================

const currentQuery = ref<TableQuery | null>(null)

provide('crud:reportQuery', (q: TableQuery) => {
  currentQuery.value = q
})

// ==================== Import ====================

const importVisible = ref(false)

function openImport() {
  importVisible.value = true
}

async function handleDownloadTemplate() {
  if (!props.crud.downloadTemplate) return
  await props.crud.downloadTemplate()
}

async function handleImportUpload(formData: FormData) {
  const { crud } = props
  if (!crud.import) throw new Error('import not defined')

  const file = formData.get('file') as File
  const config = {
    columns: cleanColumns(currentQuery.value?.columns ?? []),
    filter: currentQuery.value?.filter ?? [],
  }

  return new Promise<any>((resolve, reject) => {
    crud.import!({ file, config }, (success, data) => {
      if (success) {
        resolve(data ?? { code: 0 })
        refresh()
      } else {
        reject(new Error('导入失败'))
      }
    })
  })
}

// ==================== Export ====================

const exporting = ref(false)

/** 清洗 columns：仅保留后端需要的字段，去除 format/hidden/type 等纯前端属性 */
function cleanColumns(columns: any[]) {
  return columns.map(({ prop, label, width, minWidth, align, fieldType, optionType }) =>
    ({ prop, label, width, minWidth, align, fieldType, optionType }),
  )
}

/** 清洗空值 filter */
function cleanFilter(filter: any[]) {
  return filter.filter(f => {
    const v = f.value
    if (Array.isArray(v)) return v.length > 0 && v.some((e: any) => e !== '')
    return v !== '' && v != null
  })
}

async function handleExport() {
  const { crud } = props
  if (!crud.export || exporting.value) return
  if (!currentQuery.value) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  exporting.value = true
  try {
    const filter = cleanFilter(currentQuery.value.filter ?? [])
    await crud.export({
      columns: cleanColumns(currentQuery.value.columns),
      filter,
      sort: currentQuery.value.sort ?? {},
    })
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// ==================== DOM 插入 selection-bar ====================

function createBar() {
  const elTable = bodyRef.value?.querySelector('.el-table') as HTMLElement | null
  if (!elTable) {
    setTimeout(createBar, 50)
    return
  }

  barEl = document.createElement('div')
  barEl.className = 'u-crud-selection-bar'
  // 全内联样式，确保 100% 生效
  Object.assign(barEl.style, {
    display: 'none',
    position: 'relative',
    zIndex: '1',
    alignItems: 'center',
    justifyContent: 'flex-start',
    gap: '16px',
    minHeight: '40px',
    padding: '8px 16px',
    border: '1px solid #b3d8ff',
    borderBottom: 'none',
    borderRadius: '6px 6px 0 0',
    background: '#ecf5ff',
  })
  const hasDelete = !!props.crud.remove

  barEl.innerHTML = `
    <span style="font-size:14px;color:#606266;">
      已选择 <span class="u-crud-selection-bar__count" style="margin:0 4px;font-weight:600;color:#409eff;">0</span> 项
    </span>
    <div class="u-crud-selection-bar__actions" style="display:flex;align-items:center;gap:8px;">
      <button class="u-crud-selection-bar__btn u-crud-selection-bar__btn--cancel" style="height:28px;padding:0 12px;font-size:13px;border:1px solid #dcdfe6;border-radius:4px;background:#fff;color:#606266;cursor:pointer;">取消选择</button>
      ${hasDelete ? '<button class="u-crud-selection-bar__btn u-crud-selection-bar__btn--delete" style="height:28px;padding:0 12px;font-size:13px;border:1px solid #f56c6c;border-radius:4px;background:#fef0f0;color:#f56c6c;cursor:pointer;">删除</button>' : ''}
    </div>
  `

  elTable.parentNode!.insertBefore(barEl, elTable)

  // 事件绑定：用 class 选择器
  ;(barEl.querySelector('.u-crud-selection-bar__btn--cancel') as HTMLElement)?.addEventListener('click', clearTableSelection)
  if (hasDelete) {
    ;(barEl.querySelector('.u-crud-selection-bar__btn--delete') as HTMLElement)?.addEventListener('click', handleBatchDelete)
  }
}

function updateBar() {
  if (!barEl) return
  if (selectedCount.value > 0) {
    barEl.style.display = 'flex'
    barEl.querySelector('.u-crud-selection-bar__count')!.textContent = String(selectedCount.value)
  } else {
    barEl.style.display = 'none'
  }
}

onMounted(() => {
  nextTick(() => {
    createBar()
  })
})

onUnmounted(() => {
  barEl?.remove()
})

defineExpose({ setSelection: updateBar })
</script>

<style lang="scss" scoped>
.u-crud {
  width: 100%;
}

.u-crud-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-height: 44px;
  padding: 6px 12px;
  margin-bottom: 4px;
  border-radius: 6px;
  background: #fff;

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

/* popover 菜单样式 */
.u-crud-popover-menu {
  &__item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    font-size: 14px;
    cursor: pointer;
    border-radius: 4px;
    transition: background-color 0.2s;

    &:hover {
      background: #f0f2f5;
    }

    &.is-disabled {
      cursor: not-allowed;
      opacity: 0.6;

      &:hover {
        background: transparent;
      }
    }

    .el-icon {
      font-size: 16px;
      color: #606266;

      &.is-loading {
        animation: u-crud-rotate 1s linear infinite;
      }
    }
  }
}

@keyframes u-crud-rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>

<!-- selection-bar / popover 样式（非 scoped，因为 DOM 挂载到 body） -->
<style lang="scss">
/* el-popover 去除默认内边距 */
.u-crud-popover {
  padding: 4px 0 !important;
}

.u-crud-selection-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 40px;
  padding: 8px 16px;
  border: 1px solid #b3d8ff;
  border-bottom: none;
  border-radius: 6px 6px 0 0;
  background: #ecf5ff;

  &__left {
    font-size: 14px;
    color: #606266;
  }

  &__count {
    margin: 0 4px;
    font-weight: 600;
    color: #409eff;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    height: 28px;
    padding: 0 12px;
    font-size: 13px;
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    background: #fff;
    color: #606266;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      color: #409eff;
      border-color: #c6e2ff;
      background: #ecf5ff;
    }

    &--delete {
      color: #f56c6c;
      border-color: #f56c6c;
      background: #fef0f0;

      &:hover {
        color: #fff;
        background: #f56c6c;
      }
    }
  }
}
</style>
