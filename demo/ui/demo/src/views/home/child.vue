<template>
  <div>{{ msg }}</div>
</template>
<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  msg?: string
}

const { msg = 'hello' } = defineProps<Props>()

</script>

<style lang="scss" scoped></style>
