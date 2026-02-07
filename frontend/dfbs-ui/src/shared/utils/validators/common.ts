/**
 * Shared validation rules (phone, email) used across modules.
 */

/** Mobile (11 digits) or landline (区号-号码). */
export const PHONE_REGEX = /^(1\d{10}|\d{3,4}-\d{7,8})$/;

/** Standard email. */
export const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const PhoneRule = {
  pattern: PHONE_REGEX,
  message: '请输入11位手机号或区号-号码格式固话',
};

export const EmailRule = {
  pattern: EMAIL_REGEX,
  message: '请输入有效邮箱',
};
