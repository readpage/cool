<template>
  <Crud :crud="crud" :form-items="formItems" :load-options="loadOptions">
    <Table
      id="crud-demo"
      :config="tableConfig"
      :data="tableData"
      :export="handleExport"
      selection
      :load-options="loadOptions"
      :showAdminBtn="false"
      @query="onQuery"
      @change="onConfigChange"
      @reset="onResetSystem"
    />

  </Crud>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import Crud from '@/components/crud/index.vue'
import Table, { type ExportParams } from '@/components/table/index.vue'
import type { TableConfig, TableQuery, PageResult } from '@/components/table/types'
import type { CrudApi } from '@/components/crud/types'
import { useTableConfigStore } from '@/store/table-config'
import { useOptionsStore } from '@/store/options'
import { initTableConfig, formItems } from './config'
import { AUser } from '@/api'

const $store = useTableConfigStore()
const tableData = ref<PageResult>({ list: [], total: 0 })
const tableConfig = computed<TableConfig>(() => $store.getConfig('crud-demo') ?? initTableConfig())

// ==================== 选项加载器 ====================

const loadOptions = (type: string, keyword?: string) => useOptionsStore().getOptions(type, keyword)

// ==================== 配置 & 查询 ====================

async function onQuery(v: TableQuery, done: () => void) {
  try {
    const res = await AUser.page(v)
    tableData.value = res.data
  } catch (err) {
    console.error('获取用户列表失败', err)
  } finally {
    done()
  }
}

function onConfigChange(config: TableConfig, isAdmin?: boolean) {
  if (isAdmin) {
    $store.saveAsSystem('crud-demo', config)
  } else {
    $store.save('crud-demo', config)
  }
}

/** 恢复系统默认 → 清除用户缓存，拉取后台默认配置 */
async function onResetSystem() {
  await $store.resetToSystem('crud-demo', initTableConfig())
}

// ==================== 导出 ====================

async function handleExport(params: ExportParams) {
  return AUser.export(params)
    .then(ok => { if (!ok) ElMessage.warning('导出文件为空') })
    .catch(() => { ElMessage.error('导出失败') })
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
      await AUser.remove(ids)
      done(true)
    } catch (err) {
      console.error('删除失败', err)
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
}
</script>

<style lang="scss" scoped></style>
