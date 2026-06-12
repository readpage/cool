<template>
  <div class="report-sidebar" :class="{ collapsed }">
    <!-- 展开状态：头部工具栏 -->
    <div class="sidebar-header" v-show="!collapsed">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索报表..."
        clearable
        :prefix-icon="Search"
        size="small"
        @input="onSearch"
      />
      <el-button
        size="small"
        type="primary"
        style="margin-top: 8px; width: 100%"
        @click="onCreate"
      >
        <el-icon :size="14"><Plus /></el-icon>
        新建
      </el-button>
    </div>

    <!-- 展开状态：报表树 -->
    <div class="sidebar-tree" v-show="!collapsed" v-loading="loading">
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="{ children: 'children', label: 'label' }"
        :filter-node-method="filterNode"
        :highlight-current="true"
        node-key="id"
        :default-expand-all="!!searchKeyword"
        :expand-on-click-node="false"
        @node-click="onNodeClick"
      >
        <template #default="{ data }">
          <div
            class="tree-node-content"
            :class="{ 'is-current': data.tableKey === currentTableKey }"
            :title="data.tableKey ? `${data.label} (${data.tableKey})` : data.label"
            @dblclick.stop="onNodeDblClick(data)"
          >
            <el-icon v-if="!data.tableKey" :size="14" class="node-icon category-icon">
              <Folder />
            </el-icon>
            <el-icon v-else :size="14" class="node-icon report-icon">
              <Document />
            </el-icon>
            <span class="node-label">{{ data.label }}</span>
            <el-tag v-if="!data.tableKey" size="small" type="info" class="node-count">
              {{ data.children?.length ?? 0 }}
            </el-tag>
          </div>
        </template>
      </el-tree>
      <el-empty
        v-if="treeData.length === 0 && !loading"
        description="暂无报表"
        :image-size="60"
      />
    </div>

    <!-- 折叠 / 展开按钮 -->
    <div class="sidebar-toggle" @click="collapsed = !collapsed">
      <el-icon :size="16">
        <DArrowLeft v-if="!collapsed" />
        <DArrowRight v-else />
      </el-icon>
      <span v-show="collapsed" class="toggle-text">报表</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, Folder, Document, DArrowLeft, DArrowRight } from '@element-plus/icons-vue'
import { AReport } from '@/api/report'
import { fetchSummary } from '../hooks/useReportView'
import type { ReportSummary } from '@/api/report'

interface TreeNode {
  id: string
  label: string
  tableKey?: string
  children?: TreeNode[]
}

const props = defineProps<{
  currentTableKey: string
}>()

const emit = defineEmits<{
  select: [tableKey: string, summary?: ReportSummary]
  edit: [tableKey: string]
}>()

const router = useRouter()
const treeRef = ref()
const collapsed = ref(false)
const loading = ref(false)
const searchKeyword = ref('')
const list = ref<ReportSummary[]>([])

/** 按分类构建 el-tree 数据 */
const treeData = ref<TreeNode[]>([])

function buildTree(raw: ReportSummary[]): TreeNode[] {
  const categoryMap = new Map<string, ReportSummary[]>()
  raw.forEach(item => {
    const cat = item.category || '未分类'
    if (!categoryMap.has(cat)) categoryMap.set(cat, [])
    categoryMap.get(cat)!.push(item)
  })

  const result: TreeNode[] = []
  categoryMap.forEach((items, cat) => {
    result.push({
      id: `cat-${cat}`,
      label: cat,
      children: items.map(item => ({
        id: item.tableKey,
        label: item.name,
        tableKey: item.tableKey,
      })),
    })
  })
  return result
}

/** 获取报表列表 */
async function fetchList() {
  loading.value = true
  try {
    const data = await fetchSummary()
    list.value = data
    treeData.value = buildTree(list.value)
    // 如果已有选中报表，恢复高亮（等待 el-tree 渲染完成）
    if (props.currentTableKey) {
      await nextTick()
      treeRef.value?.setCurrentKey(props.currentTableKey)
    }
  } catch (err) {
    console.error('获取报表列表失败', err)
  } finally {
    loading.value = false
  }
}

/** 搜索过滤 */
function filterNode(value: string, data: TreeNode): boolean {
  if (!value) return true
  const kw = value.toLowerCase()
  return (
    data.label.toLowerCase().includes(kw) ||
    (data.tableKey && data.tableKey.toLowerCase().includes(kw))
  )
}

function onSearch() {
  treeRef.value?.filter(searchKeyword.value)
}

/** 单击树节点 → 切换到对应报表 */
function onNodeClick(data: TreeNode) {
  if (data.tableKey) {
    const summary = list.value.find(s => s.tableKey === data.tableKey)
    emit('select', data.tableKey, summary)
  }
}

/** 双击树节点 → 新窗口打开编辑 */
function onNodeDblClick(data: TreeNode) {
  if (data.tableKey) {
    emit('edit', data.tableKey)
  }
}

/** 新建报表 → 跳转 report-design */
function onCreate() {
  router.push({ path: '/report/design' })
}

/** 监听外部传入的 currentTableKey，同步高亮 */
watch(
  () => props.currentTableKey,
  (key) => {
    if (key && treeRef.value) {
      treeRef.value.setCurrentKey(key)
    }
  },
)

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.report-sidebar {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  width: 220px;
  min-width: 0;
  transition: width 0.25s ease;
  overflow: hidden;
  flex-shrink: 0;

  &.collapsed {
    width: 40px;
  }
}

.sidebar-header {
  padding: 10px 10px 4px;
  flex-shrink: 0;
}

.sidebar-tree {
  flex: 1;
  min-height: 0;
  overflow: hidden auto;
  padding: 4px 4px 8px 4px;

  :deep(.el-tree) {
    background: transparent;
    .el-tree-node__content {
      height: 32px;
      padding-right: 6px;
      border-radius: 4px;

      &:hover {
        background: #f0f2f5;
      }
    }
    .el-tree-node.is-current > .el-tree-node__content {
      background: #ecf5ff;
      color: #409eff;
    }
  }
}

.tree-node-content {
  display: flex;
  align-items: center;
  width: 100%;
  overflow: hidden;
  user-select: none;

  .node-icon {
    flex-shrink: 0;
    margin-right: 5px;
  }
  .category-icon {
    color: #909399;
  }
  .report-icon {
    color: #409eff;
  }
  .node-label {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 13px;
  }
  .node-count {
    flex-shrink: 0;
    margin-left: 4px;
    font-size: 11px;
    height: 18px;
    line-height: 18px;
    padding: 0 5px;
  }
}

.sidebar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 36px;
  flex-shrink: 0;
  cursor: pointer;
  color: #909399;
  border-top: 1px solid #f0f0f0;
  transition: background 0.2s, color 0.2s;
  writing-mode: horizontal-tb;
  white-space: nowrap;

  &:hover {
    background: #f5f7fa;
    color: #409eff;
  }

  .toggle-text {
    font-size: 12px;
  }
}

/* collapsed 状态下的竖排文字 */
.report-sidebar.collapsed .sidebar-toggle {
  height: 100%;
  border-top: none;

  .toggle-text {
    writing-mode: vertical-lr;
    letter-spacing: 4px;
  }
}
</style>
