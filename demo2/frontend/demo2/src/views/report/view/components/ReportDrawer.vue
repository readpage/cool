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
        <div class="menu-item disabled" @click="onPermission">
          <el-icon :size="15"><Lock /></el-icon>
          <span>权限设置</span>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Setting, Coin, Lock } from '@element-plus/icons-vue'

const router = useRouter()

/** 新窗口打开数据源管理 */
function onDatasource() {
  const url = router.resolve({ path: '/report/datasource' }).href
  window.open(url, '_blank')
}

/** 权限设置（占位，后续完善） */
function onPermission() {
  // TODO: 后续实现权限设置功能
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

  &.disabled {
    color: #c0c4cc;
    cursor: not-allowed;

    &:hover {
      background: transparent;
    }
  }
}

</style>
