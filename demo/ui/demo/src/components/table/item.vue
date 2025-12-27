<template>
  <el-table-column
    :label="item.label"
    :prop="item.prop"
    :width="item.width"
    :min-width="item.minWidth"
    :align="item.align"
    :type="item.type"
  >
    <template #default="scope">
      <el-form-item v-if="!['selection', 'index'].includes(item.type || '')" :prop="`${scope.$index}.${item.prop}`" :rules="item.required ? { required: true, message: `${item.label}不能为空`, trigger: 'change' } : item.rule" :class="{'no-add': noAdd}" style="width: 100%;">
        <!-- img -->
        <template v-if="item.prop && item.type == 'img'">
          <el-image
            :src="scope.row[item.prop]"
            :preview-src-list="[scope.row[item.prop]]"
            preview-teleported
            fit="cover"
            class="w-10 h-10"
          >
            <template #error>
              <img class="w-full h-full" src="/static/img/avatar.png" alt="avatar" />
            </template>
          </el-image>
        </template>
        <!-- input -->
        <template v-else-if="item.prop && item.type == 'input'">
          <el-input v-model="scope.row[item.prop]" clearable></el-input>
        </template>
        <!-- date -->
        <template v-else-if="item.prop && item.type == 'date'">
          <el-date-picker v-model="scope.row[item.prop]" type="date" value-format="YYYY-MM-DD" :shortcuts="shortcuts" />
        </template>
        <!-- select -->
        <template v-else-if="item.prop && item.type == 'select'">
          <el-select v-model="scope.row[item.prop]">
            <el-option v-for="e in item.options" :key="e.value" :label="e.label" :value="e.value || e" />
          </el-select>
        </template>
        <!-- custom 自定义 -->
        <template v-if="item.type == 'custom'">
          <slot :name="`table-${item.prop}`" :row="scope.row"></slot>
        </template>
      </el-form-item>
    </template>
  </el-table-column>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { ItemApi } from './table.vue'
import { Time } from 'undraw-ui'

interface Props {
  item: ItemApi
  noAdd: boolean
}

const shortcuts = [
  {
    text: '今天',
    value: new Date()
  },
  {
    text: '昨天',
    value: new Time().add(-1, 'day')
  },
  {
    text: '一周前',
    value: new Time().add(-1, 'week')
  }
]

const props = defineProps<Props>()
</script>

<style lang="scss" scoped>
.no-add {
  margin-bottom: 0;
}
</style>
