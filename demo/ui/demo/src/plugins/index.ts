import { App } from 'vue'
import { animate } from './directives'
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import 'undraw-ui/es/components/toast/toast.css'
import 'undraw-ui/es/components/context-menu/context-menu.css'

export default {
  install: (app: App<Element>) => {
    app.directive('animate', animate)
  }
}
