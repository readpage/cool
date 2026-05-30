export interface PageApi<T = any> {
  total: number
  list: T[]
}

export * from './src/requests'
export * from './user'
export * from './config'