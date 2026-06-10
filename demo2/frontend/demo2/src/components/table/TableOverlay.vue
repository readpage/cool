<template>
  <!-- 列宽拖拽：列背景高亮 -->
  <div v-show="resize.columnHighlightVisible" class="col-highlight" :style="resize.columnHighlightStyle"></div>
  <!-- 列宽拖拽：实线（原始边界） -->
  <div v-show="resize.dragLineVisible" class="drag-line" :style="resize.dragLineStyle"></div>
  <!-- 列宽拖拽：虚线（鼠标预览） -->
  <div v-show="resize.previewLineVisible" class="preview-line" :style="resize.previewLineStyle"></div>
  <!-- 列宽拖拽：像素提示框 -->
  <div v-show="resize.tooltipVisible" class="resize-tooltip" :style="resize.tooltipStyle">{{ resize.tooltipText }}</div>

  <!-- 列排序拖拽：drop 指示线 -->
  <div v-show="reorder.dropLineVisible" class="drop-line" :style="reorder.dropLineStyle"></div>
  <!-- 列排序拖拽：列背景高亮 -->
  <div v-show="reorder.colHighlightVisible" class="col-reorder-highlight" :style="reorder.colHighlightStyle"></div>
  <!-- 列排序拖拽：列名提示 -->
  <div v-show="reorder.reorderTooltipVisible" class="reorder-tooltip" :style="reorder.reorderTooltipStyle">{{ reorder.reorderTooltipLabel }}</div>
</template>

<script setup lang="ts">
interface ResizeOverlay {
  columnHighlightVisible: boolean
  columnHighlightStyle: Record<string, string>
  dragLineVisible: boolean
  dragLineStyle: Record<string, string>
  previewLineVisible: boolean
  previewLineStyle: Record<string, string>
  tooltipVisible: boolean
  tooltipStyle: Record<string, string>
  tooltipText: string
}

interface ReorderOverlay {
  dropLineVisible: boolean
  dropLineStyle: Record<string, string>
  colHighlightVisible: boolean
  colHighlightStyle: Record<string, string>
  reorderTooltipVisible: boolean
  reorderTooltipStyle: Record<string, string>
  reorderTooltipLabel: string
}

defineProps<{
  resize: ResizeOverlay
  reorder: ReorderOverlay
}>()
</script>

<style lang="scss" scoped>
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
</style>
