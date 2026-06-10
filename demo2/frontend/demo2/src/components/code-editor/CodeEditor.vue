<template>
  <div ref="container" :class="['code-editor-wrapper', `code-editor--${language}`]"></div>
</template>

<script setup lang="ts">
import { computed, ref, toRef } from 'vue'
import { sql } from '@codemirror/lang-sql'
import { json } from '@codemirror/lang-json'
import { linter } from '@codemirror/lint'
import type { Extension } from '@codemirror/state'
import { useCodeMirror } from './hooks/useCodeMirror'
import type { EditorLanguage } from './types'

// ==================== Props / Emits ====================

const props = withDefaults(
  defineProps<{
    modelValue: string
    language: EditorLanguage
    placeholder?: string
    readonly?: boolean
  }>(),
  {
    modelValue: '',
    language: 'sql',
    placeholder: '',
    readonly: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

// ==================== 语言扩展 ====================

/** JSON 实时校验 lint 源 */
function jsonLintSource() {
  return linter((view) => {
    const diagnostics: import('@codemirror/lint').Diagnostic[] = []
    try {
      JSON.parse(view.state.doc.toString())
    } catch (e: any) {
      const match = e.message.match(/position (\d+)/)
      const pos = match ? parseInt(match[1]) : 0
      diagnostics.push({
        from: pos,
        to: Math.min(pos + 1, view.state.doc.length),
        severity: 'error',
        message: e.message,
      })
    }
    return diagnostics
  })
}

// ==================== Composable ====================

const container = ref<HTMLDivElement>()

const languageExtensions: Extension[] = (() => {
  if (props.language === 'json') {
    return [json(), jsonLintSource()]
  }
  return [sql()]
})()

const { getView, isDirty, markClean } = useCodeMirror({
  container,
  modelValue: toRef(props, 'modelValue'),
  readonly: toRef(props, 'readonly'),
  placeholder: toRef(props, 'placeholder'),
  extensions: languageExtensions,
  onUpdate: (val) => emit('update:modelValue', val),
})

// ==================== 暴露的公共方法 ====================

/** 格式化 JSON（美化） */
function format() {
  const view = getView()
  if (!view) return
  try {
    const obj = JSON.parse(view.state.doc.toString())
    const formatted = JSON.stringify(obj, null, 2)
    view.dispatch({
      changes: { from: 0, to: view.state.doc.length, insert: formatted },
    })
  } catch {
    // 非法 JSON 时静默忽略
  }
}

/** 压缩 JSON（移除空格换行） */
function minify() {
  const view = getView()
  if (!view) return
  try {
    const obj = JSON.parse(view.state.doc.toString())
    const minified = JSON.stringify(obj)
    view.dispatch({
      changes: { from: 0, to: view.state.doc.length, insert: minified },
    })
  } catch {
    // 非法 JSON 时静默忽略
  }
}

defineExpose({ format, minify, isDirty, markClean })
</script>

<style lang="scss" scoped>
.code-editor-wrapper {
  height: 100%;
  overflow: auto;

  :deep(.cm-editor) {
    height: 100%;
    font-size: 13px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;

    .cm-scroller {
      overflow: auto;
      line-height: 1.7;
    }
  }

  // ========== JSON 风格覆盖 ==========
  &.code-editor--json {
    :deep(.cm-editor) {
      font-size: 12px;

      .cm-scroller {
        line-height: 1.6;
      }

      .cm-gutters {
        border-right: 1px solid #e8e8e8;
        background: #fafafa;
        color: #bbb;
        font-size: 11px;
      }

      .cm-foldGutter {
        width: 14px;

        .cm-gutterElement {
          color: #999;
          cursor: pointer;
          transition: color 0.15s;

          &:hover {
            color: #333;
          }
        }
      }
    }
  }
}
</style>
