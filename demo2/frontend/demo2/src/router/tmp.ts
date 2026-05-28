const modules = import.meta.glob('/src/views/**/index.vue')

const arr: any[] = []
Object.entries(modules).forEach(([key, value]) => {
  const regex = /([^\/]+)\/index.vue/
  const match = key.match(regex)
  if (match) {
    arr.push({ path: `/${match[1]}`, name: match[1],  component: value, meta: { title: match[1] }})
  }
})
arr.push({
  path: '/',
  redirect: '/user'
})

export default arr