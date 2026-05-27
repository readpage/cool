<template>
  <div class="u-table-wrapper">
    <!-- 表格导航工具栏 -->
    <div class="u-table-toolbar">
      <TableSettings
        :config="config"
        :show-admin-btn="showAdminBtn"
        @confirm="onSettingConfirm"
        @admin-confirm="onAdminConfirm"
        @reset="onSettingReset"
      />
    </div>

    <div ref="tableWrapperRef" class="u-table">
    <el-table
      ref="tableRef"
      :key="reorderKey"
      :data="data"
      :stripe="config.stripe"
      :size="config.size"
      :height="config.height"
      :max-height="config.maxHeight"
      :row-key="config.rowKey"
      :header-cell-style="headerCellStyle"
      @selection-change="emit('selection-change', $event)"
      @row-click="(row: any, column: any, event: Event) => emit('row-click', row, column, event)"
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
            {{ scope.row[item.prop] }}
          </slot>
        </template>
      </el-table-column>
      </template>
    </el-table>
    <!-- 列背景高亮 -->
    <div v-show="columnHighlightVisible" class="col-highlight" :style="columnHighlightStyle"></div>
    <!-- 拖拽实线：点击后固定在原始列边界 -->
    <div v-show="dragLineVisible" class="drag-line" :style="dragLineStyle"></div>
    <!-- 拖拽虚线：跟随鼠标预览新位置 -->
    <div v-show="previewLineVisible" class="preview-line" :style="previewLineStyle"></div>
    <!-- 列排序 drop 指示线 -->
    <div v-show="dropLineVisible" class="drop-line" :style="dropLineStyle"></div>
    <!-- 列排序：拖拽列背景高亮 -->
    <div v-show="colHighlightVisible" class="col-reorder-highlight" :style="colHighlightStyle"></div>
    <!-- 列排序拖拽列名提示 -->
    <div v-show="reorderTooltipVisible" class="reorder-tooltip" :style="reorderTooltipStyle">{{ reorderTooltipLabel }}</div>
    <!-- 列宽拖拽像素提示框 -->
    <div v-show="tooltipVisible" class="resize-tooltip" :style="tooltipStyle">{{ tooltipText }}</div>
  </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useColumnResize } from './hooks/useColumnResize'
import { useColumnReorder } from './hooks/useColumnReorder'
import { useColumnSort } from './hooks/useColumnSort'
import TableSettings from './TableSettings.vue'

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
}

const props = defineProps<{
  config: TableConfig
  data: Record<string, any>[]
  /** 是否显示多选列 */
  selection?: boolean
  showAdminBtn?: boolean
}>()

const emit = defineEmits<{
  (e: 'selection-change', value: any[]): void
  (e: 'row-click', row: any, column: any, event: Event): void
  /** 配置变更（列宽、筛选、排序配置等），返回完整 config */
  (e: 'change', config: TableConfig): void
  /** 管理员确认默认配置 */
  (e: 'admin-confirm', columns: TableItem[]): void
  /** 恢复系统默认 */
  (e: 'reset'): void
  /** 查询参数变化（排序、筛选等），返回 query 对象 */
  (e: 'query', query: Record<string, any>): void
}>()

const tableRef = ref()
const tableWrapperRef = ref<HTMLElement>()
const reorderKey = ref(0) // 排序后 +1 强制重绘

// debug: 确认 stripe 传值
watch(() => props.config.stripe, (v) => console.log('[table] stripe changed:', v), { immediate: true })

const isFixedCol = (item: TableItem) => !!item.fixed

// 动态列引用，config 整体替换后仍响应
const columnsRef = computed(() => props.config.columns)

// 列宽拖拽
const { dragLineVisible, dragLineStyle, previewLineVisible, previewLineStyle, columnHighlightVisible, columnHighlightStyle, tooltipVisible, tooltipStyle, tooltipText } = useColumnResize({
  wrapperRef: tableWrapperRef,
  columns: columnsRef,
  edgeThreshold: 12,
  onResizeEnd: () => emit('change', props.config),
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

const onSort = (prop: string, target?: 'asc' | 'desc') => {
  sort(prop, target)
  props.config.sort = sortProp.value && sortOrder.value
    ? { column: sortProp.value, direction: sortOrder.value }
    : undefined
  emit('change', props.config)
  emit('query', {
    columns: props.config.columns.filter(c => !c.hidden).map(c => c.prop!),
    filter: {},
    sort: props.config.sort || {},
  })
}

const headerCellStyle = () => ({
  background: '#f5f7fa',
  color: '#606266',
})

// ========================
// 初始化：对齐 + 撑满
// ========================

onMounted(async () => {
  await nextTick()

  // 初始化 align：未设置时，文本列默认 left，数值列默认 right
  const columns = props.config.columns
  columns.forEach(c => {
    if (c.align === undefined && c.prop) {
      const val = props.data[0]?.[c.prop]
      c.align = typeof val === 'number' ? 'right' : 'left'
    }
  })

  // 未撑满时，最后一列 width → minWidth，弹性填满
  const wrapper = tableWrapperRef.value
  if (!wrapper) return
  if (props.config.stripe == null) props.config.stripe = true
  const total = columns.reduce((s, c) => s + (typeof c.width === 'number' ? c.width : 0), 0)
  if (total < (wrapper.clientWidth || 0)) {
    // 倒序找最后一个可拖拽列
    for (let i = columns.length - 1; i >= 0; i--) {
      const c = columns[i]
      if (c.resizable === false || isFixedCol(c)) continue
      const w = typeof c.width === 'number' ? c.width : 0
      c.minWidth = Math.max(w, 80)
      c.width = undefined
      break
    }
  }
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

const onSettingReset = (cols: TableItem[]) => {
  props.config.columns.splice(0, props.config.columns.length)
  cols.forEach(c => props.config.columns.push(c))
  reorderKey.value++
  emit('reset')
}

defineExpose({ tableRef })
</script>

<style lang="scss" scoped>
.u-table-wrapper {
  width: 100%;
}

.u-table-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 36px;
  padding: 0 6px;
  border: 1px solid #ebeef5;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
  background: #fafafa;
}

.u-table {
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

  // 列宽拖拽高亮（蓝色）
  .col-highlight {
    position: absolute;
    background-color: rgba(#e6f7ff, 0.4);
    pointer-events: none;
    z-index: 1;
  }

  // 列排序拖拽高亮（浅灰白）
  .col-reorder-highlight {
    position: absolute;
    background-color: rgba(0, 0, 0, 0.04);
    pointer-events: none;
    z-index: 1;
  }

  // 拖拽实线：固定在原始列边界
  .drag-line {
    position: absolute;
    width: 1px;
    height: 100%;
    background-color: #409eff;
    pointer-events: none;
    z-index: 99;
  }

  // 拖拽虚线：跟随鼠标预览新位置
  .preview-line {
    position: absolute;
    width: 1px;
    height: 100%;
    border-left: 1px dashed #409eff;
    pointer-events: none;
    z-index: 98;
  }

  // 列排序 drop 指示线
  .drop-line {
    position: absolute;
    width: 1px;
    height: 100%;
    background-color: #f56c6c;
    pointer-events: none;
    z-index: 97;
  }

  // 列排序拖拽列名提示
  .reorder-tooltip {
    position: fixed;
    padding: 4px 10px;
    font-size: 12px;
    color: #fff;
    background-color: rgba(0, 0, 0, 0.72);
    border-radius: 4px;
    white-space: nowrap;
    pointer-events: none;
    z-index: 9999;
  }

  // 列宽拖拽像素提示框
  .resize-tooltip {
    position: absolute;
    padding: 2px 8px;
    font-size: 12px;
    line-height: 20px;
    color: #fff;
    background-color: rgba(0, 0, 0, 0.75);
    border-radius: 4px;
    white-space: nowrap;
    pointer-events: none;
    z-index: 100;
    transform: translateX(-50%);

    &::after {
      content: '';
      position: absolute;
      left: 50%;
      bottom: -4px;
      transform: translateX(-50%);
      width: 0;
      height: 0;
      border-left: 4px solid transparent;
      border-right: 4px solid transparent;
      border-top: 4px solid rgba(0, 0, 0, 0.75);
    }
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
</style>
