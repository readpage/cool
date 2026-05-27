<template>
  <div class="exposed-filter">
    <div v-for="item in displayConditions" :key="item.column" class="exposed-condition">
      <div class="exposed-tag">
        <span class="tag-label">{{ colLabelMap[item.column] ?? item.column }}</span>
        <span class="tag-separator">：</span>
        <el-dropdown size="small" trigger="click" @command="(cmd: string) => item.operator = cmd">
          <span class="tag-operator">
            {{ opLabelMap[item.operator] ?? item.operator }}
            <el-icon class="tag-arrow"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="op in operatorOptions" :key="op.value" :command="op.value">
                {{ op.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <el-input v-model="item.value" size="small" class="exposed-value" :placeholder="item.operator === 'in' ? '多个值，逗号分隔' : '请输入'" />
    </div>

    <template v-if="displayConditions.length > 0">
      <el-button type="primary" size="small" @click="emit('submit')">筛选</el-button>
      <el-button size="small" text style="margin-left: 0" type="primary" @click="clearDisplayConditions">
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
  value: string | string[]
  valueStr: string
  display: boolean
}

interface OperatorOption {
  label: string
  value: string
}

/* ============ Props & Emits ============ */

const props = defineProps<{
  conditions: FilterCondition[]
  columns: ColumnConfig[]
  operatorOptions: OperatorOption[]
}>()

const emit = defineEmits<{ (e: 'submit'): void }>()

/* ============ 计算属性 ============ */

const displayConditions = computed(() =>
  props.conditions.filter((c) => c.display && c.column),
)

const colLabelMap = computed(() => {
  const map: Record<string, string> = {}
  for (const col of props.columns) map[col.prop] = col.label
  return map
})

const opLabelMap = computed(() => {
  const map: Record<string, string> = {}
  for (const op of props.operatorOptions) map[op.value] = op.label
  return map
})

/* ============ 清除 ============ */

function clearDisplayConditions() {
  props.conditions.forEach((c) => {
    if (!c.display) return
    if (c.operator === 'between') {
      c.value = ['', '']
    } else if (c.operator === 'in') {
      c.valueStr = ''
      c.value = ''
    } else {
      c.value = ''
    }
  })
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

.tag-label { color: #303133; font-weight: 500; }
.tag-separator { color: #909399; }

.tag-operator {
  color: #409eff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  user-select: none;
  &:hover { color: #66b1ff; }
}

.tag-arrow { font-size: 12px; }

.exposed-value { width: 200px; flex-shrink: 0; }
</style>
