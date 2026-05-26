<template>
  <div>
    <Child v-model="value">{{ value }}</Child>
    <div>{{ app.count }}</div>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import Child from './child.vue'
import { useAppStore } from '@/store/app'

const value = ref('test')

const app = useAppStore()

</script>

<style lang="scss" scoped></style>
