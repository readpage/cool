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
          ref="selectRef"
          v-model="currentField"
          placeholder="请选择"
          filterable
          style="width: 120px"
          @visible-change="onSelectVisibleChange"
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
import { ref, computed, nextTick } from 'vue'
import { Search } from '@element-plus/icons-vue'

interface FieldOption {
  label: string
  value: string
}

interface SearchResult {
  [key: string]: string
}

const props = withDefaults(
  defineProps<{
    options?: FieldOption[]
    modelValue?: SearchResult
    initField?: string
    initKeyword?: string
  }>(),
  {
    options: () => [
      { label: '全字段', value: 'all' },
      { label: '名称', value: 'name' },
      { label: '编号', value: 'code' },
      { label: '电话', value: 'phone' },
      { label: '邮箱', value: 'email' },
    ],
    modelValue: () => ({}),
    initField: '',
    initKeyword: '',
  }
)

const emit = defineEmits<{
  (e: 'search', value: SearchResult): void
  (e: 'change', value: { field: string; keyword: string }): void
}>()

const fieldOptions = computed(() => props.options)

const currentField = ref(props.initField || (fieldOptions.value[0]?.value ?? 'all'))
const keyword = ref(props.initKeyword || '')

const selectRef = ref()

function onFieldChange() {
  emit('change', { field: currentField.value, keyword: keyword.value })
}

function onSelectVisibleChange(visible: boolean) {
  if (!visible) {
    nextTick(() => {
      const input = selectRef.value?.$el?.querySelector('input')
      input?.blur()
    })
  }
}

const currentFieldLabel = computed(() => {
  const option = fieldOptions.value.find((item) => item.value === currentField.value)
  return option?.label ?? ''
})

function handleSearch() {
  const params: SearchResult = {
    [currentField.value]: keyword.value,
  }
  emit('search', params)
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
