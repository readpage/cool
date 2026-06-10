<template>
  <Crud :crud="crud" :form-items="formItems">
    <!-- 表单底部：测试连接 -->
    <template #footer-actions="{ data }">
      <el-button :loading="testing" @click="testConnectFromForm(data)">测试连接</el-button>
    </template>

    <Table
      id="report-datasource"
      :config="tableConfig"
      :data="tableData"
      selection
      :show-admin-btn="false"
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
import Table from '@/components/table/index.vue'
import type { TableConfig, TableQuery, PageResult } from '../types/table'
import type { CrudApi } from '@/components/crud/types'
import { useTableConfigStore } from '@/store/table-config'
import { initTableConfig, formItems } from './config'
import { ADatasource } from '@/api/datasource'

const $store = useTableConfigStore()
const tableData = ref<PageResult>({ list: [], total: 0 })
const tableConfig = computed<TableConfig>(() => $store.getConfig('report-datasource') ?? initTableConfig())

// ==================== 查询 ====================

async function onQuery(_v: TableQuery, done: () => void) {
  try {
    const res = await ADatasource.list({})
    tableData.value = { list: res.data || [], total: (res.data || []).length }
  } catch (err) {
    console.error('获取数据源列表失败', err)
  } finally {
    done()
  }
}

// ==================== 配置持久化 ====================

function onConfigChange(config: TableConfig, isAdmin?: boolean) {
  if (isAdmin) {
    $store.saveAsSystem('report-datasource', config)
  } else {
    $store.save('report-datasource', config)
  }
}

async function onResetSystem() {
  await $store.resetToSystem('report-datasource', initTableConfig())
}

// ==================== 测试连接（表单） ====================

const testing = ref(false)

async function testConnectFromForm(data: Record<string, any>) {
  testing.value = true
  try {
    await ADatasource.test(data)
    ElMessage.success('连接测试成功')
  } catch {
    ElMessage.error('连接测试失败')
  } finally {
    testing.value = false
  }
}

// ==================== CRUD API ====================

const crud: CrudApi = {
  save: async (data, done) => {
    try {
      await ADatasource.save(data)
      done(true)
    } catch (err) {
      console.error('新增数据源失败', err)
      done(false)
    }
  },
  update: async (data, done) => {
    try {
      await ADatasource.save(data)
      done(true)
    } catch (err) {
      console.error('编辑数据源失败', err)
      done(false)
    }
  },
  remove: async (ids, done) => {
    try {
      for (const id of ids) {
        await ADatasource.remove(id as number)
      }
      done(true)
    } catch (err) {
      console.error('删除数据源失败', err)
      done(false)
    }
  },
}
</script>
