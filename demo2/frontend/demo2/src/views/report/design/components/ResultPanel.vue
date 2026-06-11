<template>
  <div class="panel-bottom">
    <div class="panel-header">
      <span class="panel-title">结果</span>
      <span v-if="resultData" class="result-count">共 {{ resultData.total }} 条</span>
    </div>

    <div class="panel-body">
      <Table
        v-if="resultData || tableConfig.columns.length"
        id="report-exec"
        :config="tableConfig"
        :data="tableData"
        :show-admin-btn="false"
        :export="export"
        :load-options="loadOptions"
        @query="(v, done) => $emit('query', v, done)"
      />
      <el-empty v-else description="点击右侧「▶ 运行」执行查询" :image-size="100" />
    </div>
  </div>
</template>

<script setup lang="ts">
import Table, { type ExportParams } from '@/components/table/index.vue'
import type { ReportQueryResult } from '../../types/report'
import type { TableConfig, TableQuery, PageResult } from '../../types/table'

defineProps<{
  resultData: ReportQueryResult | null
  tableConfig: TableConfig
  tableData: PageResult
  export?: (params: ExportParams) => Promise<boolean | void>
  loadOptions?: (type: string, keyword?: string) => Promise<{ label: string; value: string }[]>
}>()

type DoneFn = () => void

defineEmits<{
  query: [payload: TableQuery, done: DoneFn]
  execute: []
}>()
</script>

<style lang="scss" scoped>
.panel-bottom {
  flex: 1;
  min-height: 150px;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 36px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}

.panel-title {
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
    background: #409eff;
    border-radius: 2px;
  }
}

.result-count {
  font-size: 12px;
  color: #999;
}

.panel-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  background: #fff;

  .el-empty {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
}

// 结果表格
:deep(.u-table) {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.u-table-toolbar) {
  flex-shrink: 0;

  .table-settings {
    margin-left: auto;
  }
}

:deep(.u-table-wrapper) {
  flex: 1;
  min-height: 0;
  overflow: auto;
}
</style>
