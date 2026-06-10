import { onMounted, onUnmounted, watch, ref, type Ref, type ShallowRef } from 'vue'
import {
  EditorView,
  lineNumbers,
  highlightActiveLine,
  placeholder as placeholderExt,
  keymap,
} from '@codemirror/view'
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands'
import { bracketMatching, foldGutter, foldKeymap } from '@codemirror/language'
import type { Extension } from '@codemirror/state'

export interface UseCodeMirrorOptions {
  container: Readonly<ShallowRef<HTMLDivElement | undefined>>
  modelValue: Ref<string>
  readonly: Ref<boolean>
  placeholder: Ref<string>
  /** 语言相关扩展（静态，一次性传入） */
  extensions: Extension[]
  onUpdate: (value: string) => void
}

/**
 * CodeMirror 编辑器生命周期管理 composable
 * 封装 EditorView 创建/销毁、modelValue 双向同步、readonly 切换
 */
export function useCodeMirror(options: UseCodeMirrorOptions) {
  let view: EditorView | null = null
  let ignoreNextUpdate = false
  const isDirty = ref(false)

  onMounted(() => {
    if (!options.container.value) return

    const exts: Extension[] = [
      lineNumbers(),
      highlightActiveLine(),
      bracketMatching(),
      foldGutter(),
      history(),
      keymap.of([
        ...defaultKeymap,
        ...historyKeymap,
        ...foldKeymap,
        {
          key: 'Tab',
          run: (v) => {
            v.dispatch(v.state.replaceSelection('  '))
            return true
          },
        },
      ]),
      EditorView.updateListener.of((update) => {
        if (update.docChanged) {
          if (!ignoreNextUpdate) {
            isDirty.value = true
          }
          ignoreNextUpdate = false
          options.onUpdate(update.state.doc.toString())
        }
      }),
      ...options.extensions,
    ]

    if (options.placeholder.value) {
      exts.push(placeholderExt(options.placeholder.value))
    }

    if (options.readonly.value) {
      exts.push(EditorView.editable.of(false))
    }

    view = new EditorView({
      doc: options.modelValue.value,
      extensions: exts,
      parent: options.container.value,
    })
  })

  // 外部 modelValue 变化 → 同步进编辑器（程序化同步不标记 dirty）
  watch(
    () => options.modelValue.value,
    (val) => {
      if (view && val !== view.state.doc.toString()) {
        ignoreNextUpdate = true
        view.dispatch({
          changes: { from: 0, to: view.state.doc.length, insert: val },
        })
      }
    },
  )

  // readonly 动态切换
  watch(
    () => options.readonly.value,
    (val) => {
      if (!view) return
      view.dispatch({
        effects: EditorView.editable.reconfigure(EditorView.editable.of(!val)),
      })
    },
  )

  onUnmounted(() => {
    view?.destroy()
    view = null
  })

  /** 获取当前 EditorView 实例（供 format/minify 等高级操作使用） */
  const getView = (): EditorView | null => view

  /** 清除脏标记（外部同步后调用） */
  function markClean() {
    isDirty.value = false
  }

  return { getView, isDirty, markClean }
}
