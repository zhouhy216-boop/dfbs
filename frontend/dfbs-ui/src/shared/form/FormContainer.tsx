/**
 * 表单容器：统一外边距与布局，供 Modal/ModalForm 内表单内容包裹使用。
 */
import React from 'react';

export interface FormContainerProps {
  children: React.ReactNode;
  className?: string;
}

/** 默认表单布局配置，与 antd Form 的 layout="vertical" 一致 */
export const DEFAULT_FORM_LAYOUT = { layout: 'vertical' as const };

export function FormContainer({ children, className }: FormContainerProps) {
  return (
    <div className={className ? `unified-form-container ${className}` : 'unified-form-container'}>
      {children}
    </div>
  );
}
