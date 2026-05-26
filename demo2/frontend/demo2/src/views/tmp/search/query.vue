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

      <!-- 筛选面板 -->
      <div class="filter-panel">
        <!-- 顶部标题 -->
        <div class="panel-header">
          <span class="panel-title">设置筛选</span>
          <el-button text size="small" @click="popoverVisible = false">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>

        <!-- 中间条件区域 -->
        <div class="panel-body">
          <!-- 虚线边框包裹全部条件 -->
          <div class="condition-box">
            <!-- 单个"且"标签，垂直居中在分支线左侧 -->
            <span class="logic-badge">且</span>

            <div class="condition-list">
              <div
                v-for="(cond, index) in conditions"
                :key="index"
                class="condition-row"
              >
                <!-- 逻辑连线 + 分支 -->
                <div class="logic-col">
                  <div class="line-segment" :class="{ 'top-hidden': index === 0 }"></div>
                  <div class="line-node">
                    <!-- 分支横线 -->
                    <span class="branch-line"></span>
                  </div>
                  <div
                    class="line-segment"
                    :class="{ 'bottom-connect': index === conditions.length - 1 }"
                  ></div>
                </div>

                <div class="condition-content">
                  <!-- 字段选择 -->
                  <div class="cond-field-wrap">
                    <el-select
                      v-model="cond.column"
                      placeholder="可选择"
                      class="cond-field"
                      :teleported="false"
                      @change="onColumnChange(index)"
                    >
                      <el-option
                        v-for="col in columns"
                        :key="col.prop"
                        :label="col.label"
                        :value="col.prop"
                      />
                    </el-select>
                    <span v-if="!cond.column" class="field-error">请填写筛选条件</span>
                  </div>

                  <!-- 操作符选择 -->
                  <el-select
                    v-model="cond.operator"
                    placeholder="操作符"
                    class="cond-operator"
                    :teleported="false"
                  >
                    <el-option
                      v-for="op in operatorOptions"
                      :key="op.value"
                      :label="op.label"
                      :value="op.value"
                    />
                  </el-select>

                  <!-- 值输入 -->
                  <template v-if="cond.operator === 'between'">
                    <el-input
                      v-model="cond.value[0]"
                      placeholder="最小值"
                      class="cond-value"
                    />
                    <span class="between-sep">~</span>
                    <el-input
                      v-model="cond.value[1]"
                      placeholder="最大值"
                      class="cond-value"
                    />
                  </template>
                  <template v-else-if="cond.operator === 'in'">
                    <el-input
                      v-model="cond.valueStr"
                      placeholder="多个值用逗号分隔"
                      class="cond-value"
                    />
                  </template>
                  <template v-else>
                    <el-input
                      v-model="cond.value"
                      placeholder="请输入"
                      class="cond-value"
                    />
                  </template>

                  <!-- 删除按钮 -->
                  <el-icon
                    v-if="conditions.length > 1"
                    class="cond-delete"
                    @click="removeCondition(index)"
                  >
                    <Remove />
                  </el-icon>

                  <!-- 外露复选框 -->
                  <el-checkbox
                    v-model="cond.display"
                    class="cond-display"
                    :disabled="!cond.column"
                    @change="onDisplayChange(index)"
                  >
                    外露
                  </el-checkbox>
                </div>
              </div>

              <!-- 底部连线 + 添加按钮 -->
              <div class="add-condition-row">
                <div class="logic-col">
                  <div class="line-segment bottom-tail"></div>
                  <div class="add-btn" @click="addCondition">
                    <el-icon><Plus /></el-icon>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部分隔线 -->
        <el-divider class="panel-divider" />

        <!-- 底部按钮 -->
        <div class="panel-footer">
          <div class="footer-left">
            <el-button text>另存为</el-button>
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
import { ref, watch, computed } from 'vue'
import {
  Filter,
  Plus,
  Remove,
  Close,
} from '@element-plus/icons-vue'

/* ============ 类型定义 ============ */

interface ColumnConfig {
  prop: string
  label: string
  operator?: string
  width?: number
  align?: string
  minWidth?: number
}

interface FilterCondition {
  column: string
  operator: string
  value: string
  valueStr: string
  display: boolean
}

interface FilterResult {
  column: string
  operator: string
  value: string | [string, string] | string[]
}

/* ============ Props & Emits ============ */

const props = withDefaults(
  defineProps<{
    columns: ColumnConfig[]
    modelValue?: FilterResult[]
    exposed?: string[]
  }>(),
  {
    columns: () => [],
    modelValue: () => [],
    exposed: () => [],
  }
)

const emit = defineEmits<{
  (e: 'filter', value: FilterResult[]): void
  (e: 'update:modelValue', value: FilterResult[]): void
}>()

/* ============ 操作符选项 ============ */

const operatorOptions = [
  { label: '包含', value: 'contains' },
  { label: '等于', value: 'eq' },
  { label: '不等于', value: 'ne' },
  { label: '大于', value: 'gt' },
  { label: '小于', value: 'lt' },
  { label: '大于等于', value: 'gte' },
  { label: '小于等于', value: 'lte' },
  { label: '区间', value: 'between' },
  { label: '属于', value: 'in' },
]

/* ============ 筛选条件 ============ */

const popoverVisible = ref(false)

const canSubmit = computed(() =>
  conditions.value.every((c) => c.column)
)

function createCondition(initialColumn = ''): FilterCondition {
  return {
    column: initialColumn,
    operator: 'contains',
    value: '',
    valueStr: '',
    display: props.exposed?.includes(initialColumn) ?? false,
  }
}

/* ============ 初始化默认外露条件 ============ */

function initExposedConditions() {
  if (!props.exposed || props.exposed.length === 0) {
    conditions.value.push(createCondition())
    return
  }
  props.exposed.forEach((prop) => {
    const col = props.columns.find((c) => c.prop === prop)
    if (col) {
      conditions.value.push(createCondition(prop))
    }
  })
}

const conditions = ref<FilterCondition[]>([])

initExposedConditions()

function addCondition() {
  conditions.value.push(createCondition())
}

function removeCondition(index: number) {
  conditions.value.splice(index, 1)
}

function onColumnChange(index: number) {
  const col = props.columns.find((c) => c.prop === conditions.value[index].column)
  if (col) {
    conditions.value[index].operator = col.operator ?? 'contains'
    // 如果该字段在 exposed 列表中，自动勾选外露
    if (props.exposed?.includes(col.prop)) {
      conditions.value[index].display = true
    }
  }
}

function onDisplayChange(_index: number) {
  // 仅触发响应
}

/* ============ 构建提交数据 ============ */

function buildFilterResult(): FilterResult[] {
  return conditions.value
    .filter((c) => c.column)
    .filter((c) => {
      if (c.operator === 'between') {
        return c.value[0] !== '' || c.value[1] !== ''
      }
      if (c.operator === 'in') {
        return c.valueStr !== ''
      }
      return c.value !== ''
    })
    .map((c) => {
      let value: string | [string, string] | string[]
      if (c.operator === 'between') {
        value = [c.value[0] ?? '', c.value[1] ?? '']
      } else if (c.operator === 'in') {
        value = c.valueStr
          .split(',')
          .map((v) => v.trim())
          .filter(Boolean)
      } else {
        value = c.value
      }
      return { column: c.column, operator: c.operator, value }
    })
}

function handleFilter() {
  const result = buildFilterResult()
  emit('update:modelValue', result)
  emit('filter', result)
  popoverVisible.value = false
}

function handleClear() {
  conditions.value.length = 0
  conditions.value.push(createCondition())
  emit('update:modelValue', [])
  emit('filter', [])
}

/* ============ 初始化 modelValue ============ */

watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal.length === 0) return
    conditions.value.length = 0
    newVal.forEach((item) => {
      const cond: FilterCondition = {
        column: item.column ?? '',
        operator: item.operator ?? 'contains',
        value: '',
        valueStr: '',
        display: false,
      }
      if (item.operator === 'between' && Array.isArray(item.value)) {
        cond.value = item.value as any
      } else if (item.operator === 'in' && Array.isArray(item.value)) {
        cond.valueStr = (item.value as string[]).join(',')
      } else {
        cond.value = (item.value as string) ?? ''
      }
      conditions.value.push(cond)
    })
  },
  { immediate: true }
)

/* ============ 暴露给父组件 ============ */

defineExpose({
  conditions,
  columns: props.columns,
  operatorOptions,
})
</script>

<style lang="scss" scoped>
.filter-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

/* ============ 筛选面板 ============ */

.filter-panel {
  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .panel-title {
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
  }

  .panel-body {
    .condition-list {
      display: flex;
      flex-direction: column;
    }

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

    // ===== 左侧连线区域 =====
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

      &.top-hidden {
        visibility: hidden;
      }

      // 最后一行：底部线段延长接入+按钮
      &.bottom-connect {
        min-height: 12px;
      }

      &.bottom-tail {
        flex: none;
        height: 14px;
        background: #dcdfe6;
      }
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
      height: 0;
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

      &:hover {
        background: #ecf5ff;
      }
    }

    .condition-box {
      position: relative;
      border: 2px dashed #dcdfe6;
      border-radius: 4px;
      padding: 12px;
      padding-left: 40px; // 给"且"标签留空间
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

    .cond-operator {
      width: 100px;
      flex-shrink: 0;
    }

    .cond-field-wrap {
      width: 110px;
      flex-shrink: 0;
      position: relative;

      .cond-field {
        width: 100%;
      }
    }

    .field-error {
      position: absolute;
      left: 0;
      bottom: -18px;
      font-size: 11px;
      color: #f56c6c;
      white-space: nowrap;
    }

    .cond-value {
      flex: 1;
      min-width: 100px;
    }

    .between-sep {
      color: #909399;
      flex-shrink: 0;
    }

    .cond-display {
      flex-shrink: 0;
    }

    .cond-delete {
      flex-shrink: 0;
      font-size: 18px;
      color: #f56c6c;
      cursor: pointer;

      &:hover {
        color: #f89898;
      }
    }
  }

  .panel-divider {
    margin: 12px 0;
  }

  .panel-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .footer-left {
      flex-shrink: 0;
    }

    .footer-right {
      display: flex;
      gap: 8px;
      margin-left: auto;
    }
  }
}
</style>

<style lang="scss">
/* 全局样式，避免 scoped 限制 */
.filter-popper {
  padding: 16px !important;
}
</style>
