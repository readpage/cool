<template>
  <Table
    id="user"
    :config="tableConfig"
    :data="tableData"
    selection
    :load-options="loadOptions"
    :showAdminBtn="false"
    @query="onQuery"
    @change="onConfigChange"
  />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Table from '@/components/table/index.vue'
import type { TableConfig, TableQuery } from '@/types/table'
import { useTableConfigStore } from '@/store/table-config'
import { useOptionsStore } from '@/store/options'

/** api: POST /user/page ← 通过 Vite proxy 转发到后端 */
const API = '/api/user/page'

const $store = useTableConfigStore()
const tableData = ref<Record<string, any>[]>([])

// ==================== 配置 ====================

/** 代码兜底配置 — 仅当本地无缓存且服务端无数据时使用 */
const initConfig = (): TableConfig => ({
  columns: [
    { prop: 'id',         label: 'ID',       minWidth: 80,  align: 'center', hidden: true },
    { prop: 'username',   label: '用户名',   minWidth: 140 },
    { prop: 'sex',        label: '性别',     minWidth: 80,  align: 'center', fieldType: 'remote-select', format: 'tag', optionType: 'user_sex' },
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
      { prop: 'sex',      label: '性别',   operator: 'eq',       fieldType: 'remote-select', filterMode: 'exposed', optionType: 'user_sex' },
      { prop: 'phone',    label: '电话',   operator: 'contains' },
      { prop: 'createTime',      label: '创建时间',   operator: 'between', fieldType: 'daterange', filterMode: 'exposed'   },
      { prop: 'updateTime',      label: '修改时间',   operator: 'between', fieldType: 'daterange'      },
    ],
  },
})

// ==================== 选项加载器 → 委托 optionsStore ====================

const loadOptions = (type: string, keyword?: string) => useOptionsStore().getOptions(type, keyword)

// ==================== 测试种子 ====================

/** 测试用：将 initConfig 注入 localStorage + 内存，当前页面立即生效（注释掉即不使用） */
// seedConfig()
function seedConfig() {
  $store.seedConfig('user', initConfig())
}

// ==================== 配置 & 查询 ====================

/** 配置来源：store 缓存 → 代码兜底（appStore.init 已预加载） */
const tableConfig = computed(() => $store.getConfig('user') ?? initConfig())

/**
 * 统一查询入口：@query 返回的 payload 直接对齐后端 FilterParam 请求体
 * 后端响应 PageResult：{ total, list } → 直接赋值，Table 自动识别分页
 */
async function fetchList(payload: TableQuery) {
  try {
    const res = await fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
    const json = await res.json()
    // ⭐ 一行搞定！Table 组件自动从 { list, total } 中提取数据和分页
    tableData.value = json.data
  } catch (err) {
    console.error('获取用户列表失败', err)
  }
}

function onQuery(payload: TableQuery) {
  fetchList(payload)
}

function onConfigChange(_config: TableConfig) {
  $store.save('user', _config)
}
</script>

<style lang="scss" scoped></style>
