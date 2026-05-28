/**
 * 全局 el-select 修复：选中选项后自动 blur 内部 input
 *
 * 问题：el-select（尤其是 filterable/remote 的）在点击选项选中后，
 * 下拉面板关闭但内部 <input> 仍然持有焦点，光标会继续闪烁在输入框内。
 *
 * 原理：在 capture 阶段拦截点击事件，检测到点击
 * el-select-dropdown__item 后，在下一帧 blur 对应 select 的内部输入框。
 */
export function installSelectBlurFix() {
  const handleClick = (e: MouseEvent) => {
    const target = e.target as HTMLElement | null
    if (!target) return

    const option = target.closest('.el-select-dropdown__item') as HTMLElement | null
    if (!option) return

    // 使用 requestAnimationFrame 确保 Element Plus 先完成 v-model 更新
    requestAnimationFrame(() => {
      // 方案 1：从选项向上遍历找 .el-select（适用于 teleported="false" 的场景）
      const dropdown = option.closest('.el-select-dropdown')
      if (dropdown) {
        const select = dropdown.closest('.el-select')
        if (select) {
          const input = select.querySelector('input')
          input?.blur()
          return
        }
      }

      // 方案 2：兜底匹配 → 查找所有 aria-expanded="true" 的 el-select
      // （适用于 teleported 到 body、dropdown 与 select DOM 断开的场景）
      const expandedSelects = document.querySelectorAll('.el-select[aria-expanded="true"]')
      expandedSelects.forEach((el) => {
        const input = el.querySelector('input')
        input?.blur()
      })
    })
  }

  document.addEventListener('click', handleClick, true)
}
