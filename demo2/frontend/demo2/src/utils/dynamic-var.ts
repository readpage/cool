import { dt } from './datetime'

// ================== 上下文 ==================

export interface DynamicContext {
  userId?: string | number
  userName?: string
  deptId?: string | number
  deptName?: string
  tenantId?: string | number
  roleId?: string | number
}

let globalCtx: DynamicContext = {}

/** 设置全局上下文（用户变量取值来源） */
export function setDynamicContext(ctx: DynamicContext): void {
  globalCtx = { ...ctx }
}

/** 获取当前上下文副本 */
export function getDynamicContext(): DynamicContext {
  return { ...globalCtx }
}

// ================== 变量注册表 ==================

type VarResult = string | [string, string] | null
type VarFn = (ctx?: DynamicContext) => VarResult

const BUILTIN_VARS: Record<string, VarFn> = {
  // ── 时间类 ──
  '$today':       () => dt().format('YYYY-MM-DD'),
  '$now':         () => dt().format('YYYY-MM-DD HH:mm:ss'),
  '$yesterday':   () => dt().subtract(1, 'day').format('YYYY-MM-DD'),
  '$tomorrow':    () => dt().add(1, 'day').format('YYYY-MM-DD'),

  '$thisWeek':    () => [dt().startOf('week').format('YYYY-MM-DD'), dt().endOf('week').format('YYYY-MM-DD')],
  '$lastWeek':    () => [
    dt().subtract(1, 'week').startOf('week').format('YYYY-MM-DD'),
    dt().subtract(1, 'week').endOf('week').format('YYYY-MM-DD'),
  ],

  '$thisMonth':   () => [dt().startOf('month').format('YYYY-MM-DD'), dt().endOf('month').format('YYYY-MM-DD')],
  '$lastMonth':   () => [
    dt().subtract(1, 'month').startOf('month').format('YYYY-MM-DD'),
    dt().subtract(1, 'month').endOf('month').format('YYYY-MM-DD'),
  ],

  '$thisQuarter': () => [dt().startOf('quarter').format('YYYY-MM-DD'), dt().endOf('quarter').format('YYYY-MM-DD')],
  '$thisYear':    () => String(dt().year()),
  '$lastYear':    () => String(dt().year() - 1),

  // ── 用户类（需要上下文）──
  '$userId':      (ctx) => ctx?.userId?.toString() ?? '',
  '$userName':    (ctx) => ctx?.userName ?? '',
  '$deptId':      (ctx) => ctx?.deptId?.toString() ?? '',
  '$deptName':    (ctx) => ctx?.deptName ?? '',
  '$tenantId':    (ctx) => ctx?.tenantId?.toString() ?? '',
  '$roleId':      (ctx) => ctx?.roleId?.toString() ?? '',

  // ── 系统类（透传给后端解析，不在此处求值）──
  '$serverDate':  () => null,
  '$serverNow':   () => null,
}

const registry: Record<string, VarFn> = { ...BUILTIN_VARS }

/** 注册自定义变量 */
export function registerDynamicVar(key: string, fn: VarFn): void {
  registry[key] = fn
}

/** 移除变量 */
export function unregisterDynamicVar(key: string): void {
  delete registry[key]
}

/** 查看已注册变量 */
export function getRegisteredVars(): string[] {
  return Object.keys(registry)
}

// ================== 解析引擎 ==================

/**
 * 判断值是否为动态变量引用（以 $ 开头的字符串）
 */
export function isDynamicVar(value: unknown): boolean {
  return typeof value === 'string' && /^\$[\w]+/.test(value)
}

/**
 * 展开一个变量引用
 * 支持 $varName（返回默认值）、$varName:start（区间起始）、$varName:end（区间结束）
 */
function resolveOne(key: string, suffix: string | undefined, ctx: DynamicContext): string {
  const fn = registry['$' + key]
  if (!fn) return `$${key}` + (suffix ? `:${suffix}` : '')

  const result = fn(ctx)
  if (result == null) return `$${key}` + (suffix ? `:${suffix}` : '') // 透传

  if (Array.isArray(result)) {
    if (suffix === 'start') return result[0]
    if (suffix === 'end')   return result[1]
    return `${result[0]} ~ ${result[1]}` // 默认：区间字符串
  }

  return result
}

/**
 * 递归解析动态变量
 *
 * @param raw   待解析的值（字符串 / 数组 / 对象 / 原始值）
 * @param ctx   可选的上下文（用户信息等），不传使用全局上下文
 *
 * @example
 * resolveValue('$today')                  // '2026-06-11'
 * resolveValue('查询 $thisMonth:start')    // '查询 2026-06-01'
 * resolveValue({ from: '$yesterday', to: '$today' })
 *   // { from: '2026-06-10', to: '2026-06-11' }
 */
export function resolveValue<T = unknown>(raw: T, ctx?: DynamicContext): T {
  if (raw == null) return raw

  const context = ctx ?? globalCtx

  if (typeof raw === 'string') {
    // 精确匹配单个 $varName → 直接返回解析结果（可能是元组）
    const exactMatch = raw.match(/^\$(\w+)$/)
    if (exactMatch) {
      const fn = registry['$' + exactMatch[1]]
      if (!fn) return raw as unknown as T
      const result = fn(context)
      if (result != null) return result as unknown as T
      return raw as unknown as T
    }

    // 包含动态变量的复合字符串 → 全部展平为字符串
    return raw.replace(/\$(\w+)(?::([^$\s]*))?/g, (_match, key: string, suffix: string) =>
      resolveOne(key, suffix || undefined, context)
    ) as unknown as T
  }

  if (Array.isArray(raw)) {
    return raw.map((item) => resolveValue(item, context)) as unknown as T
  }

  if (typeof raw === 'object') {
    const result: Record<string, unknown> = {}
    for (const key of Object.keys(raw as Record<string, unknown>)) {
      result[key] = resolveValue((raw as Record<string, unknown>)[key], context)
    }
    return result as unknown as T
  }

  return raw
}

// ── 向后兼容别名 ──

/** @deprecated 请使用 `setDynamicContext` */
export const setUserContext = setDynamicContext
/** @deprecated 请使用 `getDynamicContext` */
export const getUserContext = getDynamicContext
/** @deprecated 请使用 `DynamicContext` */
export type UserContext = DynamicContext
