import { Form, Input } from 'antd';
import type { InputProps } from 'antd';

export interface SmartInputProps extends Omit<InputProps, 'onChange' | 'onBlur'> {
  /** Trim value on blur (default true). Ignored if noSpaces is true. */
  trim?: boolean;
  /** Remove ALL whitespace (spaces, tabs, newlines \\n \\r) on blur. */
  noSpaces?: boolean;
  /** Keep only a-zA-Z (strips numbers, symbols, IME separators). Applied after noSpaces. */
  onlyLetters?: boolean;
  /** Convert input to uppercase on blur (avoids IME issues; do not use in onChange). */
  uppercase?: boolean;
  /** Form field name when used inside Form.Item (for setFieldValue on blur). */
  name?: string;
  onChange?: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onBlur?: (e: React.FocusEvent<HTMLInputElement>) => void;
}

/**
 * Input with cleaning on blur. Order: noSpaces (strip \\s) -> onlyLetters (strip non-a-zA-Z) -> uppercase.
 * Uppercase is applied on blur only to avoid breaking IME (e.g. Chinese) input.
 */
export default function SmartInput({
  trim = true,
  noSpaces = false,
  onlyLetters = false,
  uppercase = false,
  name,
  onChange,
  onBlur,
  value,
  ...rest
}: SmartInputProps) {
  const form = Form.useFormInstance?.() ?? null;

  const handleBlur = (e: React.FocusEvent<HTMLInputElement>) => {
    let v = e.target.value;
    if (v == null) {
      onBlur?.(e);
      return;
    }
    let cleaned: string;
    if (noSpaces) {
      cleaned = v.replace(/\s/g, ''); // spaces, tabs, \n, \r
    } else if (trim) {
      cleaned = v.trim();
    } else {
      cleaned = v;
    }
    if (onlyLetters && cleaned) {
      cleaned = cleaned.replace(/[^a-zA-Z]/g, '');
    }
    if (uppercase && cleaned) {
      cleaned = cleaned.toUpperCase();
    }
    if (cleaned !== v) {
      e.target.value = cleaned;
      if (name && form) {
        form.setFieldValue(name, cleaned);
        form.validateFields([name]).catch(() => {});
      }
      onChange?.(e);
    }
    onBlur?.(e);
  };

  return (
    <Input
      {...rest}
      value={value}
      onChange={onChange}
      onBlur={handleBlur}
    />
  );
}
