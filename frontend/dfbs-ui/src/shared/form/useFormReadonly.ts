import { useCallback, useState } from 'react';

export interface UseFormReadonlyResult {
  readonly: boolean;
  setReadonly: (v: boolean) => void;
  toggleReadonly: () => void;
}

/**
 * 整表只读/可编辑切换状态。只提供状态与切换，具体只读展示由 ReadonlyFormView 或业务自行渲染。
 */
export function useFormReadonly(initial = false): UseFormReadonlyResult {
  const [readonly, setReadonly] = useState(initial);
  const toggleReadonly = useCallback(() => setReadonly((v) => !v), []);
  return { readonly, setReadonly, toggleReadonly };
}
