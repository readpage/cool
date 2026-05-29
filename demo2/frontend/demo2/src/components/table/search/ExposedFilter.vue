<template>
  <div class="exposed-filter">
    <div v-for="item in displayConditions" :key="item.column" class="exposed-condition">
      <div class="exposed-tag">
        <span class="tag-label">{{ colLabelMap[item.column] ?? item.column }}</span>
        <span class="tag-separator">：</span>
        <el-dropdown size="small" trigger="click" @command="(cmd: string) => item.operator = cmd as FilterCondition['operator']">
          <span class="tag-operator">
            {{ opLabelMap[item.operator] ?? item.operator }}
            <el-icon class="tag-arrow"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-for="op in getAvailableOperators(item.column)" :key="op.value" :command="op.value">
                {{ op.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <!-- between 操作符 -->
      <template v-if="item.operator === 'between' && isDateRangeField(item.column)">
        <el-date-picker
          v-model="item.value"
          :type="getDateRangeType(item.column)"
          :format="getDateFormat(item.column)"
          :value-format="getDateFormat(item.column)"
          range-separator="~"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="small"
          class="exposed-value"
          :teleported="false"
        />
      </template>
      <template v-else-if="item.operator === 'between'">
        <el-input v-model="item.value[0]" size="small" placeholder="最小值" class="exposed-value-half" />
        <span class="between-sep">~</span>
        <el-input v-model="item.value[1]" size="small" placeholder="最大值" class="exposed-value-half" />
      </template>
      <!-- in 且为下拉 -->
      <FilterValue
        v-else-if="item.operator === 'in' && isSelectField(item.column)"
        v-model="item.value"
        :column="item.column"
        :field-type="getFieldType(item.column)"
        operator="in"
        :options="getOptions(item.column)"
        :remote-method="getRemoteMethod(item.column)"
        placeholder="请选择"
        class="exposed-value"
      />
      <!-- in 文本 -->
      <el-input
        v-else-if="item.operator === 'in'"
        v-model="item.valueStr"
        size="small"
        class="exposed-value"
        placeholder="多个值，逗号分隔"
      />
      <!-- 其他 -->
      <FilterValue
        v-else
        v-model="item.value"
        :column="item.column"
        :field-type="getFieldType(item.column)"
        :operator="item.operator"
        :options="getOptions(item.column)"
        :remote-method="getRemoteMethod(item.column)"
        placeholder="请输入"
        class="exposed-value"
      />
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
import { computed, toRef } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import FilterValue from './FilterValue.vue'
import { useSearchHelpers, clearConditionValue } from './hooks/useSearchHelpers'
import type { ColumnConfig, FilterCondition, OperatorOption } from './types'

/* ============ Props & Emits ============ */

const props = defineProps<{
  conditions: FilterCondition[]
  columns: ColumnConfig[]
  operatorOptions: OperatorOption[]
  loadOptions?: (type: string, keyword?: string) => Promise<{ label: string; value: string }[]>
}>()

const emit = defineEmits<{ (e: 'submit'): void }>()

/* ============ 辅助 ============ */

const {
  getFieldType,
  isDateRangeField,
  getDateRangeType,
  getDateFormat,
  isSelectField,
  getOptions,
  getRemoteMethod,
  getAvailableOperators,
} = useSearchHelpers(
  toRef(props, 'columns'),
  toRef(props, 'loadOptions'),
  toRef(props, 'operatorOptions'),
)

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
    if (c.display) clearConditionValue(c)
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
.exposed-value-half { width: 100px; flex-shrink: 0; }
.between-sep { color: #909399; flex-shrink: 0; }
</style>
