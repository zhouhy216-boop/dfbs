/**
 * 草稿提示条：展示“有未提交草稿”并提供恢复/清除操作。与 useDraftForm 配合使用。
 */
import React from 'react';
import { Alert, Button } from 'antd';

export interface DraftAlertProps {
  /** 是否存在草稿 */
  hasDraft: boolean;
  /** 点击「恢复草稿」时调用 */
  onRestore: () => void;
  /** 点击「清除草稿」时调用 */
  onClear: () => void;
  /** 自定义提示文案，默认「您有未提交的草稿。」 */
  message?: React.ReactNode;
  className?: string;
}

const DEFAULT_MESSAGE = '您有未提交的草稿。';

export function DraftAlert({
  hasDraft,
  onRestore,
  onClear,
  message = DEFAULT_MESSAGE,
  className,
}: DraftAlertProps) {
  if (!hasDraft) return null;
  return (
    <Alert
      type="info"
      showIcon
      className={className}
      style={{ marginBottom: 16 }}
      message={
        <>
          {message}
          <Button type="link" size="small" onClick={onRestore}>
            恢复草稿
          </Button>
          <Button type="link" size="small" onClick={onClear}>
            清除草稿
          </Button>
        </>
      }
    />
  );
}
