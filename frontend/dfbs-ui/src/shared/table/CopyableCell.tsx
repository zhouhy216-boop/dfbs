/**
 * Phase-1 统一表格：可复制单元格，点击复制到剪贴板并显式提示（toast）
 */
import { message, Typography } from 'antd';

export interface CopyableCellProps {
  /** 要复制的文本，空则显示占位 */
  value?: string | number | null;
  /** 空值占位符 */
  emptyPlaceholder?: string;
}

const DEFAULT_EMPTY = '—';
const COPY_SUCCESS_MSG = '已复制';

export function CopyableCell({
  value,
  emptyPlaceholder = DEFAULT_EMPTY,
}: CopyableCellProps) {
  const text = value != null && String(value).trim() !== '' ? String(value) : emptyPlaceholder;
  const canCopy = value != null && String(value).trim() !== '';

  if (!canCopy) {
    return <span>{text}</span>;
  }
  return (
    <Typography.Text
      copyable={{
        text: String(value),
        tooltips: ['复制', COPY_SUCCESS_MSG],
        onCopy: () => message.success(COPY_SUCCESS_MSG),
      }}
    >
      {text}
    </Typography.Text>
  );
}
