<template>
  <div class="demo-page">
    <Search
      :config="config"
      @save-filter="saveFilter"
      @search="handleSearch"
    />
    <p v-if="resultStr">查询结果：{{ resultStr }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import Search, { type SearchConfig, type FilterItem } from './search/index.vue'

const STORAGE_KEY = 'search_config'

const config = reactive<SearchConfig>({
  currentField: 'all',
  filter: [
    { prop: 'title', label: '书名', operator: 'contains', filterMode: 'exposed' },
    { prop: 'author', label: '作者', operator: 'contains', filterMode: 'exposed' },
    { prop: 'category', label: '分类', operator: 'contains', filterMode: 'show' },
    { prop: 'price', label: '价格', operator: 'contains' },
    { prop: 'stock', label: '库存', operator: 'contains' },
    { prop: 'publisher', label: '出版社', operator: 'contains' },
    { prop: 'publishDate', label: '出版日期', operator: 'contains' },
    { prop: 'status', label: '状态', operator: 'contains' },
  ],
})

function loadPersisted() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const data = JSON.parse(raw)
      Object.assign(config, data)
    }
  } catch { /* ignore */ }
}

loadPersisted()

const resultStr = ref('')

function saveFilter(cfg: SearchConfig) {
  console.log('[index] saveFilter', cfg)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(cfg))
}

function handleSearch(filters: FilterItem[]) {
  console.log('[index] handleSearch', filters)
  resultStr.value = JSON.stringify(filters, null, 2)
}
</script>

<style lang="scss" scoped>
.demo-page {
  padding: 20px;

  p {
    margin-top: 12px;
    color: #666;
  }
}
</style>
