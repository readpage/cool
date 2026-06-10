<template>
  <div class="go-sider" :class="{ 'go-sider-left': position == 'left', 'go-sider-collapse': collapse }">
    <div class="content"><slot></slot></div>
    <div class="toggle-slot">
      <div class="toggle-bar" @click="collapse = !collapse">
        <div class="toggle-bar__top"></div>
        <div class="toggle-bar__bottom"></div>
      </div>
    </div>
    <aside>
      <div v-if="separator" ref="separatorRef" class="separator"></div>
      <div class="container">
        <slot name="aside"></slot>
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

const separatorRef = ref<HTMLDivElement | null>(null)

onMounted(() => {
  let startX: number, startWidth: number
  const sepEl = separatorRef.value
  if (sepEl) {
    sepEl.addEventListener('mousedown', startDrag)

    function startDrag(e: MouseEvent) {
      e.preventDefault()
      transition.value = ''
      startX = e.clientX
      startWidth = sepEl!.closest('aside')?.clientWidth || 0
      document.body.style.userSelect = 'none'
      document.body.style.cursor = 'col-resize'
      document.documentElement.addEventListener('mousemove', onDrag)
      document.documentElement.addEventListener('mouseup', stopDrag)
    }

    function onDrag(e: MouseEvent) {
      const delta = e.clientX - startX
      // left: drag → increases; right: drag ← increases
      const direction = props.position === 'left' ? 1 : -1
      let newWidth = startWidth + delta * direction
      newWidth = Math.max(200, Math.min(newWidth, 800))
      width.value = newWidth + 'px'
    }

    function stopDrag() {
      transition.value = 'all 0.3s ease-in-out'
      document.body.style.userSelect = ''
      document.body.style.cursor = ''
      document.documentElement.removeEventListener('mousemove', onDrag)
      document.documentElement.removeEventListener('mouseup', stopDrag)
    }
  }
})
</script>

<style lang="scss" scoped>
// =====================================================
// CSS Custom Properties — 位置相关值的唯一来源
// 默认值 = right 位置
// =====================================================
.go-sider {
  // ----- 布局排序 -----
  // toggle-slot 始终 order:2，固定在 content 和 aside 之间
  --sider-content-order: 1;
  --sider-aside-order: 3;
  --sider-aside-justify: flex-start;
  --sider-separator-order: 0;
  --sider-container-order: 1;

  // ----- toggle-bar 定位偏移（相对 toggle-slot） -----
  --sider-toggle-left: -28px;
  --sider-toggle-right: auto;

  // ----- toggle-bar hover 动画方向 -----
  --sider-toggle-hover-top: rotate(-12deg) scale(1.15) translateY(-2px);
  --sider-toggle-hover-bottom: rotate(12deg) scale(1.15) translateY(2px);

  // =====================================================
  // 布局
  // =====================================================
  display: flex;
  height: 100%;
  overflow: hidden;

  .content {
    order: var(--sider-content-order);
    flex: 1;
    min-width: 0;
    overflow: hidden;
  }

  // ----- 按钮锚点槽：width=0 不占空间，absolute 子元素自由伸出 -----
  .toggle-slot {
    order: 2;
    position: relative;
    flex-shrink: 0;
    width: 0;
    min-width: 0;
    z-index: 10;
  }

  .toggle-bar {
    position: absolute;
    top: calc(50% - 36px);
    left: var(--sider-toggle-left);
    right: var(--sider-toggle-right);
    cursor: pointer;
    height: 72px;
    width: 32px;

    &:hover {
      .toggle-bar__top {
        transform: var(--sider-toggle-hover-top);
        background-color: var(--go-toggle-bar-color-hover);
      }
      .toggle-bar__bottom {
        transform: var(--sider-toggle-hover-bottom);
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
      top: 34px;
    }
  }

  aside {
    order: var(--sider-aside-order);
    flex-shrink: 0;
    min-width: 0;
    position: relative;
    z-index: 1;
    display: flex;
    justify-content: var(--sider-aside-justify);
    width: v-bind(width);
    transition: v-bind(transition);
    height: 100%;
    overflow: hidden;

    .container {
      order: var(--sider-container-order);
      flex: 1;
      min-width: 0;
      overflow: hidden;
      height: 100%;
    }

    .separator {
      order: var(--sider-separator-order);
      flex-shrink: 0;
      position: relative;
      width: 5px;
      height: 100%;
      background-color: #eee;
      box-shadow: 0px 0px 2px rgba(0, 0, 0, 0.35);
      cursor: col-resize;

      &::before {
        content: '';
        position: absolute;
        inset: 0 -4px;
      }
    }
  }
}

// =====================================================
// Collapse — 箭头反转 + aside 安全裁剪
// =====================================================
.go-sider-collapse {
  aside {
    width: 0 !important;
    // overflow:hidden 已在 aside 上，安全裁剪
  }

  .toggle-bar {
    --sider-toggle-hover-top: rotate(12deg) scale(1.15) translateY(-2px);
    --sider-toggle-hover-bottom: rotate(-12deg) scale(1.15) translateY(2px);
  }
}

// =====================================================
// Left 位置
// =====================================================
.go-sider-left {
  --sider-content-order: 3;
  --sider-aside-order: 1;
  --sider-aside-justify: flex-end;
  --sider-separator-order: 1;
  --sider-container-order: 0;
  --sider-toggle-left: auto;
  --sider-toggle-right: -28px;
  --sider-toggle-hover-top: rotate(12deg) scale(1.15) translateY(-2px);
  --sider-toggle-hover-bottom: rotate(-12deg) scale(1.15) translateY(2px);

  .toggle-bar {
    transform: rotate(180deg);
  }

  &.go-sider-collapse .toggle-bar {
    --sider-toggle-hover-top: rotate(-12deg) scale(1.15) translateY(-2px);
    --sider-toggle-hover-bottom: rotate(12deg) scale(1.15) translateY(2px);
  }
}
</style>
