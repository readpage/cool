<template>
  <div class="u-table">
    <!-- 表格导航工具栏 -->
    <div class="u-table-toolbar">
      <Search
        v-if="config.search"
        :config="config.search"
        :load-options="loadOptions"
        :show-admin-btn="showAdminBtn"
        @save-filter="onSearchSave"
        @search="onSearchQuery"
        @admin-confirm="onSearchAdminConfirm"
      />
      <TableSettings
        :config="config"
        :show-admin-btn="showAdminBtn"
        @confirm="onSettingConfirm"
        @admin-confirm="onAdminConfirm"
        @reset="onSettingReset"
      />
    </div>

    <div ref="tableWrapperRef" class="u-table-wrapper">
      <el-table
        ref="tableRef"
        :key="reorderKey"
        v-loading="debouncedLoading"
        :data="tableRows"
        :stripe="config.stripe"
        :size="config.size"
        :height="config.height"
        :max-height="config.maxHeight"
        :row-key="config.rowKey"
        :header-cell-style="headerCellStyle"
        @selection-change="(val: any[]) => { console.log('[table] selection-change 触发, 行数:', val.length, val); emit('selection-change', val); console.log('[table] updateSelection 函数存在?', !!updateSelection); updateSelection?.(val) }"
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
              <span class="u-caret-wrapper" :class="{ visible: sortProp === item.prop || hoverSort === item.prop }">
                <span class="u-caret-hit" @click.stop="onSort(item.prop!, 'asc')"><i class="u-sort-caret ascending" :class="{ active: sortProp === item.prop && sortOrder === 'asc' }"></i></span>
                <span class="u-caret-hit" @click.stop="onSort(item.prop!, 'desc')"><i class="u-sort-caret descending" :class="{ active: sortProp === item.prop && sortOrder === 'desc' }"></i></span>
              </span>
            </div>
          </template>
          <template #default="scope" v-if="item.type !== 'index'">
            <slot :name="item.prop" :row="scope.row" :index="scope.$index" :value="scope.row[item.prop]">
              <template v-if="item.fieldType && item.format === 'tag'">
                <el-tag
                  :type="getTagType(item.prop!, scope.row[item.prop])"
                  size="small"
                >
                  {{ translateValue(item.prop, scope.row[item.prop]) }}
                </el-tag>
              </template>
              <template v-else-if="item.fieldType && item.format === 'dot'">
                <span class="u-dot-cell">
                  <span
                    class="u-dot"
                    :style="{ background: getDotColor(item.prop!, scope.row[item.prop]) }"
                  />{{ translateValue(item.prop, scope.row[item.prop]) }}
                </span>
              </template>
              <template v-else>
                {{ translateValue(item.prop, scope.row[item.prop]) }}
              </template>
            </slot>
          </template>
        </el-table-column>
        </template>
      </el-table>
      <!-- 分页器：仅当 data 为 PageResult 时自动显示 -->
      <div v-if="isPaginated" class="u-table-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="totalCount"
          :page-sizes="pageSizes"
          layout="total, sizes, prev, pager, next, jumper"
          small
          background
          @current-change="emitQuery"
          @size-change="onSizeChange"
        />
      </div>
      <TableOverlay
        :column-highlight-visible="columnHighlightVisible"
        :column-highlight-style="columnHighlightStyle"
        :drag-line-visible="dragLineVisible"
        :drag-line-style="dragLineStyle"
        :preview-line-visible="previewLineVisible"
        :preview-line-style="previewLineStyle"
        :tooltip-visible="tooltipVisible"
        :tooltip-style="tooltipStyle"
        :tooltip-text="tooltipText"
        :drop-line-visible="dropLineVisible"
        :drop-line-style="dropLineStyle"
        :col-highlight-visible="colHighlightVisible"
        :col-highlight-style="colHighlightStyle"
        :reorder-tooltip-visible="reorderTooltipVisible"
        :reorder-tooltip-style="reorderTooltipStyle"
        :reorder-tooltip-label="reorderTooltipLabel"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, inject } from 'vue'
import { useColumnResize } from './hooks/useColumnResize'
import { useColumnReorder } from './hooks/useColumnReorder'
import { useColumnSort } from './hooks/useColumnSort'
import TableSettings from './TableSettings.vue'
import TableOverlay from './TableOverlay.vue'
import Search, { type SearchConfig, type FilterResult } from './search/index.vue'
import { useTableInit } from './hooks/useTableInit'
import type { TableQuery, PageResult, OptionItem } from '@/types/table'

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
  /** 排序配置 { column, direction } */
  sort?: { column: string; direction: 'asc' | 'desc' }
  /** 搜索配置 */
  search?: SearchConfig
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
  /** 加载状态 */
  loading?: boolean
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

watch(
  () => props.loading,
  (val) => {
    if (val) {
      // loading → true：等 200ms 再真正显示
      clearTimeout(loadingDebounceTimer.value)
      loadingDebounceTimer.value = setTimeout(() => {
        debouncedLoading.value = true
      }, 200)
    } else {
      // loading → false：立即隐藏 + 取消等待中的定时器
      clearTimeout(loadingDebounceTimer.value)
      debouncedLoading.value = false
    }
  },
  { immediate: true },
)

// ==================== Table 自管分页状态 ====================

const currentPage = ref(1)
const pageSize = ref(10)
const pageSizes = [10, 20, 50, 100]

/** 删完最后一页 → 自动回退并重新请求 */
watch(totalCount, (t) => {
  if (!isPaginated.value) return
  const max = Math.ceil(t / pageSize.value) || 1
  if (currentPage.value > max) {
    currentPage.value = max
    nextTick(() => emitQuery())
  }
})

const emit = defineEmits<{
  (e: 'selection-change', value: any[]): void
  (e: 'row-dblclick', row: any, column: any, event: Event): void
  /** 配置变更（列宽、筛选、排序配置等），返回完整 config */
  (e: 'change', config: TableConfig): void
  /** 管理员确认默认配置 */
  (e: 'admin-confirm', columns: TableItem[]): void
  /** 恢复系统默认 */
  (e: 'reset'): void
  /** 查询参数变化（筛选/排序/分页），payload 对齐后端 FilterParam */
  (e: 'query', param: TableQuery): void
}>()

/** 注入 crud 的 selection 同步函数（如果父级是 crud 组件） */
const updateSelection = inject<((rows: any[]) => void) | null>('crud:updateSelection', null)

/** 向 crud 注册 tableRef，供 crud 调用 clearSelection 等方法 */
const registerTableRef = inject<((ref: any) => void) | null>('crud:registerTableRef', null)

/** 向 crud 注册 Table 组件实例，供 crud 调用 reload 刷新列表 */
const registerTableInstance = inject<((instance: any) => void) | null>('crud:registerTableInstance', null)

/** 注入 crud 的编辑行函数（单击行时打开修改对话框） */
const crudEditRow = inject<((row: any) => void) | null>('crud:editRow', null)

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
const { dragLineVisible, dragLineStyle, previewLineVisible, previewLineStyle, columnHighlightVisible, columnHighlightStyle, tooltipVisible, tooltipStyle, tooltipText } = useColumnResize({
  wrapperRef: tableWrapperRef,
  columns: columnsRef,
  edgeThreshold: 12,
  onResizeEnd: () => { emit('change', props.config) },
})

// 列排序拖拽
const { dropLineVisible, dropLineStyle, colHighlightVisible, colHighlightStyle, reorderTooltipVisible, reorderTooltipLabel, reorderTooltipStyle } = useColumnReorder({
  wrapperRef: tableWrapperRef,
  columns: columnsRef,
  isFixed: isFixedCol,
  onReorder: () => {
    reorderKey.value++
    emit('change', props.config)
  },
})

// 单列数据排序
const { sortProp, sortOrder, sort } = useColumnSort(props.config)
const hoverSort = ref<string | null>(null)

// 统一的查询事件构建
function emitQuery() {
  const filter = props.config.search?.filterValues ?? []
  // 过滤掉隐藏列，供导出等功能判断需要显示哪些列
  const visibleColumns = props.config.columns.filter(c => !c.hidden)
  emit('query', {
    current: currentPage.value,
    size: pageSize.value,
    filter,
    sort: props.config.sort,
    columns: visibleColumns,
  })
}

// 搜索配置变更 → 持久化
function onSearchSave(_cfg: SearchConfig) {
  emit('change', props.config)
}

// 搜索查询 → 调 API
function onSearchQuery(filters: FilterResult[]) {
  if (props.config.search) {
    props.config.search.filterValues = filters
  }
  // 筛选变化 → 重置到第一页
  currentPage.value = 1
  emitQuery()
}

const onSort = (prop: string, target?: 'asc' | 'desc') => {
  sort(prop, target)
  props.config.sort = sortProp.value && sortOrder.value
    ? { column: sortProp.value, direction: sortOrder.value }
    : undefined
  // 排序变化 → 重置到第一页
  currentPage.value = 1
  emit('change', props.config)
  emitQuery()
}

// ==================== 分页事件 ====================

/** 每页条数变化 → 已在 v-model 中更新，重置到第一页 */
function onSizeChange(_size: number) {
  currentPage.value = 1
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

/** 根据字符串 hash 自动分配一个固定的 type/color，同一 value 始终得到同一颜色 */
function autoStyle(v: string) {
  const hash = [...v].reduce((h, c) => h + c.charCodeAt(0), 0)
  const idx = Math.abs(hash) % TAG_TYPES.length
  return { tagType: TAG_TYPES[idx], dotColor: DOT_COLORS[idx] }
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
  emit('admin-confirm', cols.map(c => ({ ...c })))
  emit('change', props.config)
}

/** 搜索面板"保存为系统默认" → 触发 admin-confirm 到父组件 */
function onSearchAdminConfirm() {
  emit('admin-confirm', props.config.columns.map(c => ({ ...c })))
  emit('change', props.config)
}

const onSettingReset = (cols: TableItem[]) => {
  props.config.columns.splice(0, props.config.columns.length)
  cols.forEach(c => props.config.columns.push(c))
  reorderKey.value++
  emit('reset')
}

defineExpose({ tableRef, reload })
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
}

.u-table-wrapper {
  width: 100%;
  position: relative;

  // 表格撑满容器
  :deep(.el-table) {
    width: 100% !important;
  }

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
