export interface ChildProps {
  title: string
  default?: string
}

export type ChildEmits = {
  submit: [v: string]
}
