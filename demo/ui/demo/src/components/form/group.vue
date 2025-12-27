<template>
  <el-tabs v-if="form.group && form.group.type == 'tabs'" v-model="form.group.value">
    <template v-for="(label, index) in form.group.labels" :key="index">
      <el-tab-pane :label="label" :name="label">
        <Form ref="formRef" :form="form">
          <template v-for="(item, index) in form.items" :key="index" #[`form-${item.prop}`]="v">
            <slot v-if="!item.component" :name="`form-${item.prop}`" v-bind="v" />
          </template>
        </Form>
      </el-tab-pane>
    </template>
  </el-tabs>
  <template v-else-if="form.group && form.group.type == 'collapse'">
    <template v-for="(label) in form.group.labels">
      <Collapse :label="label">
        <Form ref="formRef" :group="label" :form="form" style="margin-top: 10px">
          <template v-for="(item, index) in form.items" :key="index" #[`form-${item.prop}`]="v">
            <slot v-if="!item.component" :name="`form-${item.prop}`" v-bind="v" />
          </template>
        </Form>
      </Collapse>
    </template>
  </template>
  <template v-else>
    <Form ref="formRef" :form="form">
    </Form>
  </template>
</template>
<script setup lang="ts">
import { computed, nextTick, provide, ref, useSlots } from 'vue'
import { FormApi } from './form.vue'
import Collapse from '@/components/collapse/index.vue'

interface Props {
  form: FormApi
}

const formRef = ref([] as any[])

const props = defineProps<Props>()

// 提交校验
function validate(callback: (vaild: boolean, fields: any) => void) {
  ;(formRef.value[0] || formRef.value).validate((valid: boolean, fields: any) => {
    if (fields) {
      const [key] = Object.entries(fields)[0]
      let form = props.form
      let item = form.items.find(e => e.prop == key)
      form.group!.value = item?.group
    }
    callback(valid, fields)
  })
}

function init() {
  let group = props.form.group
  if (group && !group.value) {
    group.value = group.labels[0]
  }
}
init()

function resetFields() {
  (formRef.value[0] || formRef.value).resetFields()
  let group = props.form.group
  if (group) {
    group.value = group.labels[0]
  }
}

defineExpose({
  validate: validate,
  resetFields: resetFields
})
</script>

<style lang="scss" scoped></style>
