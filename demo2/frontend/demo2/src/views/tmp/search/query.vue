<template>
  <div class="filter-wrapper">
    <el-popover
      ref="popoverRef"
      :visible="popoverVisible"
      placement="right-start"
      :width="560"
      :show-arrow="false"
      :hide-after="0"
      :teleported="false"
      popper-class="filter-popper"
    >
      <template #reference>
        <el-button @click.stop="popoverVisible = !popoverVisible">
          <el-icon><Filter /></el-icon>
          筛选
        </el-button>
      </template>

      <div class="filter-panel">
        <div class="panel-header">
          <span class="panel-title">设置筛选</span>
          <el-button text size="small" @click="popoverVisible = false">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>

        <div class="panel-body">
          <div class="condition-box">
            <span class="logic-badge">且</span>
            <div class="condition-list">
              <div v-for="(cond, index) in conditions" :key="index" class="condition-row">
                <div class="logic-col">
                  <div class="line-segment" :class="{ 'top-hidden': index === 0 }" />
                  <div class="line-node"><span class="branch-line" /></div>
                  <div class="line-segment" :class="{ 'bottom-connect': index === conditions.length - 1 }" />
                </div>

                <div class="condition-content">
                  <div class="cond-field-wrap">
                    <el-select
                      v-model="cond.column"
                      placeholder="可选择"
                      class="cond-field"
                      :teleported="false"
                      @change="onColumnChange(index)"
                    >
                      <el-option v-for="col in columns" :key="col.prop" :label="col.label" :value="col.prop" />
                    </el-select>
                    <span v-if="!cond.column" class="field-error">请填写筛选条件</span>
                  </div>

                  <el-select v-model="cond.operator" placeholder="操作符" class="cond-operator" :teleported="false">
                    <el-option v-for="op in operatorOptions" :key="op.value" :label="op.label" :value="op.value" />
                  </el-select>

                  <template v-if="cond.operator === 'between'">
                    <el-input v-model="cond.value[0]" placeholder="最小值" class="cond-value" />
                    <span class="between-sep">~</span>
                    <el-input v-model="cond.value[1]" placeholder="最大值" class="cond-value" />
                  </template>
                  <template v-else-if="cond.operator === 'in'">
                    <el-input v-model="cond.valueStr" placeholder="多个值用逗号分隔" class="cond-value" />
                  </template>
                  <template v-else>
                    <el-input v-model="cond.value" placeholder="请输入" class="cond-value" />
                  </template>

                  <el-icon class="cond-delete" @click="removeCondition(index)"><Remove /></el-icon>
                  <el-checkbox v-model="cond.display" class="cond-display" :disabled="!cond.column">外露</el-checkbox>
                </div>
              </div>

              <div class="add-condition-row">
                <div class="logic-col">
                  <div class="line-segment bottom-tail" />
                  <div class="add-btn" @click="addCondition">
                    <el-icon><Plus /></el-icon>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <el-divider class="panel-divider" />

        <div class="panel-footer">
          <div class="footer-left"><el-button text>另存为</el-button></div>
          <div class="footer-right">
            <el-button @click="handleClear">清除筛选值</el-button>
            <el-button type="primary" :disabled="!canSubmit" @click="handleFilter">筛选</el-button>
          </div>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Filter, Plus, Remove, Close } from '@element-plus/icons-vue'

/* ============ 类型 ============ */

interface ColumnConfig {
  prop: string
  label: string
  operator?: string
  filterMode?: 'show' | 'exposed' | 'hide'
}

interface FilterCondition {
  column: string
  operator: string
  value: string | string[]
  valueStr: string
  display: boolean
}

type FilterResult = {
  column: string
  operator: string
  value: string | [string, string] | string[]
}

/* ============ Props & Emits ============ */

const props = defineProps<{
  columns: ColumnConfig[]
  operatorOptions: { label: string; value: string }[]
  exposed?: string[]
}>()

const emit = defineEmits<{
  (e: 'filter', value: FilterResult[]): void
}>()

/* ============ 条件状态 ============ */

const popoverVisible = ref(false)
const conditions = ref<FilterCondition[]>([])

const canSubmit = computed(() => conditions.value.every((c) => c.column))

function createCondition(initialColumn = ''): FilterCondition {
  const col = props.columns.find((c) => c.prop === initialColumn)
  return {
    column: initialColumn,
    operator: col?.operator ?? 'contains',
    value: '',
    valueStr: '',
    display: col?.filterMode === 'exposed',
  }
}

/* ============ 初始化 ============ */

if (props.exposed?.length) {
  props.exposed.forEach((prop) => {
    if (props.columns.some((c) => c.prop === prop)) {
      conditions.value.push(createCondition(prop))
    }
  })
}
if (conditions.value.length === 0) {
  conditions.value.push(createCondition())
}

/* ============ 条件操作 ============ */

function addCondition() {
  conditions.value.push(createCondition())
}

function removeCondition(index: number) {
  conditions.value.splice(index, 1)
}

function onColumnChange(index: number) {
  const col = props.columns.find((c) => c.prop === conditions.value[index].column)
  if (!col) return
  conditions.value[index].operator = col.operator ?? 'contains'
  if (props.exposed?.includes(col.prop)) {
    conditions.value[index].display = true
  }
}

/* ============ 构建与提交 ============ */

function buildFilterResult(): FilterResult[] {
  return conditions.value
    .filter((c) => c.column && isEmptyValue(c))
    .map((c): FilterResult => ({
      column: c.column,
      operator: c.operator,
      value:
        c.operator === 'between' ? [c.value[0] ?? '', c.value[1] ?? ''] :
        c.operator === 'in' ? c.valueStr.split(',').map((v) => v.trim()).filter(Boolean) :
        c.value,
    }))
}

function isEmptyValue(c: FilterCondition): boolean {
  if (c.operator === 'between') return c.value[0] !== '' || c.value[1] !== ''
  if (c.operator === 'in') return c.valueStr !== ''
  return c.value !== ''
}

function handleFilter() {
  const result = buildFilterResult()
  emit('filter', result)
  popoverVisible.value = false
}

function handleClear() {
  conditions.value.forEach((c) => {
    if (c.operator === 'between') {
      c.value = ['', '']
    } else if (c.operator === 'in') {
      c.valueStr = ''
      c.value = ''
    } else {
      c.value = ''
    }
  })
  emit('filter', [])
}

/* ============ 暴露 ============ */

defineExpose({ conditions })
</script>

<style lang="scss" scoped>
.filter-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.filter-panel {
  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    .panel-title { font-size: 14px; font-weight: 600; color: #303133; }
  }

  .panel-body {
    .condition-list { display: flex; flex-direction: column; }

    .condition-row {
      display: flex;
      align-items: center;
      min-height: 40px;

      .condition-content {
        display: flex;
        align-items: center;
        gap: 6px;
        flex-wrap: wrap;
        flex: 1;
        padding-bottom: 20px;
      }
    }

    .add-condition-row {
      display: flex;
      align-items: center;

      .logic-col {
        display: flex;
        flex-direction: column;
        align-items: center;
        width: 20px;
        flex-shrink: 0;
        align-self: stretch;
      }
    }

    .logic-col {
      display: flex;
      flex-direction: column;
      align-items: center;
      width: 20px;
      flex-shrink: 0;
      align-self: stretch;
    }

    .line-segment {
      width: 2px;
      flex: 1;
      min-height: 8px;
      background: #dcdfe6;
      &.top-hidden { visibility: hidden; }
      &.bottom-connect { min-height: 12px; }
      &.bottom-tail { flex: none; height: 14px; }
    }

    .line-node {
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      z-index: 1;
      width: 100%;
      position: relative;
    }

    .branch-line {
      position: absolute;
      left: 50%;
      top: 50%;
      transform: translateY(-50%);
      width: 12px;
      border-top: 2px solid #dcdfe6;
    }

    .add-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 16px;
      height: 16px;
      border: 1px solid #409eff;
      border-radius: 50%;
      color: #409eff;
      cursor: pointer;
      font-size: 14px;
      flex-shrink: 0;
      background: #fff;
      &:hover { background: #ecf5ff; }
    }

    .condition-box {
      position: relative;
      border: 2px dashed #dcdfe6;
      border-radius: 4px;
      padding: 12px;
      padding-left: 40px;
    }

    .logic-badge {
      position: absolute;
      left: 12px;
      top: 50%;
      transform: translateY(-50%);
      font-size: 12px;
      color: #409eff;
      border: 1px solid #409eff;
      border-radius: 2px;
      padding: 1px 5px;
      line-height: 18px;
      background: #fff;
      z-index: 2;
    }

    .cond-operator { width: 100px; flex-shrink: 0; }

    .cond-field-wrap {
      width: 110px;
      flex-shrink: 0;
      position: relative;
      .cond-field { width: 100%; }
    }

    .field-error {
      position: absolute;
      left: 0;
      bottom: -18px;
      font-size: 11px;
      color: #f56c6c;
      white-space: nowrap;
    }

    .cond-value { flex: 1; min-width: 100px; }
    .between-sep { color: #909399; flex-shrink: 0; }
    .cond-display { flex-shrink: 0; }

    .cond-delete {
      flex-shrink: 0;
      font-size: 18px;
      color: #f56c6c;
      cursor: pointer;
      &:hover { color: #f89898; }
    }
  }

  .panel-divider { margin: 12px 0; }

  .panel-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    .footer-left { flex-shrink: 0; }
    .footer-right { display: flex; gap: 8px; margin-left: auto; }
  }
}
</style>

<style lang="scss">
.filter-popper { padding: 16px !important; }
</style>
