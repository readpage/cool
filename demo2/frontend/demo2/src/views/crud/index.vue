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
import { ElMessage, ElMessageBox } from 'element-plus'
import Crud from '@/components/crud/index.vue'
import Table from '@/components/table/index.vue'
import type { TableConfig, TableQuery, PageResult } from '@/types/table'
import type { CrudApi } from '@/components/crud/types'
import { useTableConfigStore } from '@/store/table-config'
import { useOptionsStore } from '@/store/options'
import { initTableConfig, formItems } from './config'
import { AUser } from '@/api'

const $store = useTableConfigStore()
const tableData = ref<PageResult>({ list: [], total: 0 })
const tableConfig = computed(() => $store.getConfig('crud-demo') ?? initTableConfig())

// ==================== 选项加载器 ====================

const loadOptions = (type: string, keyword?: string) => useOptionsStore().getOptions(type, keyword)

// ==================== 配置 & 查询 ====================

async function onQuery(v: TableQuery) {
  try {
    const res = await AUser.page(v)
    tableData.value = res.data
  } catch (err) {
    console.error('获取用户列表失败', err)
  }
}

function onConfigChange(_config: TableConfig) {
  $store.save('crud-demo', _config)
}

// ==================== CRUD API ====================

const crud: CrudApi = {
  save: async (data, done) => {
    try {
      await AUser.save(data)
      done(true)
    } catch (err) {
      console.error('新增用户失败', err)
      done(false)
    }
  },
  update: async (data, done) => {
    try {
      await AUser.save(data)
      done(true)
    } catch (err) {
      console.error('修改用户失败', err)
      done(false)
    }
  },
  remove: async (ids, done) => {
    try {
      await ElMessageBox.confirm(`确定删除选中的 ${ids.length} 条数据？`, '批量删除', { type: 'warning' })
      await AUser.remove(ids)
      done(true)
    } catch {
      done(false)
    }
  },
  downloadTemplate: () => AUser.downloadTemplate(),
  import: (params, done) => {
    const formData = new FormData()
    formData.append('file', params.file)
    formData.append('filterParam', JSON.stringify(params.config))
    AUser.importExcel(formData)
      .then(res => done(res?.code === 0 || res?.code === 200, res))
      .catch(() => done(false))
  },
  export: (data) => {
    console.log(data)
    AUser.export(data)
      .then(ok => { if (!ok) ElMessage.warning('导出文件为空') })
      .catch(() => ElMessage.error('导出失败'))
  },
}
</script>

<style lang="scss" scoped></style>
