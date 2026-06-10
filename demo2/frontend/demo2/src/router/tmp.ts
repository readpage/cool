const modules = import.meta.glob('/src/views/**/index.vue')

const arr: any[] = []
Object.entries(modules).forEach(([key, value]) => {
  // 去掉 /src/views 前缀和 /index.vue 后缀，得到路径（支持嵌套目录）
  const routePath = key.replace('/src/views', '').replace(/\/index\.vue$/, '')
  const name = routePath.split('/').pop()!
  arr.push({ path: routePath, name, component: value, meta: { title: name } })
})
arr.push({
  path: '/',
  redirect: '/crud'
})

export default arr