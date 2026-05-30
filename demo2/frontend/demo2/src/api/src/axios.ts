import axios from 'axios'
import { ElMessage } from 'element-plus'
import { ResApi } from './requests'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getAuth } from './token'

const service = axios.create({
  baseURL: '/api'
})
let num = 0

service.interceptors.request.use(
  config => {
    if (num++ == 0) {
      NProgress.start()
    }
    const auth = getAuth()
    if (auth && config.url && !config.url.includes('/refreshToken')) {
      config.headers.Authorization = auth
    }
    return config
  },
  error => {
    ElMessage.error('服务器出问题了')
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    inc()
    // const userStore = useUserStore()
    const data = response.data as ResApi
    if (data.code == 200 || data.code == null) {
      return response
    }
    // 过期token → 自动刷新并重试
    else if (data.code == 407) {
      // return userStore.retryAfterRefresh(data, () => service(response.config))
    } else if (data.code == 408) {
      // token 刷新失败，提示重新登录（带防抖）
      // userStore.handleTokenExpired()
    } else if (data.code == 402) {
      // 未登录，静默处理，不提示
    } else {
      ElMessage.warning(data.msg)
    }
    return Promise.reject(data)
  },
  error => {
    inc()
    NProgress.done()
    return Promise.reject(error)
  }
)

const inc = () => {
  num--
  NProgress.inc()
  setTimeout(() => {
    if (num <= 0) {
      NProgress.done()
    }
  }, 200)
}

export default service
