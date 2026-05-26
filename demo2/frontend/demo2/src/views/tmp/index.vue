<template>
  <div class="demo-page">
    <SearchPanel
      :config="config"
      @save-filter="saveFilter"
    />
    <p v-if="resultStr">查询结果：{{ resultStr }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import SearchPanel, { type SearchConfig, type FilterItem } from './search/index.vue'

const STORAGE_KEY = 'search_panel_config'

const config = reactive<SearchConfig>({
  currentField: 'title',
  filter: [
    { prop: 'title', label: '书名', operator: 'contains', exposed: true },
    { prop: 'author', label: '作者', operator: 'contains', exposed: true },
    { prop: 'category', label: '分类', operator: 'contains' },
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
      if (data.currentField) config.currentField = data.currentField
    }
  } catch { /* ignore */ }
}

loadPersisted()

const resultStr = ref('')

// config 变化自动持久化
watch(
  () => config.currentField,
  () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({ currentField: config.currentField }))
  }
)

function saveFilter(payload: FilterItem[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ filter: payload, currentField: config.currentField }))
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
