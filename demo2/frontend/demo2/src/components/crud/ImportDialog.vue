<template>
  <u-dialog
    :model-value="visible"
    title="批量导入"
    :close-on-click-modal="false"
    width="700px"
    @update:model-value="(val: boolean) => { if (!val) handleClose() }"
  >
    <div class="u-import-content">
      <!-- 步骤引导 -->
      <el-steps :active="step" align-center simple style="margin-bottom: 20px">
        <el-step>
          <template #title>
            <span class="u-step-clickable" @click="goToStep(0)">下载模板</span>
          </template>
        </el-step>
        <el-step>
          <template #title>
            <span class="u-step-clickable" :class="{ disabled: maxStep < 1 }" @click="goToStep(1)">上传文件</span>
          </template>
        </el-step>
        <el-step>
          <template #title>
            <span class="u-step-clickable" :class="{ disabled: maxStep < 2 }" @click="goToStep(2)">查看结果</span>
          </template>
        </el-step>
      </el-steps>

      <!-- Step 1: 下载模板 -->
      <template v-if="step === 0">
        <div class="u-import-step">
          <el-icon :size="40" color="#409eff"><Download /></el-icon>
          <p style="margin: 12px 0 4px; color: #606266">请先下载导入模板，按模板格式填写数据</p>
          <div class="u-import-actions">
            <el-button type="primary" :loading="templateLoading" @click="handleDownloadTemplate">
              下载模板
            </el-button>
            <el-button link type="primary" @click="skipDownload">已有模板，直接导入</el-button>
          </div>
        </div>
      </template>

      <!-- Step 2: 上传文件 -->
      <template v-if="step === 1">
        <div class="u-import-step">
          <el-upload
            ref="uploadRef"
            drag
            :auto-upload="false"
            :limit="1"
            accept=".xls,.xlsx"
            :on-change="onFileChange"
            :on-remove="onFileRemove"
          >
            <el-icon class="el-icon--upload" :size="40" color="#c0c4cc"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将 Excel 拖拽到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">仅支持 .xls / .xlsx 格式，单次最多 1 个文件</div>
            </template>
          </el-upload>
        </div>
      </template>

      <!-- Step 3: 结果展示 -->
      <template v-if="step === 2">
        <div class="u-import-step" v-loading="uploading">
          <template v-if="uploadResult">
            <div class="u-import-result">
              <div class="u-import-result__item">
                <span class="u-import-result__label">总计</span>
                <span class="u-import-result__value">{{ uploadResult.total }}</span>
              </div>
              <div class="u-import-result__item u-import-result__item--success">
                <span class="u-import-result__label">成功</span>
                <span class="u-import-result__value">{{ uploadResult.success }}</span>
              </div>
              <div class="u-import-result__item u-import-result__item--fail">
                <span class="u-import-result__label">失败</span>
                <span class="u-import-result__value">{{ uploadResult.fail }}</span>
              </div>
            </div>
            <div v-if="uploadResult.errors?.length" class="u-import-errors">
              <p style="color: #f56c6c; margin-bottom: 8px">失败原因：</p>
              <ul>
                <li v-for="(err, i) in uploadResult.errors" :key="i">{{ err }}</li>
              </ul>
            </div>
            <el-button link type="primary" style="margin-top: 8px" @click="goToStep(1)">重新上传</el-button>
          </template>
          <p v-else style="color: #909399">正在导入，请稍候...</p>
        </div>
      </template>
    </div>

    <template #footer>
      <div class="u-import-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button v-if="step === 1" type="primary" :disabled="!uploadFile" :loading="uploading" @click="handleUpload">
          开始导入
        </el-button>
      </div>
    </template>
  </u-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, UploadFilled } from '@element-plus/icons-vue'
import type { UploadFile, UploadInstance } from 'element-plus'

const props = defineProps<{
  visible: boolean
  /** 下载模板 API */
  templateApi: () => Promise<any>
  /** 上传文件 API（接收 FormData，返回导入结果） */
  uploadApi: (formData: FormData) => Promise<any>
}>()

const emit = defineEmits<{
  'update:visible': [val: boolean]
  done: []
}>()

const step = ref(0)
const maxStep = ref(0)
const templateLoading = ref(false)

/** 可跳转到已访问过的步骤（不能点当前步） */
function goToStep(s: number) {
  if (s !== step.value && s <= maxStep.value) {
    step.value = s
    // 回退到步骤1时重置上传状态
    if (s === 1) {
      uploadFile.value = null
      uploadResult.value = null
    }
  }
}

/** 跳过下载模板，直接进入上传 */
function skipDownload() {
  step.value = 1
  maxStep.value = Math.max(maxStep.value, 1)
}
const uploadRef = ref<UploadInstance>()
const uploadFile = ref<UploadFile | null>(null)
const uploading = ref(false)
const uploadResult = ref<{ total: number; success: number; fail: number; errors?: string[] } | null>(null)

async function handleDownloadTemplate() {
  templateLoading.value = true
  try {
    await props.templateApi()
  } catch {
    ElMessage.error('下载模板失败')
  } finally {
    templateLoading.value = false
    step.value = 1
    maxStep.value = Math.max(maxStep.value, 1)
  }
}

function onFileChange(file: UploadFile) {
  uploadFile.value = file
}

function onFileRemove() {
  uploadFile.value = null
}

async function handleUpload() {
  if (!uploadFile.value?.raw) return
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value.raw)
    // 截图保存当前的 tableConfig（search 含 columns + filter），
    // 方便后端按 Excel 表头匹配列
    const res = await props.uploadApi(formData)
    if (res?.code === 0 || res?.code === 200) {
      const data = res.data ?? res
      uploadResult.value = {
        total: data.total ?? 0,
        success: data.success ?? 0,
        fail: data.fail ?? 0,
        errors: data.errors ?? [],
      }
      ElMessage.success(res.msg ?? '导入完成')
      emit('done')
    } else {
      uploadResult.value = { total: 0, success: 0, fail: 0, errors: [res.msg ?? '导入失败'] }
      ElMessage.error(res.msg ?? '导入失败')
    }
    step.value = 2
    maxStep.value = Math.max(maxStep.value, 2)
  } catch {
    uploadResult.value = { total: 0, success: 0, fail: 0, errors: ['请求失败'] }
    ElMessage.error('导入失败，请检查文件格式')
    step.value = 2
    maxStep.value = Math.max(maxStep.value, 2)
  } finally {
    uploading.value = false
  }
}

function handleClose() {
  // 重置状态
  step.value = 0
  maxStep.value = 0
  uploadFile.value = null
  uploadResult.value = null
  emit('update:visible', false)
}
</script>

<style lang="scss" scoped>
.u-import-content {
  min-height: 180px;
}

.u-import-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.u-import-result {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;

  &__item {
    text-align: center;
    padding: 8px 20px;
    border-radius: 6px;
    background: #f5f7fa;

    &--success { background: #f0f9eb; }
    &--fail    { background: #fef0f0; }
  }

  &__label {
    display: block;
    font-size: 12px;
    color: #909399;
    margin-bottom: 4px;
  }

  &__value {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }
}

.u-import-errors {
  max-height: 120px;
  overflow-y: auto;
  font-size: 13px;

  ul {
    margin: 0;
    padding-left: 20px;
    li {
      color: #f56c6c;
      line-height: 1.6;
    }
  }
}

.u-import-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.u-step-clickable {
  cursor: pointer;
  user-select: none;

  &:hover {
    color: var(--el-color-primary);
  }

  &.disabled {
    cursor: not-allowed;
    opacity: 0.5;

    &:hover {
      color: inherit;
    }
  }
}

.u-import-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 4px;
}
</style>
