<template>
  <el-dialog
    v-model="visible"
    title="权限设置"
    width="520px"
    :close-on-click-modal="false"
    destroy-on-close
    @open="onOpen"
  >
    <div class="permission-body" v-loading="loading">
      <!-- 角色白名单 -->
      <div class="perm-row">
        <div class="perm-label">允许访问的角色（白名单）</div>
        <el-select
          v-model="form.roleIds"
          multiple
          filterable
          placeholder="选择角色（可多选）"
          style="width: 100%"
          :disabled="loading"
        >
          <el-option
            v-for="role in roleList"
            :key="role.id"
            :label="`${role.nickname || role.name} (${role.name})`"
            :value="role.id"
          />
        </el-select>
      </div>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        class="perm-tip"
      >
        <template #title>
          角色列表为空时，仅创建者可访问此报表
        </template>
      </el-alert>
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { AReport } from '@/api/report'
import type { Role, ReportPermissionDto } from '@/views/report/types/report'

const props = defineProps<{
  tableKey: string
}>()

const emit = defineEmits<{
  saved: []
}>()

const visible = ref(false)
const loading = ref(false)
const saving = ref(false)
const roleList = ref<Role[]>([])

const form = reactive<ReportPermissionDto>({
  roleIds: [],
})

function show() {
  visible.value = true
}

async function onOpen() {
  loading.value = true
  try {
    const [permRes, rolesRes] = await Promise.all([
      AReport.getPermission(props.tableKey),
      AReport.listRoles(),
    ])
    if (permRes.data) {
      form.roleIds = permRes.data.roleIds ?? []
    }
    roleList.value = rolesRes.data ?? []
  } catch (err) {
    console.error('加载权限数据失败', err)
    ElMessage.error('加载权限数据失败')
  } finally {
    loading.value = false
  }
}

async function onSave() {
  saving.value = true
  try {
    const payload: ReportPermissionDto = {
      roleIds: form.roleIds,
    }
    await AReport.updatePermission(props.tableKey, payload)
    ElMessage.success('权限更新成功')
    visible.value = false
    emit('saved')
  } catch (err) {
    console.error('保存权限失败', err)
    ElMessage.error('保存权限失败')
  } finally {
    saving.value = false
  }
}

defineExpose({ show })
</script>

<style lang="scss" scoped>
.permission-body {
  padding: 4px 0;
}

.perm-row {
  margin-bottom: 16px;
}

.perm-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 6px;
}

.perm-tip {
  margin-top: 4px;
}
</style>
