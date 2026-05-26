<template>
  <div class="table-settings" ref="rootRef">
    <!-- 右上角设置按钮 -->
    <button class="settings-btn" @click.stop="togglePanel" :class="{ active: visible }">
      <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
        <path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.488.488 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.07.62-.07.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/>
      </svg>
    </button>

    <!-- 下拉设置面板 -->
    <Transition name="panel-fade">
      <div v-show="visible" class="settings-panel" @click.stop>
        <div class="panel-header">
          <el-checkbox v-model="allChecked" :indeterminate="!allChecked && localColumns.some(c => c.type !== 'index' && !c.hidden)">全选</el-checkbox>
          <button class="close-btn" @click="visible = false">✕</button>
        </div>

        <!-- 列列表 -->
        <div class="panel-body">
          <template v-for="(col, idx) in localColumns" :key="col.prop || idx">
            <div
              v-if="col.type !== 'index'"
              class="col-item"
            :class="{ dragging: dragIdx === idx, 'drop-target': dropTargetIdx === idx && idx !== dragIdx, hidden: col.hidden }"
            @dragover.prevent="onDragOver(idx)"
            @drop.prevent="onDrop(idx)"
          >
            <span
              class="drag-handle"
              draggable="true"
              @dragstart="onDragStart(idx, $event)"
              @dragend="onDragEnd"
            >
              <svg viewBox="0 0 24 24" width="16" height="16" fill="#909399">
                <path d="M11 18c0 1.1-.9 2-2 2s-2-.9-2-2 .9-2 2-2 2 .9 2 2zm-2-8c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0-6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm6 4c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"/>
              </svg>
            </span>

            <el-checkbox
              :model-value="!col.hidden"
              @change="(v: boolean) => col.hidden = !v"
            />
            <span class="col-name">{{ col.label }}</span>

            <div class="min-width-input">
              <el-input-number
                v-model="col.minWidth"
                :min="80"
                :max="600"
                :step="10"
                size="small"
                controls-position="right"
                placeholder="最小宽"
                style="width: 110px"
                @change="col.width = 0"
              />
            </div>

            <button
              class="fixed-btn"
              :class="{ active: col.fixed === 'left' }"
              @click="col.fixed = col.fixed === 'left' ? undefined : 'left'"
              :title="col.fixed === 'left' ? '取消左侧固定' : '固定到左侧'"
            >
              <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                <rect x="3" y="4" width="3" height="16" rx="1"/>
                <rect x="9" y="6" width="12" height="4" rx="1" opacity="0.45"/>
                <rect x="9" y="13" width="8" height="4" rx="1" opacity="0.45"/>
              </svg>
            </button>
            <button
              class="fixed-btn"
              :class="{ active: col.fixed === 'right' }"
              @click="col.fixed = col.fixed === 'right' ? undefined : 'right'"
              :title="col.fixed === 'right' ? '取消右侧固定' : '固定到右侧'"
            >
              <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                <rect x="18" y="4" width="3" height="16" rx="1"/>
                <rect x="3" y="6" width="12" height="4" rx="1" opacity="0.45"/>
                <rect x="7" y="13" width="8" height="4" rx="1" opacity="0.45"/>
              </svg>
            </button>

            <el-radio-group v-model="col.align" size="small" class="align-group">
              <el-radio-button label="left" value="left" title="左对齐">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><rect x="3" y="3" width="18" height="2" rx="1"/><rect x="3" y="8" width="12" height="2" rx="1"/><rect x="3" y="13" width="16" height="2" rx="1"/></svg>
              </el-radio-button>
              <el-radio-button label="center" value="center" title="居中">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><rect x="3" y="3" width="18" height="2" rx="1"/><rect x="7" y="8" width="10" height="2" rx="1"/><rect x="5" y="13" width="14" height="2" rx="1"/></svg>
              </el-radio-button>
              <el-radio-button label="right" value="right" title="右对齐">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><rect x="3" y="3" width="18" height="2" rx="1"/><rect x="9" y="8" width="12" height="2" rx="1"/><rect x="5" y="13" width="16" height="2" rx="1"/></svg>
              </el-radio-button>
            </el-radio-group>
          </div>
          </template>
        </div>

        <!-- 拖拽提示标签 -->
        <div v-if="dragTooltip.visible" class="drag-tooltip" :style="{ left: dragTooltip.x + 'px', top: dragTooltip.y + 'px' }">
          {{ dragTooltip.label }}
        </div>

        <!-- 底部按钮 -->
        <div class="panel-footer">
          <button class="btn btn-reset" @click="resetToDefault">恢复系统默认</button>
          <div class="btn-right">
            <el-button
              v-if="showAdminBtn"
              size="small"
              type="primary"
              @click="onAdminConfirm"
            >保存为系统默认</el-button>
            <el-button size="small" @click="onConfirm">确认</el-button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { ElMessageBox } from 'element-plus'
import type { TableItem } from './index.vue'

const props = defineProps<{
  /** 表格配置 */
  config: { columns: TableItem[] }
  showAdminBtn?: boolean
}>()

const emit = defineEmits<{
  /** 确认后提交新的列配置 */
  (e: 'confirm', columns: TableItem[]): void
  /** 管理员确认默认配置（保存为系统默认） */
  (e: 'admin-confirm', columns: TableItem[]): void
  /** 恢复默认设置 */
  (e: 'reset', columns: TableItem[]): void
}>()

const visible = ref(false)
const rootRef = ref<HTMLElement>()

// 本地深拷贝的列列表，编辑过程不影响原数据
const localColumns: TableItem[] = reactive([])

// 面板打开时的快照（用于"恢复系统默认"）
let snapshotColumns: TableItem[] = []

const initColumns = (src: TableItem[]) => {
  localColumns.splice(0, localColumns.length)
  src.forEach(c => {
    localColumns.push({
      ...c,
      hidden: c.hidden ?? false,
      align: c.align ?? 'left',
    })
  })
  snapshotColumns = src.map(c => ({ ...c, hidden: c.hidden ?? false, align: c.align ?? 'left' }))
}

initColumns(props.config.columns)

// 全选状态（仅统计可配置列，不含 index）
const allChecked = computed({
  get: () => {
    const configurable = localColumns.filter(c => c.type !== 'index')
    if (configurable.length === 0) return false
    return configurable.every(c => !c.hidden)
  },
  set: (val: boolean) => {
    localColumns.forEach(c => {
      if (c.type !== 'index') {
        c.hidden = !val
      }
    })
  },
})

// 外部 columns 变化时同步
watch(() => props.config.columns, (val) => {
  initColumns(val)
}, { deep: true })

// 面板关闭时还原未确认的修改 + 刷新快照
watch(visible, (val) => {
  if (!val) {
    initColumns(props.config.columns)
  }
})



// ==================== 拖拽排序 ====================

let dragIdx = -1
const dropTargetIdx = ref(-1)

// 拖拽跟随提示
const dragTooltip = reactive({ visible: false, x: 0, y: 0, label: '' })
const onDragTooltipMove = (e: DragEvent | MouseEvent) => {
  if (!dragTooltip.visible) return
  dragTooltip.x = e.clientX + 12
  dragTooltip.y = e.clientY - 10
}

const onDragStart = (idx: number, e: DragEvent) => {
  dragIdx = idx
  dragTooltip.visible = true
  dragTooltip.label = localColumns[idx]?.label || ''
  onDragTooltipMove(e)
  document.addEventListener('dragover', onDragTooltipMove)
  if (e.dataTransfer) {
    e.dataTransfer.effectAllowed = 'move'
    e.dataTransfer.setData('text/plain', '')
  }
}
const onDragOver = (idx: number) => {
  if (dragIdx < 0 || dragIdx === idx) return
  const col = localColumns[idx]
  if (col?.type === 'index') return
  dropTargetIdx.value = idx
}
const onDragEnd = () => {
  dragIdx = -1
  dropTargetIdx.value = -1
  dragTooltip.visible = false
  document.removeEventListener('dragover', onDragTooltipMove)
}
const onDrop = (idx: number) => {
  dropTargetIdx.value = -1
  if (dragIdx < 0 || dragIdx === idx) return
  // 不允许放到固定列前面
  const minIdx = localColumns.findIndex(c => c.type !== 'index')
  idx = Math.max(idx, minIdx)
  if (dragIdx === idx) return
  const moved = localColumns.splice(dragIdx, 1)[0]
  localColumns.splice(idx, 0, moved)
  dragIdx = -1
}

// ==================== 按钮操作 ====================

const onConfirm = () => {
  visible.value = false
  emit('confirm', localColumns.map(c => ({ ...c })))
}

const onAdminConfirm = () => {
  confirming = true
  ElMessageBox.confirm('将当前列配置保存为系统默认设置，确认？', '保存系统默认', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    visible.value = false
    emit('admin-confirm', localColumns.map(c => ({ ...c })))
  }).catch(() => {}).finally(() => { confirming = false })
}

const resetToDefault = () => {
  confirming = true
  ElMessageBox.confirm('将恢复为系统默认列配置，当前修改将丢失，确认？', '恢复系统默认', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    initColumns(snapshotColumns)
    visible.value = false
    emit('reset', localColumns.map(c => ({ ...c })))
  }).catch(() => {}).finally(() => { confirming = false })
}

// 切换面板显隐
const togglePanel = () => {
  visible.value = !visible.value
}

// 确认框打开时阻止面板关闭
let confirming = false

// 点击外部关闭
const onClickOutside = (e: MouseEvent) => {
  if (!visible.value || confirming) return
  if (rootRef.value && !rootRef.value.contains(e.target as Node)) {
    visible.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', onClickOutside, true)
})
</script>

<style lang="scss" scoped>
.table-settings {
  position: relative;
  z-index: 10;
}

.settings-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  color: #909399;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  z-index: 102;

  &:hover,
  &.active {
    color: #409eff;
    border-color: #c6e2ff;
    background: #ecf5ff;
  }
}

// ==================== 下拉面板 ====================

.settings-panel {
  position: absolute;
  top: 34px;
  right: 0;
  width: 420px;
  max-height: 480px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 999;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.close-btn {
  background: none;
  border: none;
  font-size: 16px;
  color: #909399;
  cursor: pointer;
  padding: 2px 4px;
  line-height: 1;

  &:hover {
    color: #303133;
  }
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.col-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  width: 100%;
  box-sizing: border-box;
  cursor: default;
  transition: background 0.15s;

  &:hover {
    background: #f8f9fb;
  }

  &.dragging {
    opacity: 0.4;
  }

  &.hidden {
    opacity: 0.5;
  }

  &.drop-target {
    border-top: 2px solid #409eff;
    background: rgba(#409eff, 0.04);
  }
}

.drag-handle {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  border-radius: 4px;
  transition: background 0.15s;

  &:active {
    cursor: grabbing;
  }
}

.col-name {
  flex: 1 1 auto;
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.min-width-input {
  flex-shrink: 0;
}

.align-group {
  flex-shrink: 0;

  :deep(.el-radio-button__inner) {
    padding: 4px 6px;
    line-height: 1;
  }

  :deep(svg) {
    pointer-events: none;
    vertical-align: middle;
  }
}

.fixed-btn {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  color: #c0c4cc;
  cursor: pointer;
  padding: 0;
  transition: all 0.2s;

  &:hover {
    border-color: #409eff;
    color: #409eff;
  }

  &.active {
    color: #409eff;
    border-color: #a0cfff;
    background: #ecf5ff;
  }

  &.placeholder {
    width: 24px;
    visibility: hidden;
  }
}

// ==================== 底部按钮 ====================

.panel-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-top: 1px solid #ebeef5;
}

.btn-right {
  display: flex;
  gap: 8px;
}

.drag-tooltip {
  position: fixed;
  padding: 4px 10px;
  font-size: 12px;
  color: #fff;
  background: rgba(0, 0, 0, 0.72);
  border-radius: 4px;
  white-space: nowrap;
  pointer-events: none;
  z-index: 9999;
}

.btn-reset {
  padding: 5px 12px;
  font-size: 12px;
  color: #606266;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;

  &:hover {
    color: #409eff;
    border-color: #c6e2ff;
    background: #ecf5ff;
  }
}

// ==================== 过渡动画 ====================

.panel-fade-enter-active,
.panel-fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}
.panel-fade-enter-from,
.panel-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
