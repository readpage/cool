import axios from 'axios'
import { ElMessage } from 'element-plus'
import { ResApi } from './requests'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

const service = axios.create({
  baseURL: '/api'
})
let num = 0
// 重试队列，每一项将是一个待执行的函数形式
let retryRequests: any[] = []
// 是否正在刷新的标记
let isRefreshing = false

service.interceptors.request.use(
  config => {
    if ((config as any).progress) {
      inc()
    }
    return config
  },
  error => {
    ElMessage.error('服务器出问题了')
    return Promise.reject(error)
  }
)

const inc = async () => {
  num > 0 && num--
  NProgress.inc()
  setTimeout(() => {
    if (num <= 0) {
      NProgress.done()
    }
  }, 200)
}

service.interceptors.response.use(
  response => {
    // Math.trunc(res.code / 100) == 2； Math.trunc去除一个数的小数部分
    const data = response.data as ResApi
    if (data.code == 200 || !data.code) {
      return response
    }
    // 无效或过期token
    else if (data.code == 407) {
      return doRequest(response)
    } else {
      ElMessage.warning(data.msg)
    }
    return Promise.reject(data.msg)
  },
  error => {
    num> 0 && inc()
    NProgress.done()
    return Promise.reject(error)
  }
)

function doRequest(res: any): Promise<any> {
  const config = res.config
  if (!isRefreshing) {
    isRefreshing = true
    return new Promise(res)
  } else {
    return new Promise(resolve => {
      retryRequests.push(() => {
        resolve(service(config))
      })
    })
  }
}

export default service
