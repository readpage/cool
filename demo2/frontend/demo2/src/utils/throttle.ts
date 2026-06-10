/**
 * Promise 安全的节流函数：保持 Promise 链完整性，不会丢弃调用
 *
 * 与 undraw-ui throttle 的关键区别：
 *   - 被限流期间的新调用会被"尾调用"（执行最后一次，而非直接丢弃）
 *   - 始终返回 Promise<ReturnType<T>>，确保 async/await 不会因限流而悬挂
 *   - 提供 .cancel() 方法取消等待中的尾调用
 */
export function throttlePromise<T extends (...args: any[]) => Promise<any>>(fn: T, interval = 300) {
  let running = false
  let pendingArgs: Parameters<T> | null = null
  let pendingResolve: ((value: ReturnType<T>) => void) | null = null
  let pendingReject: ((reason: any) => void) | null = null
  let timer: ReturnType<typeof setTimeout> | null = null

  const run = (args: Parameters<T>): Promise<ReturnType<T>> => {
    running = true
    return fn(...args).finally(() => {
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      running = false
      // 尾调用：如果有排队参数，立即执行
      if (pendingArgs) {
        const args = pendingArgs
        const resolve = pendingResolve
        const reject = pendingReject
        pendingArgs = null
        pendingResolve = null
        pendingReject = null
        run(args).then(resolve!, reject!)
      }
    })
  }

  const throttled = (...args: Parameters<T>): Promise<ReturnType<T>> => {
    if (!running) {
      return run(args)
    }
    // 正在执行中：保存为尾调用参数，返回 Promise 在上一次完成后 resolve
    pendingArgs = args
    if (!timer) {
      timer = setTimeout(runPendingAfterInterval, interval)
    }
    return new Promise<ReturnType<T>>((resolve, reject) => {
      pendingResolve = resolve
      pendingReject = reject
    })
  }

  /** interval 到期后若还有 pending 参数，强制执行（兜底） */
  function runPendingAfterInterval() {
    timer = null
    if (pendingArgs && pendingResolve) {
      const args = pendingArgs
      const resolve = pendingResolve
      const reject = pendingReject
      pendingArgs = null
      pendingResolve = null
      pendingReject = null
      running = false // 重置 running，让 run() 可以执行
      run(args).then(resolve!, reject!)
    }
  }

  throttled.cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
    pendingArgs = null
    pendingResolve = null
    pendingReject = null
    running = false
  }

  return throttled
}

/**
 * 同步节流：返回 true 表示被限流（应跳过）
 * 适用于不需要 Promise 链的纯同步操作
 */
export function createCallThrottle(ms = 300) {
  let lastTime = 0
  return () => {
    const now = Date.now()
    if (now - lastTime < ms) return true
    lastTime = now
    return false
  }
}
