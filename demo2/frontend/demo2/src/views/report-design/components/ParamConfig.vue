<template>
  <div class="param-config">
    <div class="config-header">
      <span class="config-title">参数配置</span>
      <el-button size="small" type="primary" plain @click="addParam">
        <el-icon><Plus /></el-icon> 添加
      </el-button>
    </div>

    <div class="config-body">
      <el-empty v-if="parameters.length === 0" description="暂无参数，请先检测 SQL 参数或手动添加" :image-size="60" />
      <div v-else class="param-list">
        <div v-for="(p, idx) in parameters" :key="idx" class="param-item">
          <div class="param-header">
            <code class="param-name">{{ paramDisplay(p.name) }}</code>
            <el-button
              type="danger"
              link
              size="small"
              @click="removeParam(idx)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>

          <el-form label-position="top" size="small">
            <el-form-item label="标签">
              <el-input v-model="p.label" placeholder="显示名称" />
            </el-form-item>

            <el-form-item label="类型">
              <el-select v-model="p.type" style="width:100%">
                <el-option label="文本" value="text" />
                <el-option label="数字" value="number" />
                <el-option label="日期" value="date" />
                <el-option label="日期范围" value="daterange" />
                <el-option label="下拉选择" value="select" />
                <el-option label="远程搜索" value="remote-select" />
              </el-select>
            </el-form-item>

            <el-form-item label="必填">
              <el-switch v-model="p.required" size="small" />
            </el-form-item>

            <!-- 默认值 -->
            <el-form-item
              v-if="['text', 'number'].includes(p.type)"
              label="默认值"
            >
              <el-input-number
                v-if="p.type === 'number'"
                v-model="p.defaultValue"
                :controls="false"
                style="width:100%"
              />
              <el-input v-else v-model="p.defaultValue" placeholder="可选默认值" />
            </el-form-item>
            <el-form-item v-else-if="p.type === 'date'" label="默认值">
              <el-date-picker
                v-model="p.defaultValue"
                type="date"
                value-format="YYYY-MM-DD"
                style="width:100%"
              />
            </el-form-item>
            <el-form-item v-else-if="p.type === 'daterange'" label="默认值">
              <el-date-picker
                v-model="p.defaultValue"
                type="daterange"
                range-separator="至"
                start-placeholder="开始" end-placeholder="结束"
                value-format="YYYY-MM-DD"
                style="width:100%"
              />
            </el-form-item>

            <!-- select 静态选项 -->
            <el-form-item v-if="p.type === 'select'" label="下拉选项">
              <div class="options-editor">
                <div v-for="(opt, oi) in (p.options || [])" :key="oi" class="option-row">
                  <el-input v-model="opt.label" placeholder="标签" size="small" style="width:40%" />
                  <el-input v-model="opt.value" placeholder="值" size="small" style="width:40%" />
                  <el-button type="danger" link size="small" @click="p.options!.splice(oi, 1)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                <el-button size="small" link @click="addOption(p)">+ 添加选项</el-button>
              </div>
            </el-form-item>

            <!-- remote-select 远程类型 -->
            <el-form-item v-if="p.type === 'remote-select'" label="远程选项类型">
              <el-input v-model="p.optionType" placeholder="optionType 标识" />
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus, Delete } from '@element-plus/icons-vue'
import type { ParamDef } from '@/types/report'

const props = defineProps<{
  parameters: ParamDef[]
}>()

const emit = defineEmits<{
  'update:parameters': [value: ParamDef[]]
}>()

function paramDisplay(name: string) {
  return '#{' + name + '}'
}

function emitChange() {
  emit('update:parameters', [...props.parameters])
}

function addParam() {
  const newParam: ParamDef = {
    name: `param_${Date.now()}`,
    label: '',
    type: 'text',
    required: false,
  }
  emit('update:parameters', [...props.parameters, newParam])
}

function removeParam(idx: number) {
  const copy = [...props.parameters]
  copy.splice(idx, 1)
  emit('update:parameters', copy)
}

function addOption(p: ParamDef) {
  if (!p.options) p.options = []
  p.options.push({ label: '', value: '' })
  emitChange()
}
</script>

<style lang="scss" scoped>
.param-config {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.config-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 40px;
  padding: 0 14px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.config-title {
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
    background: #e6a23c;
    border-radius: 2px;
  }
}

.config-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 12px;
}

.param-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.param-item {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  padding: 10px 12px;
}

.param-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #f0f0f0;
}

.param-name {
  font-size: 13px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
}

:deep(.el-form-item) {
  margin-bottom: 8px;
}

:deep(.el-form-item__label) {
  font-size: 12px;
  color: #909399;
  padding-bottom: 2px;
  line-height: 1.4;
}

.options-editor {
  width: 100%;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.config-body::-webkit-scrollbar {
  width: 4px;
}
.config-body::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 2px;
}
</style>
