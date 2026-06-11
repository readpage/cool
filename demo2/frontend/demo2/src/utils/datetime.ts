type UnitType = 'year' | 'month' | 'quarter' | 'week' | 'day' | 'hour' | 'minute' | 'second' | 'ms'

// ================== 内部工具 ==================

function toDate(input: any): Date {
  if (input instanceof Date) return new Date(input.getTime())
  if (typeof input === 'string' && /^\d+$/.test(input)) {
    return new Date(parseInt(input))
  }
  return new Date(input)
}

/**
 * 标准化格式化令牌，同时支持 YYYY-MM-DD HH:mm:ss 和 y-m-d h:i:s
 */
function normalizeFormat(fmt: string): string {
  return fmt
    .replace(/YYYY/g, 'y')
    .replace(/yy/gi, 'y')
    .replace(/MM/g, 'm')
    .replace(/DD/g, 'd')
    .replace(/HH/g, 'h')
    .replace(/mm/g, 'i')
    .replace(/SS/g, 's')
}

function doFormat(date: Date, fmt: string): string {
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }

  return fmt.replace(/([ymdhisa])+/g, (_result: string, key: string) => {
    const value = formatObj[key as keyof typeof formatObj]
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value] || ''
    }
    return String(value).padStart(2, '0')
  })
}

// ================== 纯函数工具 ==================

/** 今天 00:00:00 */
export function today(): Date {
  const d = new Date()
  d.setHours(0, 0, 0, 0)
  return d
}

/** 当前时间 */
export function now(): Date {
  return new Date()
}

/** 格式化日期 YYYY-MM-DD */
export function formatDate(date: Date | string | number, fmt?: string): string {
  return formatTime(toDate(date), fmt || 'YYYY-MM-DD')
}

/** 格式化日期时间 YYYY-MM-DD HH:mm:ss */
export function formatDateTime(date: Date | string | number, fmt?: string): string {
  return formatTime(toDate(date), fmt || 'YYYY-MM-DD HH:mm:ss')
}

/** 添加天数，返回新 Date（不可变） */
export function addDays(date: Date, n: number): Date {
  const d = new Date(date)
  d.setDate(d.getDate() + n)
  return d
}

/** 添加月份，返回新 Date（不可变） */
export function addMonths(date: Date, n: number): Date {
  const d = new Date(date)
  const target = d.getDate()
  d.setDate(1)
  d.setMonth(d.getMonth() + n)
  const maxDay = daysInMonth(d.getFullYear(), d.getMonth())
  d.setDate(Math.min(target, maxDay))
  return d
}

/** 添加年份，返回新 Date（不可变） */
export function addYears(date: Date, n: number): Date {
  const d = new Date(date)
  d.setFullYear(d.getFullYear() + n)
  return d
}

/** 减天数 */
export function subtractDays(date: Date, n: number): Date {
  return addDays(date, -n)
}

/** 月初 00:00:00 */
export function startOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1)
}

/** 月末 23:59:59.999 */
export function endOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0, 23, 59, 59, 999)
}

/** 季度初 00:00:00 */
export function startOfQuarter(date: Date): Date {
  const q = Math.floor(date.getMonth() / 3)
  return new Date(date.getFullYear(), q * 3, 1)
}

/** 季度末 23:59:59.999 */
export function endOfQuarter(date: Date): Date {
  const q = Math.floor(date.getMonth() / 3)
  return new Date(date.getFullYear(), (q + 1) * 3, 0, 23, 59, 59, 999)
}

/** 年初 00:00:00 */
export function startOfYear(date: Date): Date {
  return new Date(date.getFullYear(), 0, 1)
}

/** 年末 23:59:59.999 */
export function endOfYear(date: Date): Date {
  return new Date(date.getFullYear(), 11, 31, 23, 59, 59, 999)
}

/**
 * 本周一 00:00:00
 * @param startDay 周起始日：0=周日, 1=周一（默认）
 */
export function startOfWeek(date: Date, startDay: number = 1): Date {
  const d = new Date(date)
  const day = d.getDay()
  const diff = (day < startDay ? 7 : 0) + day - startDay
  d.setDate(d.getDate() - diff)
  d.setHours(0, 0, 0, 0)
  return d
}

/**
 * 本周日 23:59:59.999
 * @param startDay 周起始日：0=周日, 1=周一（默认）
 */
export function endOfWeek(date: Date, startDay: number = 1): Date {
  const d = startOfWeek(date, startDay)
  d.setDate(d.getDate() + 6)
  d.setHours(23, 59, 59, 999)
  return d
}

/** 某月天数 */
export function daysInMonth(year: number, month: number): number {
  return new Date(year, month + 1, 0).getDate()
}

/** 比较：a 是否在 b 之前 */
export function isBefore(a: Date, b: Date): boolean {
  return a.getTime() < b.getTime()
}

/** 比较：a 是否在 b 之后 */
export function isAfter(a: Date, b: Date): boolean {
  return a.getTime() > b.getTime()
}

/** 比较：a 与 b 是否相同（可指定精度） */
export function isSame(a: Date, b: Date, unit?: UnitType): boolean {
  return new Time(a).isSame(b, unit)
}

/** 判断 target 是否在 a 和 b 之间 */
export function isBetween(target: Date, a: Date, b: Date): boolean {
  const t = target.getTime()
  const tA = a.getTime()
  const tB = b.getTime()
  return t >= Math.min(tA, tB) && t <= Math.max(tA, tB)
}

/** 两日期差值（按指定的单位） */
export function diff(a: Date, b: Date, unit: UnitType = 'ms'): number {
  return new Time(a).diff(b, unit)
}

/** 闰年判断 */
export function isLeapYear(year: number): boolean {
  return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0
}

// ================== Time 类 ==================

export class Time {
  private _date: Date

  constructor(...params: any[]) {
    if (params.length === 0) {
      this._date = new Date()
    } else {
      this._date = new (Date as any)(...params)
    }
  }

  /* ---------- 取值 ---------- */

  get value(): string { return formatTime(this._date, 'y-m-d') }
  toDate(): Date { return new Date(this._date) }

  year(): number { return this._date.getFullYear() }
  month(): number { return this._date.getMonth() + 1 }
  date(): number { return this._date.getDate() }
  day(): number { return this._date.getDay() }
  hour(): number { return this._date.getHours() }
  minute(): number { return this._date.getMinutes() }
  second(): number { return this._date.getSeconds() }
  millisecond(): number { return this._date.getMilliseconds() }
  unix(): number { return Math.floor(this._date.getTime() / 1000) }
  daysInMonth(): number { return daysInMonth(this._date.getFullYear(), this._date.getMonth()) }

  /* ---------- 查询 ---------- */

  isBefore(other: Time | Date | string | number): boolean {
    return this._date.getTime() < new Time(other as any).valueOf()
  }
  isAfter(other: Time | Date | string | number): boolean {
    return this._date.getTime() > new Time(other as any).valueOf()
  }
  isSame(other: Time | Date | string | number, unit?: UnitType): boolean {
    const a = this._date
    const b = new Time(other as any)._date
    if (!unit) return a.getTime() === b.getTime()
    switch (unit) {
      case 'year': return a.getFullYear() === b.getFullYear()
      case 'month': return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth()
      case 'day': return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate()
      case 'hour': return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate() && a.getHours() === b.getHours()
      case 'minute': return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate() && a.getHours() === b.getHours() && a.getMinutes() === b.getMinutes()
      case 'second': return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate() && a.getHours() === b.getHours() && a.getMinutes() === b.getMinutes() && a.getSeconds() === b.getSeconds()
      case 'week': return startOfWeek(a).getTime() === startOfWeek(b).getTime()
      case 'quarter': return a.getFullYear() === b.getFullYear() && Math.floor(a.getMonth() / 3) === Math.floor(b.getMonth() / 3)
      default: return false
    }
  }
  isBetween(a: Time | Date | string | number, b: Time | Date | string | number): boolean {
    const t = this._date.getTime()
    const tA = new Time(a as any).valueOf()
    const tB = new Time(b as any).valueOf()
    return (t >= Math.min(tA, tB) && t <= Math.max(tA, tB))
  }
  isLeapYear(): boolean {
    const y = this._date.getFullYear()
    return (y % 4 === 0 && y % 100 !== 0) || y % 400 === 0
  }
  diff(other: Time | Date | string | number, unit: UnitType = 'ms'): number {
    const diffMs = this._date.getTime() - new Time(other as any).valueOf()
    const table: Record<string, number> = {
      ms: 1,
      second: 1000,
      minute: 1000 * 60,
      hour: 1000 * 60 * 60,
      day: 1000 * 60 * 60 * 24,
      week: 1000 * 60 * 60 * 24 * 7
    }
    if (unit === 'year') return this.year() - new Time(other as any).year()
    if (unit === 'month') {
      const a = this._date, b = new Time(other as any)._date
      return (a.getFullYear() - b.getFullYear()) * 12 + (a.getMonth() - b.getMonth())
    }
    if (unit === 'quarter') return Math.floor(this.month() / 3) - Math.floor(new Time(other as any).month() / 3)
    return diffMs / (table[unit] || 1)
  }

  /* ---------- 运算（不可变，返回新实例） ---------- */

  add(n: number, unit: UnitType): Time {
    const date = new Date(this._date)
    if (unit === 'year') {
      date.setFullYear(date.getFullYear() + n)
    } else if (unit === 'month') {
      const target = date.getDate()
      date.setDate(1)
      date.setMonth(date.getMonth() + n)
      const maxDay = daysInMonth(date.getFullYear(), date.getMonth())
      date.setDate(Math.min(target, maxDay))
    } else {
      const table: Record<string, number> = {
        ms: 1,
        second: 1000,
        minute: 1000 * 60,
        hour: 1000 * 60 * 60,
        day: 1000 * 60 * 60 * 24,
        week: 1000 * 60 * 60 * 24 * 7
      }
      date.setTime(date.getTime() + (table[unit] || 0) * n)
    }
    return new Time(date)
  }

  subtract(n: number, unit: UnitType): Time {
    return this.add(-n, unit)
  }

  startOf(unit: UnitType): Time {
    const d = new Date(this._date)
    switch (unit) {
      case 'year': d.setMonth(0, 1); break
      case 'quarter': d.setMonth(Math.floor(d.getMonth() / 3) * 3, 1); break
      case 'month': d.setDate(1); break
      case 'week': {
        const day = d.getDay()
        d.setDate(d.getDate() - (day === 0 ? 6 : day - 1))
        break
      }
    }
    d.setHours(0, 0, 0, 0)
    return new Time(d)
  }

  endOf(unit: UnitType): Time {
    const d = new Date(this._date)
    switch (unit) {
      case 'year': d.setMonth(11, 31); break
      case 'quarter': d.setMonth(Math.floor(d.getMonth() / 3) * 3 + 3, 0); break
      case 'month': d.setMonth(d.getMonth() + 1, 0); break
      case 'week': {
        const day = d.getDay()
        d.setDate(d.getDate() + (day === 0 ? 0 : 7 - day))
        break
      }
    }
    d.setHours(23, 59, 59, 999)
    return new Time(d)
  }

  clone(): Time { return new Time(new Date(this._date)) }

  format(fmt?: string): string {
    const f = fmt ? normalizeFormat(fmt) : 'y-m-d h:i:s'
    return doFormat(this._date, f)
  }

  toString(): string { return `${this._date}` }
  valueOf(): number { return this._date.getTime() }
  toISOString(): string { return this._date.toISOString() }

  /** @deprecated 请使用 `startOf('month').format('YYYY-MM-DD')` */
  getFirstDayOfMonth(): string {
    const d = new Date(this._date)
    return new Time(d.getFullYear(), d.getMonth(), 1).format('y-m-d')
  }
  /** @deprecated 请使用 `endOf('month').format('YYYY-MM-DD')` */
  getLastDayOfMonth(): string {
    const d = new Date(this._date)
    return new Time(d.getFullYear(), d.getMonth() + 1, 0).format('y-m-d')
  }
}

// ================== 工厂函数 ==================

/** day.js 风格短工厂，等价于 `new Time(input)`，无参时取当前时间 */
export function dt(input?: Date | string | number): Time {
  return input == null ? new Time() : new Time(input)
}

// ================== 原有函数（向后兼容） ==================

export function humanTime(datetime: any): string {
  if (datetime == null) return ''

  const time = new Date()
  let outTime = new Date(datetime)
  if (/^[0-9]\d*$/.test(datetime)) {
    outTime = new Date(parseInt(datetime))
  }

  if (time.getFullYear() !== outTime.getFullYear()) {
    return formatTime(outTime, 'y-m-d')
  }
  if (time.getDate() !== outTime.getDate()) {
    return formatTime(outTime, 'm-d')
  }

  const hours = time.getHours() - outTime.getHours()
  if (time.getHours() !== outTime.getHours()) {
    return `${hours} 小时前`
  }

  const minutes = time.getMinutes() - outTime.getMinutes()
  if (minutes === 0) return '刚刚'
  return `${Math.abs(minutes)} 分钟前`
}

export function formatTime(time: any, cFormat?: string): string {
  if (arguments.length === 0) return null as any
  const date = toDate(time)
  const fmt = cFormat ? normalizeFormat(cFormat) : 'y-m-d h:i:s'
  return doFormat(date, fmt)
}

export const timeState = (): string => {
  const hours = new Date().getHours()
  if (hours >= 0 && hours < 6) return '凌晨好!'
  if (hours < 9) return '早上好!'
  if (hours < 12) return '上午好!'
  if (hours < 14) return '中午好!'
  if (hours < 17) return '下午好!'
  if (hours < 19) return '傍晚好!'
  return '晚上好!'
}

/** @deprecated 请使用 `startOfMonth(date)` 纯函数 */
export function getFirstDayOfMonth(v?: Date): Time {
  const date = v ? new Date(v) : new Date()
  return new Time(date.getFullYear(), date.getMonth(), 1)
}

/** @deprecated 请使用 `endOfMonth(date)` 纯函数 */
export function getLastDayOfMonth(v?: Date): Time {
  const date = v ? new Date(v) : new Date()
  return new Time(date.getFullYear(), date.getMonth() + 1, 0)
}
