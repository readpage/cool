<template>
  flux
  <div>{{ data }}</div>
</template>
<script setup lang="ts">
import { ref } from 'vue'

const data = ref<any[]>([])

function initStream() {
  const eventSource = new EventSource('/api/employee/getAllUsers');

  eventSource.onmessage = (event) => {
    data.value.push(event.data)
  }

  eventSource.onerror = (error) => {
    eventSource.close();
  }
}
initStream()




</script>

<style lang="scss" scoped></style>
