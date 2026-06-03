<template>
  <div ref="container" class="sql-editor-wrapper"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { EditorView, lineNumbers, highlightActiveLine, placeholder as placeholderExt, keymap } from '@codemirror/view'
import { sql } from '@codemirror/lang-sql'
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands'
import { bracketMatching } from '@codemirror/language'
import type { Extension } from '@codemirror/state'

const props = withDefaults(defineProps<{
  modelValue: string
  placeholder?: string
  readonly?: boolean
}>(), {
  modelValue: '',
  placeholder: '',
  readonly: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const container = ref<HTMLDivElement>()
let view: EditorView | null = null

onMounted(() => {
  if (!container.value) return

  const extensions: Extension[] = [
    lineNumbers(),
    highlightActiveLine(),
    bracketMatching(),
    sql(),
    history(),
    keymap.of([
      ...defaultKeymap,
      ...historyKeymap,
      { key: 'Tab', run: (v) => { v.dispatch(v.state.replaceSelection('  ')); return true } },
    ]),
    EditorView.updateListener.of((update) => {
      if (update.docChanged) {
        emit('update:modelValue', update.state.doc.toString())
      }
    }),
  ]

  if (props.placeholder) {
    extensions.push(placeholderExt(props.placeholder))
  }

  if (props.readonly) {
    extensions.push(EditorView.editable.of(false))
  }

  view = new EditorView({
    doc: props.modelValue,
    extensions,
    parent: container.value,
  })
})

watch(() => props.modelValue, (val) => {
  if (view && val !== view.state.doc.toString()) {
    view.dispatch({
      changes: { from: 0, to: view.state.doc.length, insert: val },
    })
  }
})

watch(() => props.readonly, (val) => {
  if (!view) return
  view.dispatch({
    effects: EditorView.editable.reconfigure(EditorView.editable.of(!val)),
  })
})

onUnmounted(() => {
  view?.destroy()
  view = null
})
</script>

<style lang="scss" scoped>
.sql-editor-wrapper {
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
}
</style>
