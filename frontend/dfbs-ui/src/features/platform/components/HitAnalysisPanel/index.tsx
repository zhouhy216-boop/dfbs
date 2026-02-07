import { useEffect, useState } from 'react';
import { Card, Spin } from 'antd';
import request from '@/shared/utils/request';

export interface DuplicateMatchItem {
  orgCodeShort?: string;
  customerName?: string;
  email?: string;
  phone?: string;
  orgFullName?: string;
  matchReason?: string;
}

export interface HitAnalysisPanelProps {
  /** Controlled mode: when provided, use these hits and do not fetch. */
  hits?: DuplicateMatchItem[];
  platform: string;
  platformLabel?: string;
  customerName?: string;
  email?: string;
  contactPhone?: string;
  orgFullName?: string;
  /** Called when fetch completes (fetch mode only). */
  onHitsLoaded?: (hits: DuplicateMatchItem[]) => void;
}

/**
 * Shows duplicate-hit analysis: summary + list of matching org cards.
 * Controlled: pass `hits` to display existing matches (e.g. in DuplicateCheckModal).
 * Fetch mode: omit `hits` and pass platform + customerName/email/contactPhone/orgFullName to fetch and show results; call `onHitsLoaded` when done.
 */
export default function HitAnalysisPanel({
  hits: controlledHits,
  platform,
  platformLabel,
  customerName = '',
  email = '',
  contactPhone = '',
  orgFullName = '',
  onHitsLoaded,
}: HitAnalysisPanelProps) {
  const [fetchedHits, setFetchedHits] = useState<DuplicateMatchItem[]>([]);
  const [loading, setLoading] = useState(!controlledHits);

  const isControlled = controlledHits !== undefined;
  const hits = isControlled ? controlledHits : fetchedHits;

  useEffect(() => {
    if (isControlled) return;
    let cancelled = false;
    setLoading(true);
    request
      .post<DuplicateMatchItem[]>('/v1/platform-account-applications/check-duplicates', {
        platform,
        customerName: customerName?.trim() ?? '',
        email: email ?? '',
        contactPhone: contactPhone ?? '',
        orgFullName: orgFullName?.trim() ?? '',
      })
      .then(({ data }) => {
        if (!cancelled) {
          setFetchedHits(data ?? []);
          onHitsLoaded?.(data ?? []);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setFetchedHits([]);
          onHitsLoaded?.([]);
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => { cancelled = true; };
  }, [isControlled, platform, customerName, email, contactPhone, orgFullName, onHitsLoaded]);

  if (loading) {
    return (
      <Card size="small" style={{ border: '1px solid #d9d9d9' }}>
        <div style={{ textAlign: 'center', padding: 24 }}>
          <Spin tip="正在检查重复..." />
        </div>
      </Card>
    );
  }

  if (hits.length === 0) {
    return (
      <Card size="small" style={{ border: '1px solid #52c41a', background: '#f6ffed' }}>
        <div style={{ color: '#52c41a', fontWeight: 600 }}>✅ 无重复信息</div>
      </Card>
    );
  }

  const displayLabel = platformLabel ?? platform;
  const conflictFields = new Set<string>();
  hits.forEach((h) => {
    const r = h.matchReason || '';
    if (r.includes('客户') || r.includes('Customer')) conflictFields.add('客户');
    if (r.includes('电话') || r.includes('Phone')) conflictFields.add('电话');
    if (r.includes('邮箱') || r.includes('Email')) conflictFields.add('邮箱');
    if (r.includes('全称') || r.includes('Full Name')) conflictFields.add('全称');
    if (r.includes('代码') || r.includes('Code') || r.includes('编码')) conflictFields.add('机构代码');
  });
  const summaryText = `提醒原因: "${displayLabel}"平台下已存在相同"${[...conflictFields].join('、')}"`;

  const redStyle = { color: '#ff4d4f', fontWeight: 600 as const };
  const normalStyle = { color: '#333' };

  const hitLineStyle = (reason: string | undefined, kind: 'customer' | 'phone' | 'email' | 'orgCode' | 'orgName') => {
    const r = reason || '';
    const hit =
      kind === 'customer' ? (r.includes('客户') || r.includes('Customer'))
      : kind === 'phone' ? (r.includes('电话') || r.includes('Phone'))
      : kind === 'email' ? (r.includes('邮箱') || r.includes('Email'))
      : kind === 'orgName' ? (r.includes('全称') || r.includes('Full Name'))
      : (r.includes('代码') || r.includes('Code') || r.includes('编码'));
    return hit ? redStyle : normalStyle;
  };

  return (
    <Card
      size="small"
      title={<span style={{ color: '#ff4d4f', fontWeight: 600 }}>⚠️ 重复提醒</span>}
      style={{ border: '1px solid #ff4d4f', background: '#fff2f0' }}
    >
      <div style={{ marginBottom: 12, color: '#333', fontWeight: 500 }}>
        {summaryText}
      </div>
      {hits.map((item, idx) => (
        <div key={idx} style={{ marginBottom: 12, padding: 8, background: '#fff', borderRadius: 4, border: '1px solid #ffccc7' }}>
          <div style={{ marginBottom: 4 }}><strong>机构代码/简称</strong>: <span style={hitLineStyle(item.matchReason, 'orgCode')}>{item.orgCodeShort ?? '—'}</span></div>
          <div style={{ marginBottom: 4 }}><strong>客户</strong>: <span style={hitLineStyle(item.matchReason, 'customer')}>{item.customerName ?? '—'}</span></div>
          <div style={{ marginBottom: 4 }}><strong>机构全称</strong>: <span style={hitLineStyle(item.matchReason, 'orgName')}>{item.orgFullName ?? '—'}</span></div>
          <div style={{ marginBottom: 4 }}><strong>邮箱</strong>: <span style={hitLineStyle(item.matchReason, 'email')}>{item.email ?? '—'}</span></div>
          <div style={{ marginBottom: 0 }}><strong>电话</strong>: <span style={hitLineStyle(item.matchReason, 'phone')}>{item.phone ?? '—'}</span></div>
        </div>
      ))}
    </Card>
  );
}
