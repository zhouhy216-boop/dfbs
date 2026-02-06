import { Modal } from 'antd';
import type { ReactNode } from 'react';
import HitAnalysisPanel, { type DuplicateMatchItem } from '@/components/Business/HitAnalysisPanel';

export type { DuplicateMatchItem };

export interface DuplicateCheckModalProps {
  visible: boolean;
  matches: DuplicateMatchItem[];
  /** Platform (and optional label) for HitAnalysisPanel summary. */
  platform?: string;
  platformLabel?: string;
  currentInput?: { customerName?: string; email?: string; phone?: string; orgFullName?: string } | null;
  title?: string;
  /** Custom footer buttons; receive close callback. */
  renderFooter: (close: () => void) => ReactNode;
  onCancel: () => void;
}

/**
 * Reusable duplicate warning modal: shows HitAnalysisPanel (summary + cards) and custom footer.
 */
export default function DuplicateCheckModal({
  visible,
  matches,
  platform = '',
  platformLabel,
  currentInput,
  title = '重复提醒',
  renderFooter,
  onCancel,
}: DuplicateCheckModalProps) {
  const close = () => onCancel();

  return (
    <Modal
      title={title}
      open={visible}
      onCancel={onCancel}
      footer={renderFooter(close)}
      width={640}
      destroyOnClose
    >
      <p style={{ marginBottom: 12, color: '#333' }}>
        检测到以下信息已存在，请确认是否仍要提交或选择其他操作：
      </p>
      <HitAnalysisPanel
        hits={matches}
        platform={platform}
        platformLabel={platformLabel}
        customerName={currentInput?.customerName ?? ''}
        email={currentInput?.email ?? ''}
        contactPhone={currentInput?.phone ?? ''}
        orgFullName={currentInput?.orgFullName ?? ''}
      />
    </Modal>
  );
}
