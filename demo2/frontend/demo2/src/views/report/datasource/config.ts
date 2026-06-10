import type { TableConfig } from '../types/table'
import type { FormItemConfig } from '@/components/crud/types'

// ==================== 选项常量 ====================

const dbTypeOptions = [
  { label: 'MySQL', value: 'MYSQL' },
  { label: 'PostgreSQL', value: 'POSTGRESQL' },
  { label: 'SqlServer', value: 'SQLSERVER' },
]

const statusOptions = [
  { label: '启用', value: '1', style: { dotColor: '#67c23a' } },
  { label: '停用', value: '0', style: { dotColor: '#f56c6c' } },
]

// ==================== 表格配置 ====================

export const initTableConfig = (): TableConfig => ({
  columns: [
    { prop: 'name',       label: '名称',       minWidth: 140 },
    { prop: 'dbType',     label: '类型',       minWidth: 100, align: 'center', fieldType: 'select', format: 'tag',  options: dbTypeOptions },
    { prop: 'host',       label: '主机',       minWidth: 130 },
    { prop: 'port',       label: '端口',       width: 80,     align: 'center' },
    { prop: 'dbName',     label: '库名',       minWidth: 120 },
    { prop: 'username',   label: '用户名',     width: 120 },
    { prop: 'status',     label: '状态',       width: 80,     align: 'center', fieldType: 'select', format: 'dot',  options: statusOptions },
    { prop: 'createTime', label: '创建时间',   minWidth: 180, align: 'center' },
    { prop: 'updateTime', label: '修改时间',   minWidth: 180, align: 'center' },
    { prop: 'actions',    label: '操作',       width: 100,    fixed: 'right' },
  ],
  search: {
    currentField: 'all',
    filter: [
      { prop: 'name',   label: '名称', operator: 'contains', filterMode: 'exposed' },
      { prop: 'dbType', label: '类型', operator: 'eq',       fieldType: 'select', filterMode: 'exposed', options: dbTypeOptions },
    ],
  },
})

// ==================== 表单字段配置 ====================

export const formItems: FormItemConfig[] = [
  { prop: 'name',     label: '名称',       required: true,  placeholder: '如：业务主库' },
  { prop: 'dbType',   label: '数据库类型', fieldType: 'select', required: true,  options: dbTypeOptions, defaultValue: 'MYSQL' },
  { prop: 'host',        label: '主机',       required: true,  placeholder: '127.0.0.1', defaultValue: '127.0.0.1' },
  { prop: 'port',        label: '端口',       fieldType: 'number', required: true,  componentProps: { min: 1, max: 65535 }, defaultValue: 3306 },
  { prop: 'dbName',      label: '库名',       required: true,  placeholder: 'cool' },
  { prop: 'username',    label: '用户名',     required: true },
  { prop: 'password',    label: '密码',       fieldType: 'password', placeholder: '编辑时留空则不修改密码' },
  { prop: 'description', label: '描述',       placeholder: '选填描述信息' },
  { prop: 'params',      label: '额外参数',   placeholder: 'useSSL=false&serverTimezone=GMT%2B8', defaultValue: 'useSSL=false&serverTimezone=GMT%2B8' },
]
