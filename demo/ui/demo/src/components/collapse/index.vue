<template>
  <div class="u-collapse">
    <div class="u-collapse__header" @click="() => (state.visible = !state.visible)">
      <span>{{ props.label }}</span>
      <u-icon>
        <arrow-down v-show="state.visible" />
        <arrow-up v-show="!state.visible" />
      </u-icon>
    </div>
    <div class="u-collapse__container" :class="{ 'is-fold': !state.visible }">
      <div class="child"><slot /></div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'

interface Props {
  label: string // 标题
  unfold?: boolean // 是否能展开、收起
}

const props = withDefaults(defineProps<Props>(), {
  unfold: true
})

const state = reactive({
  visible: props.unfold
})
</script>

<style lang="scss" scoped>
.u-collapse {
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    height: 32px;
    line-height: normal;
    font-weight: 700;
    padding: 0 10px;
    background-color: #f2f6fc;
    color: #606266;
    border-radius: 4px;
    cursor: pointer;
  }
  &__container {
    display: grid;
    grid-template-rows: 1fr;
    transition: all 0.3s;
    &.is-fold {
      grid-template-rows: 0fr;
    }
    .child {
      overflow-y: hidden;
      min-height: 0;
    }
  }
}
</style>
