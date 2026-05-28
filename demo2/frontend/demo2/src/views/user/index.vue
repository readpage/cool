<template>
  <Table
    id="user"
    :config="tableConfig"
    :data="tableData"
    :load-options="loadOptions"
    showAdminBtn
    @query="query"
  />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Table from '@/components/table/index.vue'
import type { TableConfig } from '@/types/table'
import { useConfigStore } from '@/store/config'

/** api: POST /user/list ← 通过 Vite proxy 转发到后端 */
const API = '/api/user/list'

const $store = useConfigStore()
const tableData = ref<Record<string, any>[]>([])

/** 代码兜底配置 — 仅当本地无缓存且服务端无数据时使用 */
const initConfig = (): TableConfig => ({
  columns: [
    { prop: 'id',         label: 'ID',       minWidth: 80,  align: 'center', hidden: true },
    { prop: 'username',   label: '用户名',   minWidth: 140 },
    { prop: 'sex',        label: '性别',     minWidth: 80,  align: 'center' },
    { prop: 'age',        label: '年龄',     minWidth: 80 },
    { prop: 'phone',      label: '电话',     minWidth: 160 },
    { prop: 'createTime', label: '创建时间', minWidth: 180, align: 'center' },
    { prop: 'updateTime', label: '修改时间', minWidth: 180, align: 'center' },
  ],
  stripe: true,
  search: {
    currentField: 'all',
    filter: [
      { prop: 'username', label: '用户名', operator: 'contains', filterMode: 'exposed' },
      { prop: 'sex',      label: '性别',   operator: 'eq',       fieldType: 'remote-select' },
      { prop: 'phone',    label: '电话',   operator: 'contains', filterMode: 'exposed' },
      { prop: 'age',      label: '年龄',   operator: 'eq'        },
    ],
  },
})

// ==================== 统一选项加载器 ====================

/** 选项加载器（type=选项类别，keyword=搜索关键词）。Table 内部据此完成表格翻译预加载和搜索下拉 */
const loadOptions = async (type: string, keyword?: string) => {
  const params = new URLSearchParams({ type, limit: keyword ? '20' : '200' })
  if (keyword) params.set('keyword', keyword)
  const res = await fetch(keyword ? `/api/option/search?${params}` : `/api/option/list?${params}`)
  const json = await res.json()
  return (json.data ?? []).map((item: any) => ({ label: item.label, value: item.value }))
}

// ==================== 配置 & 查询 ====================

/** 配置来源：store 缓存 → 代码兜底（appStore.init 已预加载） */
const tableConfig = computed(() => $store.getConfig('user') ?? initConfig())

async function fetchList(query: Record<string, any>) {
  try {
    const res = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(query),
    })
    const json = await res.json()
    tableData.value = json.data ?? []
  } catch (err) {
    console.error('获取用户列表失败', err)
  }
}

function query(query: Record<string, any>) {
  fetchList(query)
}
</script>

<style lang="scss" scoped></style>
