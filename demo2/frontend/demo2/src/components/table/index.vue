<template>
  <div class="u-table">
    <!-- 表格导航工具栏 -->
    <div class="u-table-toolbar">
      <Search
        v-if="config.search"
        :key="searchKey"
        :config="config.search"
        :load-options="loadOptions"
        :show-admin-btn="showAdminBtn"
        @save-filter="onSearchSave"
        @search="onSearchQuery"
        @admin-confirm="onSearchAdminConfirm"
      />
      <div class="u-table-toolbar__right">
        <el-button
          v-if="props.export"
          class="u-export-btn"
          :loading="exporting"
          :icon="Download"
          :title="exporting ? (exportLoadingText || '导出中...') : (exportText || '导出')"
          @click="handleExport"
        />
        <TableSettings
          :config="config"
          :show-admin-btn="showAdminBtn"
          @confirm="onSettingConfirm"
          @admin-confirm="onAdminConfirm"
          @reset="onSettingReset"
        />
      </div>
    </div>

    <div ref="tableWrapperRef" class="u-table-wrapper">
      <el-table
        ref="tableRef"
        :key="reorderKey"
        :data="tableRows"
        :stripe="config.stripe"
        :size="config.size"
        :height="config.height"
        :max-height="config.maxHeight"
        :row-key="config.rowKey"
        :header-cell-style="headerCellStyle"
        @selection-change="(val: any[]) => { emit('selection-change', val); updateSelection?.(val) }"
        @row-dblclick="(row: any, column: any, event: Event) => { crudEditRow?.(row); emit('row-dblclick', row, column, event) }"
      >
        <el-table-column v-if="selection" type="selection" width="50" />
        <template v-for="(item, index) in config.columns" :key="item.prop || index">
          <el-table-column
            v-if="!item.hidden"
          :type="item.type"
          :label="item.label"
          :prop="item.prop"
          :width="item.width"
          :min-width="item.minWidth"
          :resizable="item.resizable !== false"
          :align="item.align"
          :header-align="item.align === 'right' ? 'center' : item.align"
          :fixed="item.fixed"
          show-overflow-tooltip
        >
          <template #header v-if="item.prop && item.sortable !== false">
            <div
              class="u-sort-header"
              @mouseenter="hoverSort = item.prop!"
              @mouseleave="hoverSort = null"
            >
              <span @click.stop="onSort(item.prop!)">{{ item.label }}</span>
              <span class="u-caret-wrapper" :class="{ visible: query.sortColumn === item.prop || hoverSort === item.prop }">
                <span class="u-caret-hit" @click.stop="onSort(item.prop!, 'asc')"><i class="u-sort-caret ascending" :class="{ active: query.sortColumn === item.prop && query.sortDirection === 'asc' }"></i></span>
                <span class="u-caret-hit" @click.stop="onSort(item.prop!, 'desc')"><i class="u-sort-caret descending" :class="{ active: query.sortColumn === item.prop && query.sortDirection === 'desc' }"></i></span>
              </span>
            </div>
          </template>
          <template #default="scope" v-if="item.type !== 'index'">
            <slot :name="item.prop" :row="scope.row" :index="scope.$index" :value="scope.row[item.prop!]">
              <template v-if="item.format === 'tag'">
                <el-tag
                  :type="getTagType(item.prop!, scope.row[item.prop!])"
                  size="small"
                >
                  {{ translateValue(item.prop, scope.row[item.prop!]) }}
                </el-tag>
              </template>
              <template v-else-if="item.format === 'dot'">
                <span class="u-dot-cell">
                  <span
                    class="u-dot"
                    :style="{ background: getDotColor(item.prop!, scope.row[item.prop!]) }"
                  />{{ translateValue(item.prop, scope.row[item.prop!]) }}
                </span>
              </template>
              <template v-else>
                {{ translateValue(item.prop, scope.row[item.prop!]) }}
              </template>
            </slot>
          </template>
        </el-table-column>
        </template>
      </el-table>
      <!-- 分页器：仅当 data 为 PageResult 时自动显示 -->
      <div v-if="isPaginated" class="u-table-pagination">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="totalCount"
          :page-sizes="pageSizes"
          layout="total, sizes, prev, pager, next, jumper"
          small
          background
          @current-change="emitQuery"
          @size-change="onSizeChange"
        />
      </div>
      <TableOverlay :resize="resizeState" :reorder="reorderState" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, inject, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { useColumnResize } from './hooks/useColumnResize'
import { useColumnReorder } from './hooks/useColumnReorder'
import TableSettings from './TableSettings.vue'
import TableOverlay from './TableOverlay.vue'
import Search, { type SearchConfig, type FilterResult } from './search/index.vue'
import { useTableInit } from './hooks/useTableInit'
import type { TableQuery, PageResult, OptionItem, FilterItem } from './types'


/** 列配置 */
export interface TableItem {
  label?: string
  prop?: string
  width?: number | string
  minWidth?: number | string
  align?: 'left' | 'center' | 'right'
  type?: 'index'
  fixed?: 'left' | 'right' | boolean
  resizable?: boolean
  sortable?: boolean | 'custom'
  hidden?: boolean

  /** 列数据类型：声明后自动启用选项翻译。select=静态，remote-select=动态 */
  fieldType?: 'text' | 'select' | 'remote-select'

  /** 远程选项加载标识（fieldType='remote-select' 时有效），作为 loadOptions(type) 的 type 参数 */
  optionType?: string

  /** 静态选项（fieldType='select' 时使用） */
  options?: ({ label: string; value: string; style?: any } | string)[]

  /** 单元格显示格式：text=纯文本（默认），tag=标签，dot=圆点+文本 */
  format?: 'text' | 'tag' | 'dot'
}

/** 表格配置（服务端返回，不含 data） */
export interface TableConfig {
  columns: TableItem[]
  stripe?: boolean
  size?: 'large' | 'default' | 'small'
  height?: number | string
  maxHeight?: number | string
  rowKey?: string
  /** 搜索配置（含默认排序） */
  search?: SearchConfig
  /** 默认排序 */
  sort?: { column: string; direction: 'asc' | 'desc' }
  /** 全局选项映射 { prop → OptionItem[] }，跨 columns/search 共享翻译 */
  optionsMap?: Record<string, OptionItem[]>
}

/** 导出参数 */
export interface ExportParams {
  columns: Pick<TableItem, 'prop' | 'label' | 'width' | 'minWidth' | 'align' | 'fieldType' | 'optionType'>[]
  filter: FilterItem[]
  sort?: { column: string; direction: string }
}

const props = defineProps<{
  config: TableConfig
  /** 表格数据：纯数组（无分页）或 PageResult { list, total }（自动显示分页器） */
  data: Record<string, any>[] | PageResult
  /** 是否显示多选列 */
  selection?: boolean
  showAdminBtn?: boolean
  /** 表格唯一标识，不传则沿用现有行为（向后兼容） */
  id?: string
  /** 选项加载器：type=选项类别，keyword=搜索关键词(可选)。用于表格翻译预加载和搜索下拉 */
  loadOptions?: (type: string, keyword?: string) => Promise<{ label: string; value: string }[]>
  /** 导出 API：传入即显示导出按钮，不传不显示 */
  'export'?: (params: ExportParams) => Promise<boolean | void>
  /** 导出按钮文案，默认"导出" */
  exportText?: string
  /** 导出中文案，默认"导出中..." */
  exportLoadingText?: string
}>()

// ==================== 智能 data 派生 ====================

/** 判断 data 是否是分页对象（有 list + total） */
const isPaginated = computed(() =>
  props.data !== null && typeof props.data === 'object' && !Array.isArray(props.data),
)

/** 实际表格行数据 */
const tableRows = computed(() =>
  Array.isArray(props.data) ? props.data : (props.data as PageResult).list ?? [],
)

/** 分页总数 */
const totalCount = computed(() =>
  Array.isArray(props.data) ? 0 : (props.data as PageResult).total ?? 0,
)

// ==================== 延迟 loading：防止快速渲染闪烁 ====================

const loadingDebounceTimer = ref<ReturnType<typeof setTimeout>>()
const debouncedLoading = ref(false)

// ==================== 内部 loading 管理（done 回调模式） ====================

/** 启动内部 loading（200ms 防抖），返回 finish 函数。父组件在数据就绪后调用 done() 关闭 loading。 */
function startInternalLoading(): () => void {
  clearTimeout(loadingDebounceTimer.value)
  loadingDebounceTimer.value = setTimeout(() => {
    debouncedLoading.value = true
  }, 200)
  return () => {
    clearTimeout(loadingDebounceTimer.value)
    debouncedLoading.value = false
  }
}

// ==================== 页码大小持久化（localStorage） ====================

const pageSizes = [10, 20, 50, 100]

/** 读取持久化的 pageSize，无 id 或无存储记录时返回默认值 10 */
function getPersistedPageSize(): number {
  if (!props.id) return 10
  try {
    const raw = localStorage.getItem(`table:pageSize:${props.id}`)
    if (raw !== null) {
      const val = Number(raw)
      if (Number.isInteger(val) && pageSizes.includes(val)) return val
    }
  } catch { /* ignore */ }
  return 10
}

/** 写入持久化的 pageSize */
function persistPageSize(size: number) {
  if (!props.id) return
  try {
    localStorage.setItem(`table:pageSize:${props.id}`, String(size))
  } catch { /* ignore */ }
}

// ==================== 统一查询参数（运行时状态，与 config 分离，不持久化） ====================

const query = reactive({
  /** 当前页 (1-based) */
  current: 1,
  /** 每页条数 */
  size: getPersistedPageSize(),
  /** 筛选条件 */
  filter: [] as FilterResult[],
  /** 排序字段 */
  sortColumn: null as string | null,
  /** 排序方向 */
  sortDirection: null as 'asc' | 'desc' | null,
})

/** 控制 Search 组件重挂载（切换报表 / 系统默认恢复时 +1），强制重新 JSON 深拷贝蓝图 */
const searchKey = ref(0)

// 从 config 初始化 query.filter 和 query.sort（仅首次，后续手动触发）
function initQueryFromConfig(cfg?: TableConfig) {
  const config = cfg || props.config
  const defaults: FilterResult[] = []
  config.search?.filter.forEach((col) => {
    const v = col.value
    if (v === undefined || v === null) return
    if (typeof v === 'string' && v === '') return
    if (Array.isArray(v) && v.length === 0) return
    defaults.push({
      column: col.prop,
      operator: (col.operator || 'contains') as FilterResult['operator'],
      value: v as FilterResult['value'],
    })
  })
  query.filter = defaults.length ? defaults : []

  const s = config.sort
  if (s?.column && s?.direction) {
    query.sortColumn = s.column
    query.sortDirection = s.direction
  } else {
    query.sortColumn = null
    query.sortDirection = null
  }
}

initQueryFromConfig()

// 浅层 watch：config 对象引用被整体替换时，重置 query 状态（不自动查询，由外部显式触发）
watch(
  () => props.config,
  (newConfig) => {
    if (!newConfig?.columns?.length) return
    query.current = 1
    query.size = getPersistedPageSize()
    initQueryFromConfig(newConfig)
    searchKey.value++  // 强制 Search 组件重挂载，重新 JSON 深拷贝蓝图
  },
)

/** 排序逻辑：sort(prop) 循环 asc→desc→none；sort(prop, 'asc'/'desc') 直指方向 */
function doSort(prop: string, target?: 'asc' | 'desc') {
  if (!target) {
    if (query.sortColumn === prop) {
      if (query.sortDirection === 'asc') { query.sortDirection = 'desc' }
      else if (query.sortDirection === 'desc') { query.sortColumn = null; query.sortDirection = null }
      else { query.sortDirection = 'asc' }
    } else {
      query.sortColumn = prop; query.sortDirection = 'asc'
    }
  } else {
    if (query.sortColumn === prop && query.sortDirection === target) {
      query.sortColumn = null; query.sortDirection = null
    } else {
      query.sortColumn = prop; query.sortDirection = target
    }
  }
  query.current = 1

  // 将排序状态写回 config，供持久化
  if (query.sortColumn && query.sortDirection) {
    props.config.sort = { column: query.sortColumn, direction: query.sortDirection }
  } else {
    delete props.config.sort
  }

  emit('change', props.config)
  emitQuery()
}

// ==================== 导出 ====================

const exporting = ref(false)

/** 清洗 columns：仅保留后端需要的字段 */
function cleanColumns(cols: TableItem[]) {
  return cols.map(({ prop, label, width, minWidth, align, fieldType, optionType }) =>
    ({ prop, label, width, minWidth, align, fieldType, optionType }),
  )
}

/** 清洗空值 filter */
function cleanFilter(filter: FilterItem[]): FilterItem[] {
  return filter.filter(f => {
    const v = f.value
    if (Array.isArray(v)) return v.length > 0 && v.some((e: any) => e !== '')
    return v !== '' && v != null
  })
}

async function handleExport() {
  if (!props['export'] || exporting.value) return
  exporting.value = true
  try {
    const visibleCols = props.config.columns.filter(c => !c.hidden)
    const filter = cleanFilter(
      query.filter
        .filter(fv => !!fv.column)
        .map(fv => ({
          column: fv.column,
          operator: fv.operator,
          value: fv.value as FilterItem['value'],
        })),
    )
    await props['export']({
      columns: cleanColumns(visibleCols),
      filter,
      sort: query.sortColumn && query.sortDirection
        ? { column: query.sortColumn, direction: query.sortDirection }
        : undefined,
    })
    ElMessage.success('导出成功')
  } catch {
    // 错误提示由父组件处理
  } finally {
    exporting.value = false
  }
}

/** 删完最后一页 → 自动回退并重新请求 */
watch(totalCount, (t) => {
  if (!isPaginated.value) return
  const max = Math.ceil(t / query.size) || 1
  if (query.current > max) {
    query.current = max
    nextTick(() => emitQuery())
  }
})

const emit = defineEmits<{
  (e: 'selection-change', value: any[]): void
  (e: 'row-dblclick', row: any, column: any, event: Event): void
  /** 配置变更（列宽、筛选、排序配置等），返回完整 config；isAdmin=true 时表示管理员保存系统默认 */
  (e: 'change', config: TableConfig, isAdmin?: boolean): void
  /** 恢复系统默认 */
  (e: 'reset'): void
  /** 查询参数变化（筛选/排序/分页），payload 对齐后端 FilterParam。done() 必调以关闭 loading */
  (e: 'query', param: TableQuery, done: () => void): void
}>()

/** 注入 crud 的 selection 同步函数（如果父级是 crud 组件） */
const updateSelection = inject<((rows: any[]) => void) | null>('crud:updateSelection', null)

/** 向 crud 注册 tableRef，供 crud 调用 clearSelection 等方法 */
const registerTableRef = inject<((ref: any) => void) | null>('crud:registerTableRef', null)

/** 向 crud 注册 Table 组件实例，供 crud 调用 reload 刷新列表 */
const registerTableInstance = inject<((instance: any) => void) | null>('crud:registerTableInstance', null)

/** 注入 crud 的编辑行函数（单击行时打开修改对话框） */
const crudEditRow = inject<((row: any) => void) | null>('crud:editRow', null)

/** 向 crud 上报当前查询参数，供导出/导入使用 */
const reportQuery = inject<((q: TableQuery) => void) | null>('crud:reportQuery', null)

const tableRef = ref()
const tableWrapperRef = ref<HTMLElement>()
const reorderKey = ref(0) // 排序后 +1 强制重绘

/** 刷新当前页数据（供 Crud 组件调用的统一入口） */
function reload() {
  emitQuery()
}

onMounted(() => {
  registerTableRef?.(tableRef)
  registerTableInstance?.({ reload })
})

// ==================== 初始化（onMounted 逻辑）====================

const { internalLookup, internalStyles } = useTableInit({
  config: props.config,
  tableRows,
  tableWrapperRef,
  loadOptions: props.loadOptions,
  emitQuery,
})

const isFixedCol = (item: TableItem) => !!item.fixed

// 动态列引用，config 整体替换后仍响应
const columnsRef = computed(() => props.config.columns)

// 列宽拖拽
const resizeState = reactive(useColumnResize({
  wrapperRef: tableWrapperRef,
  columns: columnsRef,
  edgeThreshold: 12,
  onResizeEnd: () => { emit('change', props.config) },
}))

// 列排序拖拽
const reorderState = reactive(useColumnReorder({
  wrapperRef: tableWrapperRef,
  columns: columnsRef,
  isFixed: isFixedCol,
  onReorder: () => {
    reorderKey.value++
    emit('change', props.config)
  },
}))

const hoverSort = ref<string | null>(null)



// 统一的查询事件构建
function emitQuery() {
  // 按列去重，同名列保留最后一个值（避免外部拼接导致重复）；过滤空值
  const dedupMap = new Map<string, FilterItem>()
  for (const fv of query.filter) {
    if (fv.column) {
      dedupMap.set(fv.column, {
        column: fv.column,
        operator: fv.operator,
        value: fv.value as FilterItem['value'],
      })
    }
  }
  const filter: FilterItem[] = [...dedupMap.values()].filter(f => {
    const v = f.value
    if (Array.isArray(v)) return v.length > 0 && v.some((e: any) => e !== '')
    return v !== '' && v !== null && v !== undefined
  })
  const visibleColumns = props.config.columns.filter(c => !c.hidden)
  const payload: TableQuery = {
    current: query.current,
    size: query.size,
    filter,
    sort: query.sortColumn && query.sortDirection
      ? { column: query.sortColumn, direction: query.sortDirection }
      : undefined,
    columns: visibleColumns,
  }
  const done = startInternalLoading()
  emit('query', payload, done)
  reportQuery?.(payload)
}

// 搜索配置变更 → 持久化（仅 filter/currentField 等元数据，不含 filterValues）
function onSearchSave(_cfg: SearchConfig) {
  emit('change', props.config)
}

// 搜索查询 → 更新 query.filter，同步 config 默认值，调 API
function onSearchQuery(filters: FilterResult[]) {
  query.filter = filters
  query.current = 1

  // 同步 config 默认值：搜索条件覆盖 config.search.filter 中对应列的 value
  const searchMap = new Map(filters.filter(f => f.column).map(f => [f.column, f]))
  if (props.config.search?.filter) {
    for (const col of props.config.search.filter) {
      const sf = searchMap.get(col.prop)
      if (sf !== undefined) {
        col.value = sf.value
        col.operator = sf.operator as any
      }
    }
  }

  emitQuery()
}

function onSort(prop: string, target?: 'asc' | 'desc') {
  doSort(prop, target)
}

// ==================== 分页事件 ====================

/** 每页条数变化 → 已在 v-model 中更新，重置到第一页 */
function onSizeChange(_size: number) {
  persistPageSize(_size)
  query.current = 1

  // 将排序状态写回 config，供持久化
  if (query.sortColumn && query.sortDirection) {
    props.config.sort = { column: query.sortColumn, direction: query.sortDirection }
  } else {
    delete props.config.sort
  }

  emit('change', props.config)
  emitQuery()
}

/** 根据 internalLookup 翻译单元格值，未命中返回原值 */
function translateValue(prop: string | undefined, rawValue: any): any {
  if (!prop || rawValue == null) return rawValue
  const dict = internalLookup.value[prop]
  return dict ? (dict[String(rawValue)] ?? rawValue) : rawValue
}

const TAG_TYPES = ['primary', 'success', 'warning', 'danger', 'info'] as const
const DOT_COLORS = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']

/** autoStyle 结果缓存，避免每单元格重复 hash 计算 */
const styleCache = new Map<string, { tagType: typeof TAG_TYPES[number]; dotColor: string }>()

/** 根据字符串 hash 自动分配一个固定的 type/color，同一 value 始终得到同一颜色 */
function autoStyle(v: string) {
  const cached = styleCache.get(v)
  if (cached) return cached
  const hash = [...v].reduce((h, c) => h + c.charCodeAt(0), 0)
  const idx = Math.abs(hash) % TAG_TYPES.length
  const result = { tagType: TAG_TYPES[idx], dotColor: DOT_COLORS[idx] }
  styleCache.set(v, result)
  return result
}

/** 获取指定值对应的 el-tag type（显式 style > 自动 fallback） */
function getTagType(prop: string, rawValue: any): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  if (rawValue == null) return 'info'
  const styleMap = internalStyles.value[prop]
  if (styleMap) {
    const s = styleMap[String(rawValue)]
    if (s?.tagType) return s.tagType
  }
  return autoStyle(String(rawValue)).tagType
}

/** 获取指定值对应的 dot 颜色（显式 style > 自动 fallback） */
function getDotColor(prop: string, rawValue: any): string {
  if (rawValue == null) return '#ccc'
  const styleMap = internalStyles.value[prop]
  if (styleMap) {
    const s = styleMap[String(rawValue)]
    if (s?.dotColor) return s.dotColor
  }
  return autoStyle(String(rawValue)).dotColor
}

const headerCellStyle = () => ({
  background: '#f5f7fa',
  color: '#606266',
})



// 列设置面板事件
const onSettingConfirm = (cols: TableItem[]) => {
  props.config.columns.splice(0, props.config.columns.length)
  cols.forEach(c => props.config.columns.push(c))
  reorderKey.value++
  emit('change', props.config)
}

const onAdminConfirm = (cols: TableItem[]) => {
  props.config.columns.splice(0, props.config.columns.length)
  cols.forEach(c => props.config.columns.push(c))
  reorderKey.value++
  emit('change', props.config, true)
}

/** 搜索面板"保存为系统默认" → 触发 change(isAdmin=true) 到父组件 */
function onSearchAdminConfirm() {
  emit('change', props.config, true)
}

const onSettingReset = (cols: TableItem[]) => {
  props.config.columns.splice(0, props.config.columns.length)
  cols.forEach(c => props.config.columns.push(c))
  reorderKey.value++
  searchKey.value++  // 强制 Search 重挂载，以新 columns 重新 JSON 深拷贝初始化 conditions
  query.filter = []
  emit('reset')
}

defineExpose({ tableRef, reload, refresh: emitQuery })
</script>

<style lang="scss" scoped>
.u-table {
  width: 100%;
}

.u-table-toolbar {
  display: flex;
  justify-content: space-between;
  min-height: 36px;
  padding: 4px 6px;

  &__right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.u-export-btn {
  height: 28px !important;
  width: 28px !important;
  padding: 0 !important;
  min-width: unset !important;
}

.u-table-wrapper {
  width: 100%;
  position: relative;
  overflow: auto;

  // 单元格文字溢出省略号
  :deep(.el-table__cell) {
    .cell {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  // 表头默认 grab 光标（中间区域可拖拽排序）
  :deep(.el-table__header th) {
    position: relative;
    cursor: grab;

    &::after {
      content: '';
      position: absolute;
      right: 0;
      top: 0;
      width: 12px;
      height: 100%;
      cursor: col-resize;
      z-index: 10;
    }
  }

  // 固定列表头使用默认箭头
  :deep(.el-table-fixed-column--left),
  :deep(.el-table-fixed-column--right),
  :deep(.el-table__fixed-right .el-table__header th) {
    cursor: default !important;

    &::after {
      cursor: col-resize !important;
    }
  }

  // 拖拽进行中 → 默认光标 + 列名提示
  &.reordering :deep(.el-table__header th) {
    cursor: default !important;
    &::after { cursor: default !important; }
  }
  // 列宽拖拽中 → col-resize 光标
  &.resizing :deep(.el-table__header th) {
    cursor: col-resize !important;
    &::after { cursor: col-resize !important; }
  }

  :deep(.dragging) {
    opacity: 0.4;
  }

  // 排序表头（Element Plus 同款）
  :deep(.u-sort-header) {
    display: inline-flex;
    align-items: center;
    line-height: 1;

    > span:first-child {
      cursor: pointer;
      display: flex;
      align-items: center;
    }
  }

  :deep(.u-caret-wrapper) {
    display: inline-flex;
    flex-direction: column;
    align-items: center;
    vertical-align: middle;
    height: 14px;
    width: 24px;
    cursor: pointer;
    overflow: initial;
    position: relative;
    opacity: 0;
    transition: opacity 0.15s;

    &.visible {
      opacity: 1;
    }
  }

  :deep(.u-caret-hit) {
    position: absolute;
    left: 0;
    width: 24px;
    height: 7px;
    cursor: pointer;

    &:first-child { top: 0; }
    &:last-child { bottom: 0; }
  }

  :deep(.u-sort-caret) {
    width: 0;
    height: 0;
    border: solid 5px transparent;
    position: absolute;
    left: 5px;

    &.ascending {
      border-bottom-color: #c0c4cc;
      top: -4px;

      &.active {
        border-bottom-color: #409eff;
      }
    }

    &.descending {
      border-top-color: #c0c4cc;
      bottom: -5px;

      &.active {
        border-top-color: #409eff;
      }
    }
  }
}

.u-table-pagination {
  display: flex;
  justify-content: flex-start;
  padding: 12px 6px 4px;
  border: 1px solid #ebeef5;
  border-top: none;
  border-radius: 0 0 4px 4px;
  background: #fafafa;
}

/* dot 单元格样式 */
.u-dot-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.u-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>
