<template>
  <div class="search-group">
    <el-input
      v-model="keyword"
      :placeholder="`搜索${currentFieldLabel}`"
      class="search-input"
      clearable
      @keyup.enter="handleSearch"
    >
      <template #prepend>
        <el-select
          v-model="currentField"
          placeholder="请选择"
          filterable
          style="width: 120px"
          @change="onFieldChange"
        >
          <el-option
            v-for="item in fieldOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </template>
      <template #append>
        <el-button :icon="Search" @click="handleSearch" />
      </template>
    </el-input>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'

interface FieldOption {
  label: string
  value: string
}

const props = withDefaults(
  defineProps<{
    options: FieldOption[]
    initField?: string
    initKeyword?: string
  }>(),
  {
    options: () => [],
    initField: '',
    initKeyword: '',
  },
)

const emit = defineEmits<{
  (e: 'search', value: Record<string, string>): void
  (e: 'change', value: { field: string; keyword: string }): void
}>()

/* ============ 内置全选项 ============ */

const ALL_OPTION: FieldOption = { label: '全选', value: 'all' }

const fieldOptions = computed(() =>
  [ALL_OPTION, ...props.options.filter((o) => o.value !== 'all')],
)

const currentField = ref(props.initField || ALL_OPTION.value)
const keyword = ref(props.initKeyword || '')

const currentFieldLabel = computed(() =>
  fieldOptions.value.find((item) => item.value === currentField.value)?.label ?? '',
)

/* ============ 事件 ============ */

function onFieldChange() {
  emit('change', { field: currentField.value, keyword: keyword.value })
}

function handleSearch() {
  emit('search', { [currentField.value]: keyword.value })
}
</script>

<style lang="scss" scoped>
.search-group {
  max-width: 400px;
  .search-input {
    :deep(.el-input-group__prepend) {
      background-color: var(--el-fill-color-blank);
      padding: 0;
      overflow: visible;
      width: 120px;
      box-sizing: content-box;
    }
  }
}
</style>
