<template>
  <el-drawer
    :model-value="visible"
    :title="title"
    :size="width"
    :close-on-click-modal="false"
    destroy-on-close
    @update:model-value="$emit('update:visible', $event)"
  >
    <!-- ★ 动态表单 -->
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="80px"
      status-icon
    >
      <template v-for="item in visibleItems" :key="item.prop">
        <el-form-item
          :label="item.label"
          :prop="item.prop"
          :style="item.width ? { width: typeof item.width === 'number' ? item.width + 'px' : item.width } : undefined"
          :label-width="item.labelWidth"
        >
          <!-- input -->
          <el-input
            v-if="!item.fieldType || item.fieldType === 'input'"
            v-model="formData[item.prop]"
            :placeholder="item.placeholder || `请输入${item.label}`"
            :disabled="item.disabled"
            :clearable="item.clearable !== false"
            v-bind="item.componentProps"
            v-on="item.event || {}"
          />

          <!-- number -->
          <el-input-number
            v-else-if="item.fieldType === 'number'"
            v-model="formData[item.prop]"
            :disabled="item.disabled"
            style="width: 100%"
            v-bind="item.componentProps"
            v-on="item.event || {}"
          />

          <!-- textarea -->
          <el-input
            v-else-if="item.fieldType === 'textarea'"
            v-model="formData[item.prop]"
            type="textarea"
            :rows="3"
            :placeholder="item.placeholder || `请输入${item.label}`"
            :disabled="item.disabled"
            :clearable="item.clearable !== false"
            v-bind="item.componentProps"
            v-on="item.event || {}"
          />

          <!-- select（静态选项） -->
          <el-select
            v-else-if="item.fieldType === 'select'"
            v-model="formData[item.prop]"
            :placeholder="item.placeholder || `请选择${item.label}`"
            :disabled="item.disabled"
            :clearable="item.clearable !== false"
            v-bind="item.componentProps"
            v-on="item.event || {}"
          >
            <el-option
              v-for="opt in normalizeOptions(item.options)"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>

          <!-- remote-select（远程搜索） -->
          <el-select
            v-else-if="item.fieldType === 'remote-select'"
            v-model="formData[item.prop]"
            :placeholder="item.placeholder || `请选择${item.label}`"
            :disabled="item.disabled"
            filterable
            remote
            :remote-method="(kw: string) => handleRemoteSearch(item.prop, item.optionType || item.prop, kw)"
            :loading="remoteLoading[item.prop]"
            :clearable="item.clearable !== false"
            v-bind="item.componentProps"
            v-on="item.event || {}"
          >
            <el-option
              v-for="opt in (remoteOptionMap[item.prop] || [])"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>

          <!-- date -->
          <el-date-picker
            v-else-if="item.fieldType === 'date'"
            v-model="formData[item.prop]"
            type="date"
            value-format="YYYY-MM-DD"
            :placeholder="item.placeholder || `请选择${item.label}`"
            :disabled="item.disabled"
            style="width: 100%"
            v-bind="item.componentProps"
            v-on="item.event || {}"
          />

          <!-- daterange -->
          <el-date-picker
            v-else-if="item.fieldType === 'daterange'"
            v-model="formData[item.prop]"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            :disabled="item.disabled"
            style="width: 100%"
            v-bind="item.componentProps"
            v-on="item.event || {}"
          />

          <!-- switch -->
          <el-switch
            v-else-if="item.fieldType === 'switch'"
            v-model="formData[item.prop]"
            :disabled="item.disabled"
            v-bind="item.componentProps"
            v-on="item.event || {}"
          />

          <!-- ★ 自定义 slot 出口 -->
          <slot
            v-else
            :name="`form-${item.prop}`"
            :item="item"
            :data="formData"
          />
        </el-form-item>
      </template>
    </el-form>

    <template #footer>
      <div class="u-form-drawer-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确认</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch, computed, nextTick } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { CrudApi, FormItemConfig } from './types'
import type { OptionItem } from '@/types/table'

const props = withDefaults(defineProps<{
  visible: boolean
  row?: Record<string, any> | null
  crud: CrudApi
  /** 表单字段配置 */
  formItems?: FormItemConfig[]
  /** 远程选项加载器，复用 optionsStore */
  loadOptions?: (type: string, keyword?: string) => Promise<{ label: string; value: string }[]>
  /** 抽屉宽度 */
  width?: string | number
}>(), {
  width: '50%',
})

const emit = defineEmits<{
  'update:visible': [val: boolean]
  saved: []
}>()

const formRef = ref<FormInstance>()
const formData = ref<Record<string, any>>({})
const saving = ref(false)

const isEdit = computed(() => !!props.row)
const title = computed(() => isEdit.value ? '修改' : '新增')

// ==================== 可见字段（过滤 hideAdd / hideEdit） ====================

const visibleItems = computed(() => {
  const items = props.formItems ?? []
  return items.filter(item => {
    if (isEdit.value && item.hideEdit) return false
    if (!isEdit.value && item.hideAdd) return false
    return true
  })
})

// ==================== 自动生成校验规则 ====================

const formRules = computed<FormRules>(() => {
  const rules: FormRules = {}
  for (const item of visibleItems.value) {
    const itemRules: any[] = []
    if (item.required) {
      itemRules.push({ required: true, message: `${item.label}不能为空`, trigger: ['blur', 'change'] })
    }
    if (item.rules) {
      const arr = Array.isArray(item.rules) ? item.rules : [item.rules]
      itemRules.push(...arr)
    }
    if (itemRules.length) rules[item.prop] = itemRules
  }
  return rules
})

// ==================== 远程选项 ====================

const remoteOptionMap = ref<Record<string, { label: string; value: string }[]>>({})
const remoteLoading = ref<Record<string, boolean>>({})

async function handleRemoteSearch(prop: string, optionType: string, keyword: string) {
  if (!props.loadOptions) return
  remoteLoading.value[prop] = true
  try {
    const items = await props.loadOptions(optionType, keyword)
    remoteOptionMap.value[prop] = items
  } finally {
    remoteLoading.value[prop] = false
  }
}

// ==================== 打开时重置数据 + 预加载选项 ====================

watch(() => props.visible, async (val) => {
  if (!val) return

  // 1. 深拷贝行数据 / 空对象
  formData.value = props.row ? { ...props.row } : {}

  // 2. 应用默认值
  for (const item of (props.formItems ?? [])) {
    if (item.defaultValue !== undefined && formData.value[item.prop] === undefined) {
      formData.value[item.prop] = item.defaultValue
    }
  }

  // 3. 预加载 remote-select 选项（optionType 优先，fallback 到 prop）
  const remoteFields = (props.formItems ?? [])
    .filter(f => f.fieldType === 'remote-select')
    .map(f => ({ prop: f.prop, optionType: f.optionType || f.prop }))

  if (props.loadOptions && remoteFields.length > 0) {
    for (const { prop, optionType } of remoteFields) {
      remoteLoading.value[prop] = true
      try {
        const items = await props.loadOptions(optionType)
        remoteOptionMap.value[prop] = items
      } catch { /* ignore */ }
      remoteLoading.value[prop] = false
    }
  }

  // 4. 清除校验
  nextTick(() => formRef.value?.clearValidate())
})

// ==================== 选项规范化 ====================

function normalizeOptions(options?: (OptionItem | string)[]) {
  if (!options) return []
  return options.map(opt =>
    typeof opt === 'string' ? { label: opt, value: opt } : opt
  )
}

// ==================== 取消 / 保存 ====================

function handleCancel() {
  emit('update:visible', false)
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return // 校验失败，不继续
  }

  if (isEdit.value) {
    if (!props.crud.update) return
    props.crud.beforeUpdate?.(formData.value)
    saving.value = true
    props.crud.update(formData.value, (success = true) => {
      saving.value = false
      if (success) {
        ElMessage.success('修改成功')
        emit('update:visible', false)
        emit('saved')
      } else {
        ElMessage.error('修改失败')
      }
    })
  } else {
    if (!props.crud.save) return
    props.crud.beforeSave?.(formData.value)
    saving.value = true
    props.crud.save(formData.value, (success = true) => {
      saving.value = false
      if (success) {
        ElMessage.success('新增成功')
        emit('update:visible', false)
        emit('saved')
      } else {
        ElMessage.error('新增失败')
      }
    })
  }
}
</script>

<style lang="scss" scoped>
.u-form-drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
