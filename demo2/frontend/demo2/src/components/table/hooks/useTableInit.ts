import { ref, onMounted, nextTick, type ComputedRef, type Ref } from 'vue'
import type { TableConfig, TableItem } from '../index.vue'
import type { OptionItem, OptionStyle } from '../types'
import { plainOptionsCache, cacheVersion } from '../search/filterCache'

interface UseTableInitOptions {
  config: TableConfig
  tableRows: ComputedRef<Record<string, any>[]>
  tableWrapperRef: Ref<HTMLElement | undefined>
  loadOptions?: (type: string, keyword?: string) => Promise<{ label: string; value: string }[]>
  /** 触发初始查询 */
  emitQuery: () => void
}

/**
 * 表格初始化 hook
 *
 * 封装 onMounted 阶段的全部初始化逻辑：
 * 1. 首次数据查询
 * 2. 选项字典预加载：从 columns / filter / optionsMap 收集 select / remote-select 字段
 * 3. 列对齐智能推断（文本 → left，数值 → right）
 * 4. 弹性填充（表格未撑满时，最后一列 width → minWidth）
 * 5. 默认配置补全（stripe）
 */
export function useTableInit(options: UseTableInitOptions) {
  const { config, tableRows, tableWrapperRef, loadOptions, emitQuery } = options

  /** 字典翻译映射表：{ prop: { value: label } } */
  const internalLookup = ref<Record<string, Record<string, string>>>({})

  /** 选项样式映射表：{ prop: { value: OptionStyle } }，用于 tag/dot 渲染 */
  const internalStyles = ref<Record<string, Record<string, OptionStyle>>>({})

  const isFixedCol = (item: TableItem) => !!item.fixed

  onMounted(async () => {
    await nextTick()

    // 1. 初始查询
    emitQuery()

    // 2. 选项翻译预加载：三来源合并（columns → optionsMap → loadOptions）
    const lookup: Record<string, Record<string, string>> = {}
    const styles: Record<string, Record<string, OptionStyle>> = {}

    /**
     * 辅助：将 OptionItem[] 注入 lookup + styles
     */
    function ingestOptions(prop: string, items: OptionItem[]) {
      if (!lookup[prop]) lookup[prop] = {}
      if (!styles[prop]) styles[prop] = {}
      for (const item of items) {
        lookup[prop][item.value] = item.label
        if (item.style) {
          styles[prop][item.value] = item.style
        }
      }
    }

    // ① 来源1：columns 中直接声明的静态 options
    for (const col of config.columns) {
      if (!col.prop || !col.fieldType) continue
      if (col.fieldType !== 'select' && col.fieldType !== 'remote-select') continue
      if (col.options?.length) {
        const items: OptionItem[] = col.options.map(o =>
          typeof o === 'string' ? { label: o, value: o } : o,
        )
        ingestOptions(col.prop, items)
      }
    }

    // ② 来源2：optionsMap 静态配置
    if (config.optionsMap) {
      for (const [prop, items] of Object.entries(config.optionsMap)) {
        ingestOptions(prop, items)
      }
    }

    // ③ 来源3：loadOptions 异步加载（remote-select 字段，兜底）
    //     收集需要 loadOptions 的 prop → optionType 映射
    //     optionType 优先，fallback 到 prop（解决不同表同名字段选项冲突）
    const propToOptionType = new Map<string, string>()

    // 从 columns 收集 remote-select
    for (const col of config.columns) {
      if (col.prop && col.fieldType === 'remote-select') {
        propToOptionType.set(col.prop, (col as any).optionType || col.prop)
      }
    }

    // 从 search.filter 收集 select / remote-select
    for (const f of config.search?.filter ?? []) {
      if (f.fieldType === 'select' || f.fieldType === 'remote-select') {
        propToOptionType.set(f.prop, (f as any).optionType || f.prop)
      }
    }

    // 异步加载（仅对 lookup 中缺失的 prop 发起请求）
    if (loadOptions && propToOptionType.size > 0) {
      const toFetch = [...propToOptionType.entries()].filter(([prop]) => !lookup[prop])
      if (toFetch.length > 0) {
        const results = await Promise.all(
          toFetch.map(async ([prop, optionType]) => {
            try {
              const items = await loadOptions(optionType)
              return { prop, items }
            } catch {
              return { prop, items: [] as { label: string; value: string }[] }
            }
          }),
        )
        for (const { prop, items } of results) {
          ingestOptions(prop, items as OptionItem[])
          // 同步写入 FilterValue 共享缓存，确保 remote-select 初始即有 label
          if (items.length > 0) {
            plainOptionsCache[prop] = items as OptionItem[]
            cacheVersion.value++
          }
        }
      }
    }

    // ④ 受 optionsStore 影响：如果 loadOptions 存在且某些 remote-select 的 prop
    //    在 optionsMap/columns 中没有配置，则触发 loadOptions 预加载
    //    已在步骤③中处理

    internalLookup.value = lookup
    internalStyles.value = styles

    // 3. 初始化 align：未设置时，文本列默认 left，数值列默认 right
    const columns = config.columns
    columns.forEach(c => {
      if (c.align === undefined && c.prop) {
        const val = tableRows.value[0]?.[c.prop]
        c.align = typeof val === 'number' ? 'right' : 'left'
      }
    })

    // 4. 弹性填充：未撑满时，最后一列 width → minWidth
    // ★ 延迟到下一帧，避免 forced reflow 阻塞 Table 首次渲染
    const wrapper = tableWrapperRef.value
    if (!wrapper) return
    if (config.stripe == null) config.stripe = true
    requestAnimationFrame(() => {
      const columns = config.columns
      const total = columns.reduce((s, c) => s + (typeof c.width === 'number' ? c.width : 0), 0)
      if (total < (wrapper.clientWidth || 0)) {
        for (let i = columns.length - 1; i >= 0; i--) {
          const c = columns[i]
          if (c.resizable === false || isFixedCol(c) || c.minWidth != null) continue
          const w = typeof c.width === 'number' ? c.width : 0
          c.minWidth = Math.max(w, 80)
          c.width = undefined
          break
        }
      }
    })
  })

  return { internalLookup, internalStyles }
}
