import { isEmpty, isNull, removeEmptyField } from 'undraw-ui'
import axios from './axios'

interface ParamApi {
  url: string
  method: 'get' | 'post' | 'put' | 'delete' | string
  params?: any
  data?: any
  paramsSerializer: (val: any) => string
}

export interface ReqApi {
  url?: string
  params?: any
  data?: any
}

export interface ResApi<T = any> {
  code: number
  data: T
  msg: string
}

/**
 * @param url
 * @param method
 * @returns
 */
export function apiAxios<T = any>(url: string, method = 'get') {
  /**
   * data: string => url | {url, ...other} => url = {url}, params = {...other}
   */
  return (data?: string | object) => {
    let param: ParamApi = { url: url, method: method, paramsSerializer: params => toParams(params) }
    if (typeof data == 'string') {
      param.url += data
    } else if (typeof data == 'object') {
      let temp = data as any
      if (!isEmpty(temp.url)) {
        param.url += temp.url
        delete temp.url
      }
      if (method == 'get') {
        param.params = data
      } else {
        param.data = data
      }
    }
    return axios(param as any).then(res => {
      return res.data as ResApi<T>
    })
  }
}

function toParams(obj: any, serializeArrays = false) {
  obj = removeEmptyField(obj)
  const params = new URLSearchParams()
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      const value = obj[key]
      if (Array.isArray(value)) {
        if (serializeArrays) {
          // 将数组序列化为逗号分隔的字符串
          params.append(key, value.join(','))
        } else {
          // 将数组的每个元素作为单独的参数添加
          value.forEach(item => {
            params.append(key, item)
          })
        }
      } else {
        // 如果值不是数组，直接追加
        params.append(key, value)
      }
    }
  }
  return params.toString()
}

export function download(url: string, method = 'get') {
  let param: ParamApi = { url: url, method: method, paramsSerializer: params => toParams(params) }
  return (data?: string | object) => {
    if (method == 'get') {
      param.params = data
    } else {
      param.data = data
    }
    return axios({
      ...param,
      responseType: 'blob',
      onDownloadProgress: progressEvent => {
        // getProgress.close()
        // 获取到已下载的大小
        const loaded = progressEvent.loaded
        // 获取到文件总大小
        // const total = progressEvent.total
        // progress.process = 99
        // 计算下载进度
        // const percent = total ? parseFloat(((loaded / total) * 100).toFixed(2)) : 100
        // progress.content = `下载中...     ${(loaded / 1024 / 1024).toFixed(2)}MB`
        // 打印下载进度
        // console.log(loaded, total, '下载进度：' + percent + '%')
      }
    }).then(res => {
      // 从响应头中获取Content-Disposition
      const contentDisposition = res.headers['content-disposition']
      if (contentDisposition) {
        let filename = contentDisposition.split('filename=')[1]
        const url = window.URL.createObjectURL(new Blob([res.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', decodeURIComponent(filename))
        document.body.appendChild(link)
        link.click()
        return true
      } else {
        return false
      }
    })
  }
}
