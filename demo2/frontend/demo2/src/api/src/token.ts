type Provider = () => {
  access_token?: string
  token_type?: string
} | null | undefined

let _provider: Provider | null = null

/** 绑定 token 来源（main.ts 初始化时调用一次） */
export function bindToken(provider: Provider) {
  _provider = provider
}

/** 获取认证头，如 "bearer xxx"；无 token 返回 null */
export function getAuth(): string | null {
  const token = _provider?.()
  if (!token?.access_token) return null
  const type = token.token_type || 'bearer'
  return `${type} ${token.access_token}`
}
