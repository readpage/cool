import { storage } from 'undraw-ui'
import { ref, watch, watchEffect } from 'vue'

/**
 * @param name default theme
 */
function useDark(name = 'u-theme') {
  const colorScheme = !!(window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches)
    ? 'dark'
    : 'light'
  const theme = storage.get<string>(name)
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  // 当前模式是否是深色模式
  const isDark = ref(theme === 'dark' || (theme === 'auto' && colorScheme === 'dark'))
  const lock = ref(false)

  // 监听isDark
  watchEffect(() => {
    if (isDark.value) {
      document.documentElement.classList.add('dark')
      if (!lock.value) {
        if (colorScheme == 'dark') {
          storage.set(name, 'auto')
        } else {
          storage.set(name, 'dark')
        }
      }
    } else {
      document.documentElement.classList.remove('dark')
      if (!lock.value) {
        if (colorScheme == 'light') {
          storage.set(name, 'auto')
        } else {
          storage.set(name, 'light')
        }
      }
    }
    lock.value = false
  })

  const handler = (e: MediaQueryListEvent) => {
    if (storage.get(name) == 'auto') {
      lock.value = true
      isDark.value = e.matches
    }
  }

  mediaQuery.addEventListener('change', handler)
  return isDark
}

export const isDark = ref(useDark())
