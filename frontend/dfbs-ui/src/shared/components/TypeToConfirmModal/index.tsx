import { useState, useEffect } from 'react';
import { Input, Modal } from 'antd'; // Required: both used in JSX; do not remove (prevents "Input is not defined" at runtime).

export interface TypeToConfirmModalProps {
  open: boolean;
  onCancel: () => void;
  title: string;
  /** Main description / warning. Can be ReactNode for lists or custom copy. */
  description: React.ReactNode;
  /** Exact text user must type to enable confirm. Default "RESET". Comparison is case-sensitive. */
  confirmText?: string;
  /** Optional transform of input before comparing to confirmText (e.g. s => s.toUpperCase() for RESET). */
  transformInput?: (value: string) => string;
  onConfirm: () => Promise<void>;
  okText?: string;
  danger?: boolean;
  loading?: boolean;
}

/**
 * Reusable modal that requires the user to type a confirmation string (e.g. RESET) before enabling the confirm button.
 * Single implementation for all type-to-confirm flows; enforces exact match with confirmText.
 */
export function TypeToConfirmModal({
  open,
  onCancel,
  title,
  description,
  confirmText = 'RESET',
  transformInput = (s) => s,
  onConfirm,
  okText = '确认',
  danger = false,
  loading = false,
}: TypeToConfirmModalProps) {
  const [value, setValue] = useState('');
  const normalized = transformInput(value);
  const canConfirm = normalized === confirmText;

  useEffect(() => {
    if (open) setValue('');
  }, [open]);

  const handleOk = async () => {
    if (!canConfirm) return;
    await onConfirm();
  };

  return (
    <Modal
      title={title}
      open={open}
      onCancel={onCancel}
      onOk={handleOk}
      okText={okText}
      okButtonProps={{ danger, disabled: !canConfirm, loading }}
      destroyOnClose
    >
      <div>{description}</div>
      <p style={{ marginTop: 8 }}>请在下方输入 <strong>{confirmText}</strong> 以确认：</p>
      <Input
        value={value}
        onChange={(e) => setValue(e.target.value)}
        placeholder={`输入 ${confirmText}`}
        style={{ marginTop: 8 }}
      />
    </Modal>
  );
}
