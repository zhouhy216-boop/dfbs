import { useEffect } from 'react';
import type { FormInstance } from 'antd';

/**
 * 将默认模板值写入表单（如 initialValues 的补充或草稿恢复前的占位）。
 * 当 template 变化且 formRef.current 存在时执行 setFieldsValue。
 * 不替代业务侧在 onFinish 后 clearDraft 或 submit 逻辑。
 */
export function useFormTemplate(
  formRef: React.RefObject<FormInstance | undefined | null>,
  template: Record<string, unknown> | null | undefined
) {
  useEffect(() => {
    if (!template || typeof template !== 'object' || Object.keys(template).length === 0) return;
    const form = formRef?.current;
    if (!form?.setFieldsValue) return;
    try {
      form.setFieldsValue(template as Record<string, unknown>);
    } catch {
      // ignore
    }
  }, [formRef, template]);
}
