<template>
  <div class="panel-top" :style="{ height: topHeight + 'px' }">
    <div class="panel-header">
      <span class="panel-title">SQL 环境</span>
      <span class="panel-hint">
        <code>{{filter}}</code> 筛选 &nbsp;|&nbsp; <code>{{sort}}</code> 排序 &nbsp;|&nbsp; <code>[[...]]</code> 可选块
      </span>
    </div>
    <div class="panel-body">
      <SqlEditor
        :model-value="modelValue"
        :placeholder="placeholder"
        @update:model-value="$emit('update:modelValue', $event)"
      />

      <!-- 右侧浮动操作按钮 -->
      <div class="floating-actions">
        <el-tooltip content="运行 Ctrl+Enter" placement="left">
          <el-button
            type="primary"
            circle
            size="large"
            :loading="loading"
            class="action-run"
            @click="$emit('run')"
          >
            <span v-if="!loading" class="run-icon">▶</span>
          </el-button>
        </el-tooltip>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import SqlEditor from '@/components/sql-editor/SqlEditor.vue'

withDefaults(defineProps<{
  modelValue: string
  topHeight: number
  loading?: boolean
  placeholder?: string
}>(), {
  loading: false,
  placeholder: "SELECT prod_num '编码', prod_name '名称' FROM itm WHERE 1=1 {{filter}} {{sort}}",
})

defineEmits<{
  'update:modelValue': [value: string]
  run: []
}>()
</script>

<style lang="scss" scoped>
.panel-top {
  display: flex;
  flex-direction: column;
  min-height: 100px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 36px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;

  &::before {
    content: '';
    display: inline-block;
    width: 3px;
    height: 14px;
    background: #409eff;
    border-radius: 2px;
  }
}

.panel-hint {
  font-size: 11px;
  color: #bbb;

  code {
    background: #f5f5f5;
    padding: 1px 5px;
    border-radius: 3px;
    font-size: 11px;
    color: #909399;
  }
}

.panel-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background: #fff;
  position: relative;
}

.floating-actions {
  position: absolute;
  right: 20px;
  bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  z-index: 100;
}

.action-run {
  width: 48px;
  height: 48px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 6px 18px rgba(64, 158, 255, 0.55);
  }

  .run-icon {
    font-size: 18px;
    margin-left: 2px;
  }
}
</style>
