<template>
  <div class="panel-top" :style="{ height: topHeight + 'px' }">
    <!-- 导航栏 -->
    <div class="panel-navbar">
      <div class="navbar-left">
        <el-button link class="back-btn" @click="$emit('back')">
          ← 返回
        </el-button>
        <div class="report-name-wrap">
          <el-input
            :model-value="reportName"
            @update:model-value="$emit('update:reportName', $event)"
            placeholder="未命名报告"
            class="name-input"
            size="small"
            :maxlength="50"
          />
          <span v-if="!isSaved" class="unsaved-dot" title="有未保存的修改">●</span>
        </div>
        <el-input
          :model-value="category"
          @update:model-value="$emit('update:category', $event)"
          placeholder="分类"
          class="category-input"
          size="small"
          :maxlength="20"
          clearable
        />
        <el-select
          :model-value="datasourceId"
          @update:model-value="$emit('update:datasourceId', $event)"
          placeholder="数据源"
          size="small"
          class="datasource-select"
          clearable
          filterable
        >
          <el-option label="默认主数据源" value="" />
          <el-option
            v-for="ds in datasourceList"
            :key="ds.id"
            :label="ds.name"
            :value="ds.id"
          />
        </el-select>
      </div>
      <div class="navbar-center">
        <span class="navbar-hint">
          <code>{<!-- -->{filter}}</code> 筛选 &nbsp;|&nbsp; <code>{<!-- -->{sort}}</code> 排序 &nbsp;|&nbsp; <code>#{key}</code> 变量 &nbsp;|&nbsp; <code>[[...]]</code> 可选块
        </span>
      </div>
      <div class="navbar-right">
        <span v-if="isSaved && lastSavedTime" class="save-status">已保存 {{ lastSavedTime }}</span>
        <el-button type="primary" size="small" :loading="loading" @click="$emit('run')">
          ▶ 运行
        </el-button>
        <el-button type="success" size="small" @click="$emit('save')">
          💾 保存
        </el-button>
      </div>
    </div>

    <div class="panel-body">
      <CodeEditor
        language="sql"
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
import { ref, onMounted } from 'vue'
import CodeEditor from '@/components/code-editor/CodeEditor.vue'
import { ADatasource } from '@/api/datasource'
import type { DatasourceEntity } from '@/api/datasource'

withDefaults(defineProps<{
  modelValue: string
  topHeight: number
  loading?: boolean
  placeholder?: string
  reportName?: string
  isSaved?: boolean
  lastSavedTime?: string
  datasourceId?: string | number | null
  category?: string
}>(), {
  loading: false,
  placeholder: "SELECT prod_num '编码', prod_name '名称' FROM itm WHERE 1=1 {{filter}} {{sort}}",
  reportName: '',
  isSaved: true,
  lastSavedTime: '',
  datasourceId: null,
  category: '',
})

defineEmits<{
  'update:modelValue': [value: string]
  'update:reportName': [value: string]
  'update:datasourceId': [value: string | number | null]
  'update:category': [value: string]
  run: []
  save: []
  back: []
}>()

const datasourceList = ref<DatasourceEntity[]>([])

onMounted(async () => {
  try {
    const { data } = await ADatasource.list({})
    datasourceList.value = data ?? []
  } catch (e) {
    console.error('获取数据源列表失败', e)
  }
})
</script>

<style lang="scss" scoped>
.panel-top {
  display: flex;
  flex-direction: column;
  min-height: 100px;
  overflow: hidden;
}

// ==================== 导航栏 ====================

.panel-navbar {
  display: flex;
  align-items: center;
  height: 44px;
  padding: 0 12px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
  gap: 12px;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.back-btn {
  font-size: 13px;
  color: #606266;
  padding: 4px 6px;
}

.report-name-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
}

.name-input {
  width: 180px;

  :deep(.el-input__wrapper) {
    box-shadow: none;
    background: transparent;
    padding: 0 8px;
    border-radius: 4px;
  }

  :deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
  }

  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #409eff inset;
  }

  :deep(.el-input__inner) {
    font-size: 14px;
    font-weight: 500;
    color: #303133;

    &::placeholder {
      color: #c0c4cc;
      font-weight: 400;
    }
  }
}

.unsaved-dot {
  color: #e6a23c;
  font-size: 10px;
  line-height: 1;
  flex-shrink: 0;
  cursor: default;
}

.category-input {
  width: 100px;
  flex-shrink: 0;
}

.datasource-select {
  width: 150px;
  flex-shrink: 0;
}

.navbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 0;
}

.navbar-hint {
  font-size: 11px;
  color: #bbb;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  code {
    background: #f5f5f5;
    padding: 1px 5px;
    border-radius: 3px;
    font-size: 11px;
    color: #909399;
  }
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.save-status {
  font-size: 12px;
  color: #67c23a;
  white-space: nowrap;
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

.action-save {
  width: 48px;
  height: 48px;
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 6px 18px rgba(103, 194, 58, 0.55);
  }

  .save-icon {
    font-size: 18px;
  }
}
</style>
