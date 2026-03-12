/**
 * 只读表单视图：按字段配置展示 label + value，用于整表只读切换时的展示。
 */
import React from 'react';

export interface ReadonlyFormFieldConfig {
  key: string;
  label: string;
  /** 可选，不传则用 values[key] */
  render?: (value: unknown) => React.ReactNode;
}

export interface ReadonlyFormViewProps {
  /** 当前值（如 form.getFieldsValue()） */
  values: Record<string, unknown>;
  /** 字段列表，顺序与 label */
  fields: ReadonlyFormFieldConfig[];
  className?: string;
}

const defaultRender = (v: unknown): React.ReactNode => {
  if (v == null || v === '') return '—';
  return String(v);
};

export function ReadonlyFormView({ values, fields, className }: ReadonlyFormViewProps) {
  return (
    <div className={className ? `unified-form-readonly-view ${className}` : 'unified-form-readonly-view'}>
      {fields.map(({ key, label, render = defaultRender }) => (
        <div key={key} className="unified-form-readonly-row">
          <strong>{label}</strong>: {render(values[key])}
        </div>
      ))}
    </div>
  );
}
