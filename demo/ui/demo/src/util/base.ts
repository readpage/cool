import { DirectiveBinding } from "vue"

/**
 * 移动元素位置
 * @param array 数组
 * @param oldIndex 旧索引
 * @param newIndex 新索引
 * @returns
 */
export const moveElement = (array: any[], oldIndex: number, newIndex: number) => {
  // 获取元素
  let element = array[oldIndex]
  // 从旧索引位置删除元素
  array.splice(oldIndex, 1)
  // 插入到新索引位置
  array.splice(newIndex, 0, element)
  return array
}

/**
 * 根据对象数组中某一属性的值删除
 * @returns
 */
export const removeByValue = (arr: any[], attr: string, value: any) => {
  let index = null
  for (let i = 0; i < arr.length; i++) {
    if (arr[i][attr] == value) {
      index = i
      break
    }
  }
  if (index != null) {
    arr.splice(index, 1)
  }
}

/**
 * 创建自然数组
 * @param start
 * @param end
 * @returns
 */
export function createNaturalNumArr(start: number, end: number) {
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
}

/**
 * 返回像素
 * @param val
 * @returns
 */
export const toPx = (val: any) => {
  if (isNaN(val) || val == null) {
    return val
  } else {
    return val + 'px'
  }
}

export function mergeObject(target: any, source: any) {
  for (const key in source) {
    if (source.hasOwnProperty(key) && !target.hasOwnProperty(key)) {
      target[key] = source[key];
    }
  }
  return target;
}

export const vClickOutside = {
  beforeMount(el: any, binding: DirectiveBinding) {
    function documentClick(e: MouseEvent) {
      let el2 = document.querySelector(binding.arg as any)
      if (!(el.contains(e.target as Node) || el2 && el2.contains(e.target as Node))) {
        binding.value(e);
      }
    }
    document.addEventListener('click', documentClick);
    el._clickOutside = documentClick;
  },
  unmounted(el: any) {
    document.removeEventListener('click', el._clickOutside);
    delete el._clickOutside;
  }
}
