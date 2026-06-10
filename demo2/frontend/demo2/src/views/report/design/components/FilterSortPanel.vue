<template>
  <div class="filter-sort-panel">
    <!-- 筛选条件 -->
    <div v-if="showFilter" class="config-section">
      <div class="section-header">
        <span class="section-title">筛选条件 <code class="syntax-hint">&#123;&#123;filter&#125;&#125;</code></span>
        <el-button size="small" link @click="$emit('addFilter')">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
      <el-empty
        v-if="filterConditions.length === 0"
        description="暂未配置筛选条件"
        :image-size="50"
      />
      <div v-for="(f, idx) in filterConditions" :key="idx" class="condition-row">
        <el-select v-model="f.column" placeholder="列" size="small" style="width:100px" filterable>
          <el-option v-for="c in availableColumns" :key="c.prop" :label="c.label" :value="c.prop" />
        </el-select>
        <el-select v-model="f.operator" size="small" style="width:80px">
          <el-option label="等于" value="eq" />
          <el-option label="≠" value="ne" />
          <el-option label="大于" value="gt" />
          <el-option label="小于" value="lt" />
          <el-option label="≥" value="gte" />
          <el-option label="≤" value="lte" />
          <el-option label="包含" value="like" />
          <el-option label="区间" value="between" />
        </el-select>
        <el-date-picker
          v-if="f.operator === 'between'"
          v-model="f.value"
          type="daterange"
          range-separator="至"
          start-placeholder="开始"
          end-placeholder="结束"
          size="small"
          style="flex:1;min-width:180px"
          value-format="YYYY-MM-DD"
        />
        <el-input v-else v-model="f.value" placeholder="值" size="small" style="flex:1;min-width:80px" />
        <el-button type="danger" link size="small" @click="$emit('removeFilter', idx)">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 排序条件 -->
    <div v-if="showSort" class="config-section">
      <div class="section-header">
        <span class="section-title">排序 <code class="syntax-hint">&#123;&#123;sort&#125;&#125;</code></span>
        <el-button size="small" link @click="$emit('addSort')">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
      <el-empty
        v-if="sortConditions.length === 0"
        description="暂未配置排序条件"
        :image-size="50"
      />
      <div v-for="(s, idx) in sortConditions" :key="idx" class="condition-row">
        <el-select v-model="s.column" placeholder="列" size="small" style="flex:1" filterable>
          <el-option v-for="c in availableColumns" :key="c.prop" :label="c.label" :value="c.prop" />
        </el-select>
        <el-radio-group v-model="s.direction" size="small">
          <el-radio-button value="asc">升序</el-radio-button>
          <el-radio-button value="desc">降序</el-radio-button>
        </el-radio-group>
        <el-button type="danger" link size="small" @click="$emit('removeSort', idx)">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 提示 -->
    <div v-if="(showFilter || showSort) && availableColumns.length === 0" class="hint-tip">
      请先执行一次查询以获取可用列
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus, Delete } from '@element-plus/icons-vue'
import type { FilterCondition, SortCondition } from '../../types/report'

defineProps<{
  availableColumns: { prop: string; label: string }[]
  filterConditions: FilterCondition[]
  sortConditions: SortCondition[]
  showFilter?: boolean
  showSort?: boolean
}>()

defineEmits<{
  addFilter: []
  removeFilter: [idx: number]
  addSort: []
  removeSort: [idx: number]
}>()
</script>

<style lang="scss" scoped>
.filter-sort-panel {
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
  overflow-y: auto;
}

.config-section {
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;

  &::before {
    content: '';
    display: inline-block;
    width: 3px;
    height: 14px;
    background: #67c23a;
    border-radius: 2px;
  }
}

.condition-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
  background: #fff;
  padding: 6px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
}

.hint-tip {
  padding: 12px 14px;
  font-size: 12px;
  color: #909399;
  text-align: center;
  background: #fdf6ec;
  margin: 8px 14px;
  border-radius: 4px;
}

.syntax-hint {
  font-size: 11px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 1px 5px;
  border-radius: 3px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-weight: 400;
}

::deep(.el-empty) {
  padding: 12px 0;
}

::deep(.el-empty__description p) {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
