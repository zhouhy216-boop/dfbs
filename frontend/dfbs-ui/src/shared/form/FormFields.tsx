/**
 * 通用表单项封装：电话、邮箱、文本（trim），与 SmartInput 及统一校验规则配合。
 */
import React from 'react';
import { Form } from 'antd';
import type { FormItemProps } from 'antd';
import SmartInput from '@/shared/components/SmartInput';
import { PhoneRule, EmailRule } from './formValidators';

export interface FormFieldPhoneProps extends Omit<FormItemProps, 'children'> {
  name: string;
  placeholder?: string;
}

export function FormFieldPhone({
  name,
  placeholder = '11位手机号或区号-号码',
  rules = [],
  ...rest
}: FormFieldPhoneProps) {
  return (
    <Form.Item
      name={name}
      label="联系电话"
      rules={[{ required: true, message: '此项必填' }, PhoneRule, ...(Array.isArray(rules) ? rules : [rules])]}
      {...rest}
    >
      <SmartInput name={name} noSpaces placeholder={placeholder} />
    </Form.Item>
  );
}

export interface FormFieldEmailProps extends Omit<FormItemProps, 'children'> {
  name: string;
  placeholder?: string;
}

export function FormFieldEmail({
  name,
  label = '邮箱',
  placeholder = '邮箱',
  rules = [],
  ...rest
}: FormFieldEmailProps) {
  return (
    <Form.Item
      name={name}
      label={label}
      rules={[{ required: true, message: '此项必填' }, EmailRule, ...(Array.isArray(rules) ? rules : [rules])]}
      {...rest}
    >
      <SmartInput name={name} type="email" noSpaces placeholder={placeholder} />
    </Form.Item>
  );
}

export interface FormFieldTextProps extends Omit<FormItemProps, 'children'> {
  name: string;
  label: string;
  placeholder?: string;
  trim?: boolean;
  /** 帮助/说明文案 */
  help?: React.ReactNode;
}

export function FormFieldText({
  name,
  label,
  placeholder,
  trim = true,
  help,
  rules = [],
  ...rest
}: FormFieldTextProps) {
  return (
    <Form.Item
      name={name}
      label={label}
      rules={Array.isArray(rules) ? rules : [rules]}
      extra={help}
      {...rest}
    >
      <SmartInput name={name} trim={trim} placeholder={placeholder ?? label} />
    </Form.Item>
  );
}
