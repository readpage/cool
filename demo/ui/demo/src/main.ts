import { createApp } from 'vue'
import App from './App.vue'
import 'uno.css'
import pinia from './store'
import router from './router'
import plugins from './plugins'

const app = createApp(App)
app.use(plugins)
app.use(pinia)
app.use(router)
app.mount('#app')