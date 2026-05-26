<template>
  <Table :config="tableConfig" :data="tableData" :selection="selection" :show-admin-btn="true" @change="saveConfig" @reset="resetConfig" @admin-confirm="adminConfirm" @query="onQuery" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Table from '@/components/table/index.vue'
import type { TableConfig, TableItem } from '@/components/table/index.vue'

const tableData = ref([
  { id: 1, title: '深入浅出Vue.js',             author: '刘博文',      category: '前端开发', price: 79.00,   stock: 128, publisher: '人民邮电出版社', publishDate: '2019-03-01', status: '在售' },
  { id: 2, title: 'JavaScript高级程序设计（第4版）', author: 'Matt Frisbie', category: '前端开发', price: 129.00,  stock: 96,  publisher: '人民邮电出版社', publishDate: '2020-12-01', status: '在售' },
  { id: 3, title: '算法导论（第3版）',            author: 'Thomas H.Cormen', category: '计算机科学', price: 128.00,  stock: 45,  publisher: '机械工业出版社', publishDate: '2013-01-01', status: '在售' },
  { id: 4, title: '设计模式：可复用面向对象软件的基础', author: 'GoF',          category: '软件工程', price: 69.00,   stock: 0,   publisher: '机械工业出版社', publishDate: '2000-09-01', status: '缺货' },
  { id: 5, title: '重构：改善既有代码的设计',        author: 'Martin Fowler',  category: '软件工程', price: 99.00,   stock: 73,  publisher: '人民邮电出版社', publishDate: '2019-05-01', status: '在售' },
  { id: 6, title: 'TypeScript编程',              author: 'Boris Cherny',   category: '前端开发', price: 69.00,   stock: 55,  publisher: '中国电力出版社', publishDate: '2020-08-01', status: '在售' },
  { id: 7, title: '高性能MySQL（第4版）',          author: 'Silvia Botros',  category: '数据库',   price: 139.00,  stock: 32,  publisher: '电子工业出版社', publishDate: '2022-01-01', status: '在售' },
  { id: 8, title: '人月神话',                    author: 'Fred Brooks',    category: '软件工程', price: 49.00,   stock: 0,   publisher: '清华大学出版社', publishDate: '2015-04-01', status: '缺货' },
])

const STORAGE_KEY = 'table-config'
const initConfig = (): TableConfig => ({
  columns: [
    { prop: 'title',      label: '书名',       width: 200 },
    { prop: 'author',     label: '作者',       width: 140 },
    { prop: 'category',   label: '分类',       width: 120, align: 'center' },
    { prop: 'price',      label: '价格',       width: 100 },
    { prop: 'stock',      label: '库存',       width: 80 },
    { prop: 'publisher',  label: '出版社',     width: 180 },
    { prop: 'publishDate', label: '出版日期',  width: 140, align: 'center' },
    { prop: 'status',     label: '状态',      minWidth: 100, align: 'center' },
  ],
  sort: { column: 'price', direction: 'desc' },
})

const selection = ref(false)

const tableConfig = ref<TableConfig>({} as TableConfig)

function init() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    tableConfig.value = saved ? JSON.parse(saved) : initConfig()
  } catch {
    tableConfig.value = initConfig()
  }
}

init()
const saveConfig = (config: TableConfig) => {
  console.log('change', config)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
}

// 恢复默认设置
const resetConfig = () => {
  localStorage.removeItem(STORAGE_KEY)
  tableConfig.value = initConfig()
}

// 管理员确认默认配置（保存为系统默认）
function adminConfirm(columns: TableItem[]) {
  console.log('adminConfirm', columns)
}

// 查询参数变化（排序、筛选等）
function onQuery(query: Record<string, any>) {
  console.log('query', query)
  // 这里调用接口刷新数据
}

</script>

<style lang="scss" scoped></style>
