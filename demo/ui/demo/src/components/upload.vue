<template>
  <div class="u-upload">
    <el-upload
      drag
      class="u-upload__box"
      :show-file-list="false"
      :auto-upload="false"
      accept=".png,.jepg,.jpg,.webp,.gif,.svg"
      :on-change="change"
    >
      <div class="u-upload__item u-upload__avatar">
        <div v-if="files" class="img">
          <!-- 工具 -->
          <div class="img--action select-none" @click.stop>
            <!-- 预览 -->
            <el-icon @click.stop="showViewer = true">
              <zoom-in />
            </el-icon>

            <!-- 删除 -->
            <el-icon @click.stop="files = null">
              <delete />
            </el-icon>
          </div>
          <el-image :src="files" class="img" fit="contain"></el-image>
        </div>
        <div v-else class="box">
          <el-icon class="icon"><Plus /></el-icon>
          <span>{{ title }}</span>
        </div>
      </div>
    </el-upload>
    <el-image-viewer v-if="showViewer" :initial-index="0" :url-list="previewList" @close="() => (showViewer = false)" />
  </div>
</template>
<script setup lang="ts">
import { Plus, ZoomIn, Delete } from '@element-plus/icons-vue'
import { computed, ref } from 'vue'
import { toPx } from '@/util'
interface Props {
  title?: string // 上传按钮的文字
  accept?: string // 接受上传的文件类型
  width?: number | string
  height?: number | string
  modelValue?: string | null
}
const props = withDefaults(defineProps<Props>(), {
  title: '上传封面',
  width: 105,
  height: 105
})

const showViewer = ref(false)
const previewList = ref<string[]>([])

const emit = defineEmits<{
  change: [file: File, done: (val: string) => void]
  'update:modelValue': [val?: string | null]
}>()

const files = computed({
  get() {
    return props.modelValue
  },
  set(val) {
    emit('update:modelValue', val)
  }
})

const change = (file: any, list: any) => {
  const rawFile: File = file.raw
  emit('change', rawFile, (val: string) => {
    files.value = val
    previewList.value = [val]
    console.log('change', previewList.value, val)
  })
}

const width = computed(() => toPx(props.width))
const height = computed(() => toPx(props.height))
</script>

<style lang="scss" scoped v-bind>
.u-upload {
  width: v-bind(width);
  height: v-bind(height);
  &__item {
    width: v-bind(width);
    height: v-bind(height);
  }
  &__avatar {
    .img {
      height: 100%;
      width: 100%;
      &--action {
        position: absolute;
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 9;
        height: 100%;
        width: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        border-radius: 5px;
        opacity: 0;
        transition: opacity 0.3s cubic-bezier(0.55, 0, 0.1, 1);
        &:hover {
          opacity: 1;
        }

        .el-icon {
          color: #fff;
          font-size: 18px;
          margin: 0 8px;

          &:hover {
            color: #0ea5e9;
          }
        }
      }
    }
    .box {
      display: flex;
      height: 105px;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.65);
    }
  }
  :deep() {
    .el-upload-dragger {
      padding: 0;
    }
  }
}
</style>
