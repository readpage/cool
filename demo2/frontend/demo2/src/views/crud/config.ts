import type { TableConfig } from '@/types/table'
import type { FormItemConfig } from '@/components/crud/types'

// ==================== 表格配置 ====================

export const initTableConfig = (): TableConfig => ({
  columns: [
    { prop: 'id',         label: 'ID',       minWidth: 80,  align: 'center', hidden: true },
    { prop: 'username',   label: '用户名',   minWidth: 140 },
    { prop: 'sex',        label: '性别',     minWidth: 80,  align: 'center', fieldType: 'remote-select', format: 'tag' },
    { prop: 'age',        label: '年龄',     minWidth: 80 },
    { prop: 'phone',      label: '电话',     minWidth: 160 },
    { prop: 'createTime', label: '创建时间', minWidth: 180, align: 'center' },
    { prop: 'updateTime', label: '修改时间', minWidth: 180, align: 'center' },
  ],
  stripe: true,
  search: {
    currentField: 'all',
    filter: [
      { prop: 'username',   label: '用户名',   operator: 'contains', filterMode: 'exposed' },
      { prop: 'sex',        label: '性别',     operator: 'eq',       fieldType: 'remote-select', filterMode: 'exposed' },
      { prop: 'phone',      label: '电话',     operator: 'contains' },
      { prop: 'createTime', label: '创建时间', operator: 'between',  fieldType: 'daterange', filterMode: 'exposed' },
      { prop: 'updateTime', label: '修改时间', operator: 'between',  fieldType: 'daterange' },
    ],
  },
})

// ==================== 表单字段配置 ====================

export const formItems: FormItemConfig[] = [
  { prop: 'username',   label: '用户名',   required: true },
  { prop: 'sex',        label: '性别',     fieldType: 'remote-select', required: true },
  { prop: 'age',        label: '年龄',     fieldType: 'number', required: true, componentProps: { min: 0, max: 150 } },
  { prop: 'phone',      label: '电话',     required: true }
]
