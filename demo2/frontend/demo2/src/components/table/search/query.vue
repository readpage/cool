<template>
  <div class="filter-wrapper">
    <el-popover
      ref="popoverRef"
      :visible="popoverVisible"
      placement="right-start"
      :width="700"
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
                      clearable
                      @change="onColumnChange(index)"
                    >
                      <el-option v-for="col in columns" :key="col.prop" :label="col.label" :value="col.prop" />
                    </el-select>
                    <span v-if="!cond.column" class="field-error">请填写筛选条件</span>
                  </div>

                  <el-select v-model="cond.operator" placeholder="操作符" class="cond-operator" :teleported="false">
                    <el-option v-for="op in getAvailableOperators(cond.column)" :key="op.value" :label="op.label" :value="op.value" />
                  </el-select>

                  <template v-if="cond.operator === 'between'">
                    <!-- 日期范围：单个 date picker range -->
                    <template v-if="isDateRangeField(cond.column)">
                      <el-date-picker
                        v-model="cond.value"
                        :type="getDateRangeType(cond.column)"
                        :format="getDateFormat(cond.column)"
                        :value-format="getDateFormat(cond.column)"
                        range-separator="~"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        class="cond-value"
                        :teleported="false"
                      />
                    </template>
                    <!-- 文本范围：两个输入框 -->
      <template v-else>
                    <el-input v-model="cond.value[0]" placeholder="最小值" class="cond-value cond-value-between" clearable />
                    <span class="between-sep">~</span>
                    <el-input v-model="cond.value[1]" placeholder="最大值" class="cond-value cond-value-between" clearable />
                  </template>
                  </template>
                  <template v-else-if="cond.operator === 'in'">
                    <!-- 下拉多选 -->
                    <FilterValue
                      v-if="isSelectField(cond.column)"
                      v-model="cond.value"
                      :column="cond.column"
                      :field-type="getFieldType(cond.column)"
                      :picker-type="getPickerType(cond.column)"
                      operator="in"
                      :options="getOptions(cond.column)"
                      :remote-method="getRemoteMethod(cond.column)"
                      placeholder="请选择"
                    />
                    <!-- 文本逗号分隔 -->
                    <el-input v-else v-model="cond.valueStr" placeholder="多个值用逗号分隔" class="cond-value" clearable />
                  </template>
                  <template v-else>
                    <FilterValue
                      v-model="cond.value"
                      :column="cond.column"
                      :field-type="getFieldType(cond.column)"
                      :picker-type="getPickerType(cond.column)"
                      :operator="cond.operator"
                      :options="getOptions(cond.column)"
                      :remote-method="getRemoteMethod(cond.column)"
                      placeholder="请输入"
                    />
                  </template>

                  <el-icon class="cond-delete" @click="removeCondition(index)"><Remove /></el-icon>
                  <el-checkbox :model-value="cond.filterMode === 'exposed'" @change="(val: any) => cond.filterMode = val ? 'exposed' : 'hide'" class="cond-display" :disabled="!cond.column">外露</el-checkbox>
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
          <div class="footer-left">
            <el-button v-if="showAdminBtn" text type="primary" @click="emit('admin-confirm')">保存为系统默认</el-button>
          </div>
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
import { ref, computed, toRef } from 'vue'
import { Filter, Plus, Remove, Close } from '@element-plus/icons-vue'
import FilterValue from './FilterValue.vue'
import { useSearchHelpers, OPERATOR_MAP, defaultOperator, buildFilter, clearConditionValue } from './hooks/useSearchHelpers.js'
import type { ColumnConfig, FilterCondition, FilterResult } from './types.js'

/* ============ Props & Emits ============ */

const props = defineProps<{
  columns: ColumnConfig[]
  operatorOptions: { label: string; value: string }[]
  /** 初始默认筛选值（系统默认参数 / localStorage 恢复），JSON 深拷贝后解绑，仅用于组件初始化 */
  initialValues?: FilterResult[]
  loadOptions?: (type: string, keyword?: string) => Promise<{ label: string; value: string }[]>
  showAdminBtn?: boolean
}>()

const emit = defineEmits<{
  (e: 'filter', value: FilterResult[]): void
  (e: 'admin-confirm'): void
}>()

/* ============ 辅助 hook ============ */

const {
  getFieldType,
  getPickerType,
  isDateRangeField,
  getDateRangeType,
  getDateFormat,
  isSelectField,
  getOptions,
  getRemoteMethod,
  getAvailableOperators,
} = useSearchHelpers(
  toRef(props, 'columns'),
  toRef(props, 'loadOptions') as any,
  toRef(props, 'operatorOptions'),
)

/* ============ 条件状态 ============ */

const popoverVisible = ref(false)
const conditions = ref<FilterCondition[]>([])

const canSubmit = computed(() => conditions.value.every((c) => c.column))

function createCondition(initialColumn = ''): FilterCondition {
  const col = props.columns.find((c) => c.prop === initialColumn)
  const defaultOp = defaultOperator(col)
  return {
    column: initialColumn,
    operator: (col?.operator ?? defaultOp) as FilterCondition['operator'],
    value: '',
    valueStr: '',
    filterMode: col?.filterMode ?? 'hide',
  }
}

/* ============ 深拷贝初始化（蓝图 → 独立实例） ============ */

/** FilterResult → FilterCondition 转换（JSON 深拷贝后 columns 已解绑，col 来自副本） */
function conditionFromFilterResult(fv: FilterResult, cols: ColumnConfig[]): FilterCondition | null {
  if (fv.column === 'all') return null
  const col = cols.find(c => c.prop === fv.column)
  if (!col) return null
  const v = fv.value
  const valueStr = fv.operator === 'in' && Array.isArray(v) ? v.join(',') : ''
  return {
    column: fv.column,
    operator: fv.operator as FilterCondition['operator'],
    value:
      fv.operator === 'between' && Array.isArray(v)
        ? (v as [string, string])
        : fv.operator === 'in'
          ? ''
          : (v as string),
    valueStr,
    filterMode: col.filterMode ?? 'hide',
  }
}

/**
 * JSON 深拷贝蓝图 → 构建独立的初始 conditions
 * 通过 JSON 序列化/反序列化彻底断开与 props.columns 的引用关联
 */
function cloneFromBlueprint(
  cols: ColumnConfig[],
  initialValues?: FilterResult[],
): FilterCondition[] {
  // 🔑 JSON 深拷贝解绑（Blueprint 与 Instance 一刀两断）
  const blueprint: ColumnConfig[] = JSON.parse(JSON.stringify(cols))

  // ① 有显式初始值：系统默认参数 / localStorage 恢复
  if (initialValues?.length) {
    const result = initialValues
      .map(fv => conditionFromFilterResult(fv, blueprint))
      .filter(Boolean) as FilterCondition[]
    // 🔧 补充：filterMode === 'exposed' 但没有初始值的列也要创建条件
    const exposedNoValue = blueprint.filter(c => c.filterMode === 'exposed'
      && !initialValues.some(iv => iv.column === c.prop)
    )
    if (exposedNoValue.length) {
      console.log('[query.vue] cloneFromBlueprint 补充 exposed 条件:', exposedNoValue.map(c => `${c.prop}(filterMode=${c.filterMode})`))
      exposedNoValue.forEach(col => {
        result.push({
          column: col.prop,
          operator: (col.operator ?? defaultOperator(col)) as FilterCondition['operator'],
          value: '',
          valueStr: '',
          filterMode: 'exposed',
        })
      })
    }
    console.log('[query.vue] cloneFromBlueprint 路径① initialValues →', result.map(c => `${c.column}=${c.value} filterMode=${c.filterMode}`))
    return result
  }

  // ② 从蓝图默认值构建（ColumnConfig.value 有值的列）
  const defaults = blueprint.filter(c => {
    const v = c.value
    if (v === undefined || v === null) return false
    if (typeof v === 'string' && v === '') return false
    if (Array.isArray(v) && v.length === 0) return false
    return true
  })

  if (defaults.length) {
    const result = defaults.map(col => ({
      column: col.prop,
      operator: (col.operator ?? defaultOperator(col)) as FilterCondition['operator'],
      value: col.value! as any,
      valueStr: '',
      filterMode: col.filterMode ?? 'hide',
    }))
    console.log('[query.vue] cloneFromBlueprint 路径② blueprint默认值 →', result.map(c => `${c.column}=${c.value}`))
    return result
  }

  // ③ 没有默认值 → 用 filterMode !== 'hide' 的列创建空条件
  const visible = blueprint.filter(c => c.filterMode !== 'hide')
  if (visible.length) {
    console.log('[query.vue] cloneFromBlueprint 路径③ 空条件 → visible:', visible.map(c => c.prop))
    return visible.map(col => createCondition(col.prop))
  }

  // ④ 兜底：一个空条件
  console.log('[query.vue] cloneFromBlueprint 路径④ 兜底空条件')
  return [createCondition()]
}

/** 外部调用：重新初始化（如系统默认恢复 / 切换报表） */
function reinit(values?: FilterResult[]) {
  conditions.value = cloneFromBlueprint(props.columns, values)
  console.log('[query.vue] reinit 后 conditions:', conditions.value.map(c => `${c.column}=${c.value} filterMode=${c.filterMode}`))
}

/* ============ 初始化 ============ */
conditions.value = cloneFromBlueprint(props.columns, props.initialValues)
console.log('[query.vue] 初始化后 conditions:', conditions.value.map(c => `${c.column}=${c.value} filterMode=${c.filterMode}`))


/* ============ 条件操作 ============ */

function addCondition() {
  conditions.value.push(createCondition())
}

function removeCondition(index: number) {
  conditions.value.splice(index, 1)
}

function onColumnChange(index: number) {
  const col = props.columns.find((c) => c.prop === conditions.value[index].column)
  if (!col) {
    // 列被清空或不存在，重置整行条件
    conditions.value[index].operator = 'contains'
    conditions.value[index].value = ''
    conditions.value[index].valueStr = ''
    return
  }
  // 根据 fieldType 自动修正操作符
  const validOps = OPERATOR_MAP[col.fieldType ?? 'text'] ?? OPERATOR_MAP.text
  let nextOp = (col.operator ?? defaultOperator(col)) as FilterCondition['operator']
  if (!validOps.includes(nextOp)) {
    nextOp = validOps[0] as FilterCondition['operator']
  }
  // 切换列时清除旧值，避免上一列的值不匹配新列的选项导致显示 raw value
  conditions.value[index].operator = nextOp
  conditions.value[index].value = ''
  conditions.value[index].valueStr = ''
  if (col.filterMode === 'exposed') {
    conditions.value[index].filterMode = 'exposed'
  }
}

/* ============ 构建与提交 ============ */

function handleFilter() {
  const result = buildFilter(conditions.value)
  emit('filter', result)
  popoverVisible.value = false
}

function handleClear() {
  conditions.value.forEach(clearConditionValue)
}

/* ============ 暴露 ============ */

defineExpose({ conditions, reinit })
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
        flex: 1;
        min-width: 0;
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
    .cond-value-between { flex: 1; min-width: 80px; max-width: 140px; }
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
