import { isEmpty, removeEmptyField } from 'undraw-ui'
import axios from './axios'
// import { fetchEventSource, FetchEventSourceInit } from '@echofly/fetch-event-source'
import { getAuth } from './token'


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

export function toParams(obj: any, serializeArrays = false) {
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

/**
 * 通过 axios（带 token）加载图片，返回 blob URL
 * 解决 <img> 标签直接引用后端地址时无法携带 token 的问题
 *
 * @param imageUrl 原始图片 URL（如 http://xxx:8080/ai-service/file/download?objectName=xxx）
 * @returns blob URL，可直接赋给 <img :src>
 */
export async function getAuthImageUrl(imageUrl: string): Promise<string> {
  // 将 IP 直连 URL 转为同源代理路径（避免跨域）
  // http://xxx:8080/ai-service/file/... → /ai-service/file/...
  // axios.baseURL = '/api' → 最终请求 /api/ai-service/file/... → Vite proxy 转发
  const proxyPath = imageUrl.replace(/^https?:\/\/[^/]+/, '')
  const res = await axios({
    url: proxyPath,
    method: 'get',
    responseType: 'blob',
  })
  return URL.createObjectURL(res.data)
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

// export function stream(url: string) {
//   return (options: Omit<FetchEventSourceInit, 'method' | 'headers'> = {}) => {
//     const auth = getAuth()
//     return fetchEventSource(`/api${url}`, {
//       method: 'POST',
//       headers: {
//         ...(auth ? { 'Authorization': auth } : {}),
//         'Content-Type': 'application/json'
//       },
//       ...options
//     })
//   }
// }

/**
 * 文件上传（multipart/form-data），纯 XHR 流式模式
 *
 * 返回 undefined，通过 streamCallbacks 回调获取结果
 */
export function uploadFile(url: string) {
  return (formData: FormData, options: {
    onUploadProgress?: (percent: number) => void
    streamCallbacks: {
      onProgress?: (stage: string) => void
      onResult?: (data: any) => void
      onError?: (err: any) => void
    }
  }) => {
    const auth = getAuth()
    const { onProgress, onResult, onError } = options.streamCallbacks
    const { onUploadProgress } = options
    const xhr = new XMLHttpRequest()

    xhr.open('POST', `/api${url}`, true)
    if (auth) xhr.setRequestHeader('Authorization', auth)

    if (onUploadProgress) {
      xhr.upload.onprogress = (event) => {
        if (event.total) {
          onUploadProgress(Math.round((event.loaded / event.total) * 100))
        }
      }
    }

    // 增量解析：下载响应过程中实时提取 PROGRESS 行
    let lastProcessedLen = 0
    xhr.onprogress = () => {
      const text = xhr.responseText || ''
      const newChunk = text.substring(lastProcessedLen)
      lastProcessedLen = text.length
      for (const rawLine of newChunk.split('\n')) {
        const line = rawLine.trim()
        const match = line.match(/^(?:data:)?PROGRESS:(.*)/)
        if (match) {
          onProgress?.(match[1])
        }
      }
    }

    xhr.onreadystatechange = () => {
      if (xhr.readyState === 4) {
        const fullText = xhr.responseText || ''
        if (!fullText) { onError?.('服务器返回为空'); return }

        const resultMatch = fullText.match(/^(?:data:)?RESULT:([\s\S]*)/m)
        if (resultMatch) {
          const raw = resultMatch[1].trim()
          try {
            const data = JSON.parse(raw)
            setTimeout(() => onResult?.(data), 0)
          } catch (e) {
            console.error('[uploadFile] JSON parse error:', e)
            setTimeout(() => onError?.('解析响应失败'), 0)
          }
        } else {
          try {
            const data = JSON.parse(fullText.trim())
            setTimeout(() => onResult?.(data), 0)
          } catch {
            setTimeout(() => onError?.('分析失败，未获取到结果'), 0)
          }
        }
      }
    }

    xhr.onerror = () => onError?.('网络异常，上传失败')
    xhr.send(formData)
  }
}