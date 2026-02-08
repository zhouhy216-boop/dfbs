import { useState, useCallback, useEffect } from 'react';
import { Modal } from 'antd';
import type { ReactNode } from 'react';
import HitAnalysisPanel, { type DuplicateMatchItem, getPlatformStatusDisplay } from '@/features/platform/components/HitAnalysisPanel';

export type { DuplicateMatchItem };

/** Stage of the application flow for stage-aware button visibility. */
export type DuplicateModalStage = 'sales' | 'enterprise_confirm' | 'enterprise_direct' | 'service';

export interface DuplicateCheckModalContext {
  /** Currently selected hit (for Apply Reuse visibility). */
  selectedHit: DuplicateMatchItem | null;
  /** Whether selected hit is 已禁用 or 已欠费 (Apply Reuse shown only then). */
  selectedHitCanReuse: boolean;
  /** House-rule violation: Confirm Create hidden, placeholders may show. */
  violatesHouseRules: boolean;
}

export interface DuplicateCheckModalProps {
  visible: boolean;
  matches: DuplicateMatchItem[];
  /** Platform (and optional label) for HitAnalysisPanel summary. */
  platform?: string;
  platformLabel?: string;
  currentInput?: { customerName?: string; email?: string; phone?: string; orgFullName?: string } | null;
  title?: string;
  /** Stage for button visibility (Sales / Enterprise confirm / Service). */
  stage?: DuplicateModalStage;
  /** When true, Confirm Create is hidden and house-rule placeholders may show. */
  violatesHouseRules?: boolean;
  /** Custom footer; receives close and context (selectedHit, selectedHitCanReuse, violatesHouseRules). */
  renderFooter: (close: () => void, context: DuplicateCheckModalContext) => ReactNode;
  /** Cancel = 返回编辑: close modal only, return to form. */
  onCancel: () => void;
}

/** True when the hit is 已禁用 or 已欠费 (Apply Reuse allowed). */
export function canApplyReuseForHit(hit: DuplicateMatchItem): boolean {
  const label = getPlatformStatusDisplay(hit);
  return label === '已禁用' || label === '已欠费';
}

/**
 * Reusable duplicate warning modal: shows HitAnalysisPanel (summary + cards with platform status) and stage-aware footer.
 * Default-selects first hit when opened so 申请复用 visibility is deterministic.
 */
export default function DuplicateCheckModal({
  visible,
  matches,
  platform = '',
  platformLabel,
  currentInput,
  title = '重复提醒',
  violatesHouseRules = false,
  renderFooter,
  onCancel,
}: DuplicateCheckModalProps) {
  const [selectedHitIndex, setSelectedHitIndex] = useState<number | null>(null);
  const [enrichedHits, setEnrichedHits] = useState<DuplicateMatchItem[]>([]);

  const close = useCallback(() => onCancel(), [onCancel]);

  /** Default: auto-select first hit when modal opens so button visibility is deterministic. */
  useEffect(() => {
    if (visible && matches.length > 0) {
      setSelectedHitIndex(0);
    }
  }, [visible, matches.length]);

  const displayHits = enrichedHits.length > 0 ? enrichedHits : matches;
  /** Keep selection in range when enriched list updates. */
  useEffect(() => {
    if (selectedHitIndex != null && selectedHitIndex >= displayHits.length && displayHits.length > 0) {
      setSelectedHitIndex(0);
    }
  }, [displayHits.length, selectedHitIndex]);
  const selectedHit = selectedHitIndex != null && selectedHitIndex >= 0 && selectedHitIndex < displayHits.length
    ? displayHits[selectedHitIndex]
    : null;
  const selectedHitCanReuse = selectedHit ? canApplyReuseForHit(selectedHit) : false;

  const context: DuplicateCheckModalContext = {
    selectedHit,
    selectedHitCanReuse,
    violatesHouseRules,
  };

  return (
    <Modal
      title={title}
      open={visible}
      onCancel={onCancel}
      footer={renderFooter(close, context)}
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
        selectedHitIndex={selectedHitIndex}
        onSelectHit={(idx) => setSelectedHitIndex(idx)}
        onHitsEnriched={setEnrichedHits}
      />
    </Modal>
  );
}
