import { createApp } from 'vue'
import App from './App.vue'
import 'uno.css'
import pinia from './store'
import router from './router'
import plugins from './plugins'
import { useAppStore } from './store/app'

const app = createApp(App)
app.use(pinia)

// 应用初始化：预加载所有表格配置
const appStore = useAppStore()
appStore.init()

app.use(plugins)
app.use(router)
app.mount('#app')