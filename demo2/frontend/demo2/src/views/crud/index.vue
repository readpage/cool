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
import type { TableConfig, TableQuery, PageResult } from '@/types/table'
import type { CrudApi } from '@/components/crud/types'
import { useTableConfigStore } from '@/store/table-config'
import { useOptionsStore } from '@/store/options'
import { initTableConfig, formItems } from './config'
import { pageUser, saveUser, removeUsers } from '@/api'

const $store = useTableConfigStore()
const tableData = ref<PageResult>({ list: [], total: 0 })

// ==================== 选项加载器 ====================

const loadOptions = (type: string, keyword?: string) => useOptionsStore().getOptions(type, keyword)

// ==================== 配置 & 查询 ====================

/** 配置来源：store 缓存 → 代码兜底 */
const tableConfig = computed(() => $store.getConfig('crud-demo') ?? initTableConfig())
async function onQuery(v: TableQuery) {
  try {
    const res = await pageUser( v)
    tableData.value = res.data
  } catch (err) {
    console.error('获取用户列表失败', err)
  }
}

function onConfigChange(_config: TableConfig) {
  $store.save('crud-demo', _config)
}

// ==================== CRUD API（放在此因需访问 tableQuery / fetchList） ====================

const crud: CrudApi = {
  save: async (data, done) => {
    try {
      await saveUser(data)
      done(true)
    } catch (err) {
      console.error('新增用户失败', err)
      done(false)
    }
  },
  update: async (data, done) => {
    try {
      await saveUser(data)
      done(true)
    } catch (err) {
      console.error('修改用户失败', err)
      done(false)
    }
  },
  remove: async (ids, done) => {
    try {
      await removeUsers(ids)
      done(true)
    } catch (err) {
      console.error('删除用户失败', err)
      done(false)
    }
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
