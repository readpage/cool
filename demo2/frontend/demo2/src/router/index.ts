import { isNull } from 'undraw-ui'
import { createRouter, createWebHistory } from 'vue-router'
import arr from './tmp'


const router = createRouter({
  history: createWebHistory(),
  routes: arr,
  linkActiveClass: 'active'
})

router.beforeEach(async (to, form, next) => {
  let title = to.meta.title as string
  document.title = title ? title : isNull(document.querySelector('title')?.text, 'loading...')
  next()
})

export default router
