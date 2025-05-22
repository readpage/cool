import { isNull } from 'undraw-ui'
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'home',
    component: () => import('@/views/home/index.vue'),
    meta: {
      title: '首页'
    }
  },
  {
    path: '/demo',
    name: 'demo',
    component: () => import('@/views/demo/index.vue'),
    meta: {
      title: '演示'
    }
  },
  {
    path: '/stream',
    name: 'stream',
    component: () => import('@/views/stream/index.vue'),
    meta: {
      title: 'steam'
    }
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: routes
})

router.beforeEach(async (to, form, next) => {
  let title = to.meta.title as string
  document.title = title ? title : isNull(document.querySelector('title')?.text, 'loading...')
  next()
})

export default router
