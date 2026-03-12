/**
 * 最小可复用表单轮子：容器、分组、通用字段、校验、草稿、只读切换、模板、说明文案。
 * 从 Platform Application 表单现状抽取，供后续 Contract Review V1 等业务表单复用。
 */
export { FormSection } from './FormSection';
export type { FormSectionProps } from './FormSection';
export { FormContainer, DEFAULT_FORM_LAYOUT } from './FormContainer';
export type { FormContainerProps } from './FormContainer';
export { DraftAlert } from './DraftAlert';
export type { DraftAlertProps } from './DraftAlert';
export { useFormReadonly } from './useFormReadonly';
export { ReadonlyFormView } from './ReadonlyFormView';
export type { ReadonlyFormViewProps, ReadonlyFormFieldConfig } from './ReadonlyFormView';
export { FormFieldPhone, FormFieldEmail, FormFieldText } from './FormFields';
export type { FormFieldPhoneProps, FormFieldEmailProps, FormFieldTextProps } from './FormFields';
export { useFormTemplate } from './useFormTemplate';
export * from './formValidators';
import './formWheelStyles.css';
