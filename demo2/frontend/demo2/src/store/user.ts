import { defineStore } from 'pinia'

/** 用户信息 */
export interface UserInfo {
  id?: number
  username?: string
  nickname?: string
  auths?: string[]
}

export const useUserStore = defineStore('user', {
  state: () => ({
    info: {} as UserInfo,
  }),
})
