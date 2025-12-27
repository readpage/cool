<template>
  <div>
    <el-button @click="submit">{{ title }}</el-button>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { ChildProps, ChildEmits } from '.'

const props = defineProps<ChildProps>()

const emit = defineEmits<ChildEmits>()

const submit = () => {
  emit('submit', props.title)
}
</script>

<style lang="scss" scoped></style>
