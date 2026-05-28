import type { App } from 'vue'
import { animation } from './directives'
import { installSelectBlurFix } from './selectBlur'
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import 'undraw-ui/es/components/toast/toast.css'
import 'undraw-ui/es/components/context-menu/context-menu.css'

export default {
  install: (app: App<Element>) => {
    app.directive('animation', animation)
    installSelectBlurFix()
  }
}
