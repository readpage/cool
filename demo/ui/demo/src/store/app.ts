import { defineStore } from 'pinia'

export const useAppStore =  defineStore('app', {
  state: () => ({
    count: 0,
  }),
  getters: {
    doubleCount(state) {
      return state.count * 2
    },
  },
  actions: {
    increment() {
      this.count++
    }
  }
})
