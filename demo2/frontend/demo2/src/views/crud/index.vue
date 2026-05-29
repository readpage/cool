<template>
  <Crud :crud="crud" :form-items="formItems" :load-options="loadOptions">
    <Table
      id="crud-demo"
      :config="tableConfig"
      :data="tableData"
      selection
      :load-options="loadOptions"
      :showAdminBtn="false"
      @query="onQuery"
      @change="onConfigChange"
    />
  </Crud>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Crud from '@/components/crud/index.vue'
import Table from '@/components/table/index.vue'
import type { TableConfig, TableQuery } from '@/types/table'
import type { CrudApi } from '@/components/crud/types'
import { useTableConfigStore } from '@/store/table-config'
import { useOptionsStore } from '@/store/options'
import { initTableConfig, formItems } from './config'

/** api: POST /user/page ← 通过 Vite proxy 转发到后端 */
const API = '/api/user/page'

const $store = useTableConfigStore()
const tableData = ref<Record<string, any>[]>([])
const tableQuery = ref<TableQuery | null>(null)

// ==================== 选项加载器 ====================

const loadOptions = (type: string, keyword?: string) => useOptionsStore().getOptions(type, keyword)

// ==================== 配置 & 查询 ====================

/** 配置来源：store 缓存 → 代码兜底 */
const tableConfig = computed(() => $store.getConfig('crud-demo') ?? initTableConfig())

async function fetchList(payload: TableQuery) {
  try {
    const res = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
    const json = await res.json()
    tableData.value = json.data
  } catch (err) {
    console.error('获取用户列表失败', err)
  }
}

function onQuery(payload: TableQuery) {
  tableQuery.value = payload
  fetchList(payload)
}

function onConfigChange(_config: TableConfig) {
  $store.save('crud-demo', _config)
}

// ==================== CRUD API（放在此因需访问 tableQuery / fetchList） ====================

const crud: CrudApi = {
  save: (data, done) => {
    console.log('新增', data)
    // TODO: await fetch('/api/user/save', { method: 'POST', body: JSON.stringify(data) })
    // setTimeout(() => {
    //   done()
    //   tableQuery.value && fetchList(tableQuery.value) // 新增后刷新表格
    // }, 500)
  },
  update: (data, done) => {
    console.log('修改', data)
    // TODO: await fetch('/api/user/update', { method: 'PUT', body: JSON.stringify(data) })
    // setTimeout(() => {
    //   done()
    //   tableQuery.value && fetchList(tableQuery.value) // 修改后刷新表格
    // }, 500)
  },
  remove: (ids, done) => {
    console.log('删除', ids)
    // TODO: await fetch('/api/user/batch-delete', { method: 'POST', body: JSON.stringify({ ids }) })
    // setTimeout(() => {
    //   done()
    //   tableQuery.value && fetchList(tableQuery.value) // 删除后刷新表格
    // }, 500)
  },
  import: () => {
    console.log('导入')
  },
  export: () => {
    console.log('导出')
  },
}
</script>

<style lang="scss" scoped></style>
