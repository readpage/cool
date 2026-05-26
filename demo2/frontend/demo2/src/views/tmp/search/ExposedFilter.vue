<template>
  <div class="exposed-filter">
    <!-- 外露筛选条件行 -->
    <div
      v-for="item in displayConditions"
      :key="item.column"
      class="exposed-condition"
    >
      <!-- 标签式字段+操作符 -->
      <div class="exposed-tag">
        <span class="tag-label">{{ getColumnLabel(item.column) }}</span>
        <span class="tag-separator">：</span>
        <el-dropdown size="small" trigger="click" @command="(cmd: string) => item.operator = cmd">
          <span class="tag-operator">
            {{ getOperatorLabel(item.operator) }}
            <el-icon class="tag-arrow"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="op in operatorOptions"
                :key="op.value"
                :command="op.value"
              >
                {{ op.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <el-input
        v-model="item.value"
        size="small"
        class="exposed-value"
        :placeholder="getExposedPlaceholder(item.operator)"
      />
    </div>

    <!-- 底部操作栏 -->
    <template v-if="displayConditions.length > 0">
      <el-button type="primary" size="small" @click="emit('submit')">
        筛选
      </el-button>
      <el-button
        size="small"
        text
        style="margin-left: 0"
        type="primary"
        @click="clearDisplayConditions"
      >
        清除筛选值
      </el-button>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'

/* ============ 类型 ============ */

interface ColumnConfig {
  prop: string
  label: string
}

interface FilterCondition {
  column: string
  operator: string
  value: string
  display: boolean
}

interface OperatorOption {
  label: string
  value: string
}

/* ============ Props ============ */

const props = defineProps<{
  conditions: FilterCondition[]
  columns: ColumnConfig[]
  operatorOptions: OperatorOption[]
}>()

const emit = defineEmits<{
  (e: 'submit'): void
}>()

/* ============ 外露条件 ============ */

const displayConditions = computed(() =>
  props.conditions.filter((c) => c.display && c.column)
)

function clearDisplayConditions() {
  props.conditions.forEach((c) => {
    if (c.display) {
      c.value = ''
      c.operator = 'contains'
    }
  })
}

function getColumnLabel(prop: string): string {
  return props.columns.find((c) => c.prop === prop)?.label ?? prop
}

function getOperatorLabel(value: string): string {
  return props.operatorOptions.find((op) => op.value === value)?.label ?? value
}

function getExposedPlaceholder(operator: string): string {
  if (operator === 'in') return '多个值，逗号分隔'
  return '请输入'
}
</script>

<style lang="scss" scoped>
.exposed-filter {
  display: inline-flex;
  align-items: center;
  margin-top: 5px;
  gap: 8px;
  flex-wrap: wrap;
}

.exposed-condition {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.exposed-tag {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 8px;
  background: #ecf5ff;
  border-radius: 4px;
  font-size: 13px;
  white-space: nowrap;
}

.tag-label {
  color: #303133;
  font-weight: 500;
}

.tag-separator {
  color: #909399;
}

.tag-operator {
  color: #409eff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  user-select: none;

  &:hover {
    color: #66b1ff;
  }
}

.tag-arrow {
  font-size: 12px;
}

.exposed-value {
  width: 200px;
  flex-shrink: 0;
}
</style>
