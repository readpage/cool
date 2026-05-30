import { apiAxios } from './src/requests'
import type { PageResult } from '@/types/table'
import type { TableQuery } from '@/types/table'

/** 分页查询用户列表 */
export const pageUser = apiAxios<PageResult>('/user/page', 'post')

/** 新增/修改用户（id=null 新增，id≠null 修改） */
export const saveUser = apiAxios<boolean>('/user/save', 'post')

/** 批量删除用户 */
export const removeUsers = apiAxios<boolean>('/user/remove', 'delete')
