/**
 * 表单分组：标题 + 可选说明 + 子内容。供统一表单轮子与业务表单复用。
 */
import React from 'react';

export interface FormSectionProps {
  /** 分组标题，不传则不渲染标题 */
  title?: React.ReactNode;
  /** 分组说明/填写指引，显示在标题下方 */
  help?: React.ReactNode;
  children: React.ReactNode;
  className?: string;
}

export function FormSection({ title, help, children, className }: FormSectionProps) {
  return (
    <div className={className ? `unified-form-section ${className}` : 'unified-form-section'}>
      {title != null && (
        <div className="unified-form-section-title">{title}</div>
      )}
      {help != null && (
        <div className="unified-form-section-help">{help}</div>
      )}
      <div className="unified-form-section-body">{children}</div>
    </div>
  );
}
