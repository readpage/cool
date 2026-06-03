<template>
  <div class="param-bar" v-if="visibleParams.length > 0">
    <el-form :model="values" inline size="small">
      <template v-for="p in visibleParams" :key="p.name">
        <el-form-item :label="p.label || p.name">
          <el-date-picker
            v-if="p.type === 'daterange'"
            v-model="values[p.name]"
            type="daterange" range-separator="至"
            start-placeholder="开始" end-placeholder="结束"
            value-format="YYYY-MM-DD" style="width:210px"
          />
          <el-date-picker
            v-else-if="p.type === 'date'"
            v-model="values[p.name]"
            type="date" value-format="YYYY-MM-DD" style="width:160px"
          />
          <el-input-number v-else-if="p.type === 'number'" v-model="values[p.name]" style="width:160px" />
          <el-input v-else v-model="values[p.name]" placeholder="请输入" style="width:160px" @keyup.enter="$emit('execute')" />
        </el-form-item>
      </template>
      <el-form-item>
        <el-button size="small" @click="$emit('reset')">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ParamDef } from '@/types/report'

const props = defineProps<{
  parameters: ParamDef[]
  values: Record<string, any>
}>()

defineEmits<{
  execute: []
  reset: []
}>()

/** 过滤掉内置参数（filter/sort/column），不在此展示 */
const visibleParams = computed(() =>
  props.parameters.filter(p => !p.builtin)
)
</script>

<style lang="scss" scoped>
.param-bar {
  flex-shrink: 0;
  padding: 8px 16px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;

  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 12px;
  }
  :deep(.el-form-item__label) {
    font-size: 12px;
    padding-right: 6px;
  }
}
</style>
