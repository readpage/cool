<template>
  <div class="report-param-form">
    <el-form :model="values" inline>
      <template v-for="p in parameters" :key="p.name">
        <el-form-item :label="p.label" :required="p.required">
          <!-- 日期范围 -->
          <el-date-picker
            v-if="p.type === 'daterange'"
            v-model="values[p.name]"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 240px"
            @change="emitChange"
          />
          <!-- 日期 -->
          <el-date-picker
            v-else-if="p.type === 'date'"
            v-model="values[p.name]"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 180px"
            @change="emitChange"
          />
          <!-- 远程下拉 -->
          <el-select
            v-else-if="p.type === 'remote-select'"
            v-model="values[p.name]"
            filterable
            remote
            reserve-keyword
            :remote-method="(kw: string) => loadRemoteOptions(p, kw)"
            :loading="loadingMap[p.name]"
            placeholder="请选择"
            style="width: 180px"
            @change="emitChange"
          >
            <el-option
              v-for="o in remoteOptionsMap[p.name] ?? []"
              :key="o.value"
              :label="o.label"
              :value="o.value"
            />
          </el-select>
          <!-- 静态下拉 -->
          <el-select
            v-else-if="p.type === 'select'"
            v-model="values[p.name]"
            placeholder="请选择"
            style="width: 180px"
            @change="emitChange"
          >
            <el-option
              v-for="o in p.options ?? []"
              :key="o.value"
              :label="o.label"
              :value="o.value"
            />
          </el-select>
          <!-- 数字 -->
          <el-input-number
            v-else-if="p.type === 'number'"
            v-model="values[p.name]"
            style="width: 180px"
            @change="emitChange"
          />
          <!-- 默认文本 -->
          <el-input
            v-else
            v-model="values[p.name]"
            placeholder="请输入"
            style="width: 180px"
            @input="emitChange"
          />
        </el-form-item>
      </template>

      <el-form-item v-if="parameters.length > 0">
        <el-button type="primary" @click="$emit('query', values)">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-empty v-if="parameters.length === 0" description="该报告没有查询参数" :image-size="60" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import type { ParamDef, OptionDef } from '@/types/report'

const props = defineProps<{
  parameters: ParamDef[]
  /** 远程加载选项的函数（可选，复用 options Store） */
  loadOptions?: (type: string, keyword?: string) => Promise<OptionDef[]>
}>()

const emit = defineEmits<{
  query: [values: Record<string, any>]
  change: [values: Record<string, any>]
}>()

// 表单值
const values = reactive<Record<string, any>>({})

// 远程选项缓存
const remoteOptionsMap = reactive<Record<string, OptionDef[]>>({})
const loadingMap = reactive<Record<string, boolean>>({})

// 初始化默认值
function initDefaults() {
  for (const p of props.parameters) {
    if (p.defaultValue !== undefined && p.defaultValue !== null) {
      if (p.type === 'daterange') {
        // 默认值格式：逗号分隔 "2024-01-01,2024-01-31"
        const parts = String(p.defaultValue).split(',')
        if (parts.length === 2) values[p.name] = parts
      } else if (p.type === 'number') {
        values[p.name] = Number(p.defaultValue)
      } else {
        values[p.name] = p.defaultValue
      }
    }

    // 预加载远程选项
    if (p.type === 'remote-select' && p.optionType && props.loadOptions) {
      loadRemoteOptions(p)
    }
  }
}

// 远程加载选项
async function loadRemoteOptions(p: ParamDef, keyword?: string) {
  if (!props.loadOptions || !p.optionType) return
  const key = p.optionType ?? p.name
  loadingMap[p.name] = true
  try {
    const items = await props.loadOptions(key, keyword)
    remoteOptionsMap[p.name] = items
  } catch {
    console.warn(`[ReportParamForm] 加载远程选项失败: ${key}`)
  } finally {
    loadingMap[p.name] = false
  }
}

function emitChange() {
  // 过滤空值
  const cleanValues: Record<string, any> = {}
  for (const key of Object.keys(values)) {
    const v = values[key]
    if (v !== undefined && v !== null && v !== '') {
      // 数组至少有一个值
      if (Array.isArray(v) && v.length === 0) continue
      cleanValues[key] = v
    }
  }
  emit('change', cleanValues)
}

function reset() {
  for (const p of props.parameters) {
    delete values[p.name]
  }
  initDefaults()
  emit('change', {})
}

// 参数定义变化时重新初始化
watch(() => props.parameters, () => {
  for (const key of Object.keys(values)) {
    delete values[key]
  }
  initDefaults()
}, { deep: true })

onMounted(() => initDefaults())
</script>

<style lang="scss" scoped>
.report-param-form {
  padding: 12px 0;
}
</style>
