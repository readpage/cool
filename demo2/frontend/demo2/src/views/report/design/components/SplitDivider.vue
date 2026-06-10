<template>
  <div class="split-divider" @mousedown="$emit('mousedown', $event)">
    <div class="split-handle" />
  </div>
</template>

<script setup lang="ts">
defineEmits<{
  mousedown: [e: MouseEvent]
}>()
</script>

<style lang="scss" scoped>
.split-divider {
  height: 6px;
  background: #f0f2f5;
  cursor: row-resize;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s;
  position: relative;
  z-index: 10;

  &:hover,
  &:active {
    background: #e0e4e8;
  }
}

.split-handle {
  width: 32px;
  height: 4px;
  background: #c0c4cc;
  border-radius: 2px;
  transition: background 0.15s;

  .split-divider:hover &,
  .split-divider:active & {
    background: #909399;
  }
}
</style>
