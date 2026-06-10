/**
 * 数据源管理 API — 对应 ReportController 数据源端点
 */
import { apiAxios } from './src/requests'

export interface DatasourceEntity {
  id?: number
  name: string
  dbType: string
  host: string
  port: number
  dbName: string
  username: string
  description?: string
  password?: string
  params?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export const ADatasource = {
  /** 数据源列表 — GET /report/datasource/list */
  list: apiAxios<DatasourceEntity[]>('/report/datasource/list', 'get'),

  /** 新增/编辑 — POST /report/datasource/save */
  save: apiAxios<number>('/report/datasource/save', 'post'),

  /** 删除 — DELETE /report/datasource/remove/{id} */
  remove: (id: number) =>
    apiAxios<string>(`/report/datasource/remove/${id}`, 'delete')(),

  /** 测试连接 — POST /report/datasource/test */
  test: apiAxios<boolean>('/report/datasource/test', 'post'),
}
