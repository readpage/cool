<template>
  <div class="go-sider" :class="{ 'go-sider-left': position == 'left', 'go-sider-collapse': collapse }">
    <div class="content"><slot></slot></div>
    <aside>
      <div v-if="separator" class="separator"></div>
      <div class="container">
        <slot name="aside"></slot>
      </div>
      <div class="toggle-bar" @click="collapse = !collapse">
        <div class="toggle-bar__top"></div>
        <div class="toggle-bar__bottom"></div>
      </div>
    </aside>
  </div>
</template>
<script setup lang="ts">
import { toPx } from 'undraw-ui'
import { onMounted, ref } from 'vue'

interface Props {
  width?: number | string
  position?: 'left' | 'right'
  separator?: boolean
}
const props = withDefaults(defineProps<Props>(), {
  width: 400,
  position: 'right',
  separator: false
})

const collapse = defineModel({ default: false })
const width = ref(toPx(props.width))
const transition = ref('all 0.3s ease-in-out')

onMounted(() => {
  let startX: number, startWidth: number
  let separtaorDom = document.querySelector('.separator') as HTMLDivElement
  if (separtaorDom) {
    separtaorDom.addEventListener('mousedown', startDrag)
    function startDrag(e: MouseEvent) {
      transition.value = ''
      startX = e.clientX
      startWidth = document.querySelector('aside')?.clientWidth || 0
      console.log(startWidth)
      document.documentElement.addEventListener('mousemove', onDrag)
      document.documentElement.addEventListener('mouseup', stopDrag)
    }

    function onDrag(e: MouseEvent) {
      console.log(startWidth, e.clientX, startX)
      let newWidth = startWidth - (e.clientX - startX)
      width.value = newWidth + 'px'
    }

    function stopDrag(e: MouseEvent) {
      transition.value = 'all 0.3s ease-in-out'
      document.documentElement.removeEventListener('mousemove', onDrag)
      document.documentElement.removeEventListener('mouseup', stopDrag)
    }
  }
})
</script>

<style lang="scss" scoped>
.go-sider {
  display: flex;
  overflow: hidden;
  height: 100%;
  .content {
    flex: 1;
  }
  aside {
    flex-shrink: 0;
    position: relative;
    z-index: 1;
    display: flex;
    justify-content: flex-start;
    width: v-bind(width);
    transition: v-bind(transition);
    height: 100%;
    .container {
      overflow: hidden;
      height: 100%;
    }
    .toggle-bar {
      left: -28px;
      cursor: pointer;
      height: 72px;
      width: 32px;
      position: absolute;
      top: calc(50% - 36px);
      &:hover {
        .toggle-bar__top {
          transform: rotate(-12deg) scale(1.15) translateY(-2px);
          background-color: var(--go-toggle-bar-color-hover);
        }
        .toggle-bar__bottom {
          transform: rotate(12deg) scale(1.15) translateY(2px);
          background-color: var(--go-toggle-bar-color-hover);
        }
      }
      &__top,
      &__bottom {
        position: absolute;
        width: 4px;
        border-radius: 2px;
        background-color: var(--go-toggle-bar-color);
        height: 38px;
        left: 14px;
        transition:
          background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1),
          transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      }
      &__bottom {
        position: absolute;
        top: 34px;
      }
    }
    .separator {
      width: 5px;
      height: 100%;
      background-color: #eee;
      box-shadow: 0px 0px 2px rgba(0, 0, 0, 0.35);
      cursor: e-resize;
    }
  }
}
.go-sider-collapse {
  aside {
    width: 0;
    .toggle-bar {
      &:hover {
        .toggle-bar__top {
          transform: rotate(12deg) scale(1.15) translateY(-2px);
        }
        .toggle-bar__bottom {
          transform: rotate(-12deg) scale(1.15) translateY(2px);
        }
      }
    }
  }
}
.go-sider-left {
  flex-direction: row-reverse;
  aside {
    flex-direction: row-reverse;
    justify-content: flex-end;
    .toggle-bar {
      left: auto;
      right: -28px;
      transform: rotate(180deg);
    }
  }
}
</style>
