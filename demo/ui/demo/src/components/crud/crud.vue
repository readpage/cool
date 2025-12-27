<template>
  <div class="u-crud">
    <div v-show="searchVisible">
      <slot name="query"></slot>
    </div>
    <div ref="toolRef" class="tool">
      <div>
        <el-button v-if="crud.save" :icon="Plus" class="u-small" type="primary" @click="save">新增</el-button>
        <el-button
          v-if="crud.update"
          :icon="EditPen"
          class="u-small"
          :disabled="state.selections?.length != 1"
          type="success"
          @click="update(state.selections[0])"
        >
          修改
        </el-button>
        <el-button
          v-if="crud.remove"
          :icon="Delete"
          class="u-small"
          :loading="btnLoading"
          :disabled="state.selections?.length == 0"
          type="danger"
          @click="remove(state.selections)"
        >
          删除
        </el-button>
        <slot name="tool"></slot>
      </div>
      <div>
        <el-tooltip v-if="$slots.search" :content="searchVisible ? '隐藏搜索' : '显示搜索'" placement="top">
          <el-button size="small" circle :icon="Search" @click="searchVisible = !searchVisible"></el-button>
        </el-tooltip>
        <el-tooltip content="刷新" placement="top">
          <el-button size="small" circle :icon="Refresh" @click="refresh"></el-button>
        </el-tooltip>
      </div>
    </div>
    <Table
      ref="tableRef"
      :table="table"
      :max-height="maxHeight"
      @on-selection="val => (state.selections = val)"
      @input-blur="val => $emit('inputBlur', val)"
    >
      <template v-for="(item, index) in table.columns" :key="index" #[`table-${item.prop}`]="v">
        <slot :name="`table-${item.prop}`" v-bind="v"></slot>
      </template>
    </Table>
    <u-dialog v-model="visible.form" :title="state.title">
      <Group v-if="form" ref="groupRef" :form="form">
        <template v-for="(item, index) in form.items" :key="index" #[`form-${item.prop}`]="v">
          <slot v-if="!item.component" :name="`form-${item.prop}`" v-bind="v" />
        </template>
      </Group>
      <template #footer>
        <div style="width: 100%; display: flex; justify-content: flex-end">
          <el-button @click="visible.form = false">取消</el-button>
          <el-button type="primary" :loading="state.btnLoading" @click="submit()">确定</el-button>
        </div>
      </template>
    </u-dialog>
  </div>
</template>
<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, provide, reactive, ref } from 'vue'
import { Plus, EditPen, Delete, Search, Refresh } from '@element-plus/icons-vue'
import Table, { TableApi } from '../table/table.vue'
import Group from '@/components/form/group.vue'
import { ElMessageBox } from 'element-plus'
import { FormApi } from '../form/form.vue'
import { cloneDeep } from 'undraw-ui'
export type { TableApi } from '../table/table.vue'

export interface CrudApi {
  save?: (val: any, finish: () => void) => void
  update?: (val: any, finish: () => void) => void
  remove?: (val: any[], finish: () => void) => void
}

interface Props {
  table: TableApi
  crud: CrudApi
  form?: FormApi
  maxHeight?: number | string
}

const props = withDefaults(defineProps<Props>(), {})

const searchVisible = ref(true)
const btnLoading = ref(false)
const state = reactive({
  selections: [] as any[],
  btnLoading: false,
  title: ''
})

const visible = reactive({
  form: false
})

const tableRef = ref<InstanceType<typeof Table>>()
const groupRef = ref()

const emit = defineEmits<{
  inputBlur: [val: any]
}>()

function save() {
  state.title = '新增'
  visible.form = true
  nextTick(() => {
    groupRef.value.resetFields()
  })
}

const update = (val: any) => {
  state.title = '修改'
  visible.form = true
  nextTick(() => {
    groupRef.value.resetFields()
    props.form!.data = cloneDeep(val)
  })
}

function edit(val: any) {
  update(val)
}
provide('edit', edit)

const remove = (val: any[]) => {
  ElMessageBox.confirm('你确定要删除吗？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    draggable: true
  }).then(() => {
    if (props.crud.remove) {
      props.crud.remove(val, refresh)
    }
  })
}

function submit() {
  groupRef.value.validate((valid: boolean) => {
    if (valid) {
      let crud = props.crud
      if (state.title == '新增') {
        crud.save && crud.save(props.form?.data, refresh)
      } else if (state.title == '修改') {
        crud.update && crud.update(props.form?.data, refresh)
      }
    }
  })
}

const refresh = () => {
  visible.form = false
  tableRef.value?.reload()
}

const toolRef = ref()
const tableHeight = ref()

provide('u-crud', props.crud)
onMounted(() => {
  tableHeight.value = toolRef.value.clientHeight
})

defineExpose({
  refresh
})
</script>

<style lang="scss" scoped>
.u-crud {
  height: 100%;
  padding: 10px;
  background-color: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  .tool {
    margin-bottom: 8px;
    display: flex;
    justify-content: space-between;
  }
  .u-table {
    flex: 1;
  }
}
</style>
<style>
.u-small {
  height: 28px !important;
  padding: 5px 11px !important;
  font-size: 12px !important;
  border-radius: calc(var(--el-border-radius-base) - 1px) !important;
}
</style>
