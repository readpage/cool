import { createApp } from 'vue'
import App from './App.vue'
import 'uno.css'
import pinia from './store'
import router from './router'
import plugins from './plugins'
import { useAppStore } from './store/app'
import { bindToken } from './api/src/token'

const app = createApp(App)
app.use(pinia)

app.use(plugins)
app.use(router)
app.mount('#app')

// 应用初始化：预加载所有表格配置 & 恢复选项缓存
// 延迟到下一帧执行，避免阻塞首屏渲染
const appStore = useAppStore()
requestAnimationFrame(() => {
  appStore.init()
})