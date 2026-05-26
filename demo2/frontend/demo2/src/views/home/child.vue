<template>
  <div class="child">
    <div class="msg">{{ msg }}</div>
    <div class="flex gap-15px">
      <el-input v-model="value" style="width: 200px"></el-input>
      <slot />
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  msg?: string
}

const { msg = 'hello' } = defineProps<Props>()
const value = defineModel()

</script>

<style lang="scss" scoped>
.child {
  display: flex;
  flex-direction: column;
  gap: 10px;
  .msg {
    @apply text-gray-500;
  }
}
</style>
