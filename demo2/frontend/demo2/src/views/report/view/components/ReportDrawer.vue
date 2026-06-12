<template>
  <div class="report-drawer-trigger">
    <el-popover
      placement="bottom-end"
      :width="180"
      trigger="click"
      :show-arrow="false"
    >
      <template #reference>
        <span class="gear-btn" title="更多操作">
          <el-icon :size="18"><Setting /></el-icon>
        </span>
      </template>

      <div class="popover-menu">
        <div class="menu-item" @click="onDatasource">
          <el-icon :size="15"><Coin /></el-icon>
          <span>数据源</span>
        </div>
        <div class="menu-item" @click="onPermission">
          <el-icon :size="15"><Lock /></el-icon>
          <span>权限设置</span>
        </div>
      </div>
    </el-popover>

    <!-- 权限设置弹窗 -->
    <PermissionDialog ref="permDialogRef" :table-key="currentTableKey" @saved="$emit('permSaved')" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Setting, Coin, Lock } from '@element-plus/icons-vue'
import PermissionDialog from './PermissionDialog.vue'

defineProps<{
  /** 当前选中报表的 tableKey，用于权限设置 */
  currentTableKey: string
}>()

defineEmits<{
  /** 权限保存成功 */
  permSaved: []
}>()

const router = useRouter()
const permDialogRef = ref<InstanceType<typeof PermissionDialog>>()

/** 跳转数据源管理 */
function onDatasource() {
  router.push({ path: '/report/datasource' })
}

/** 打开权限设置弹窗 */
function onPermission() {
  permDialogRef.value?.show()
}
</script>

<style lang="scss" scoped>
.report-drawer-trigger {
  display: inline-flex;
  align-items: center;
}

.gear-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #ecf5ff;
    color: #409eff;
  }
}

.popover-menu {
  padding: 4px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 13px;
  color: #303133;
  border-radius: 4px;
  transition: background 0.15s;

  &:hover {
    background: #f0f2f5;
  }
}
</style>
