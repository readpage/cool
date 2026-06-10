import { apiAxios, download } from './src/requests'
import type { PageResult } from '@/components/table/types'

export const AUser = {
  /** 分页查询 */
  page: apiAxios<PageResult>('/user/page', 'post'),
  /** 新增/修改（id=null 新增，id≠null 修改） */
  save: apiAxios<boolean>('/user/save', 'post'),
  /** 批量删除 */
  remove: apiAxios<boolean>('/user/remove', 'delete'),
  /** 导出 Excel */
  export: download('/user/export', 'post'),
  /** 下载导入模板 */
  downloadTemplate: download('/user/template', 'post'),
  /** 导入 Excel（multipart/form-data） */
  importExcel: (formData: FormData) =>
    fetch('/api/user/import', {
      method: 'POST',
      body: formData,
    }).then(res => res.json()),
}
