import { useEffect, useState } from 'react';
import { Card, Spin } from 'antd';
import request from '@/shared/utils/request';

/** Platform org status for duplicate hit (Enabled/Disabled/Overdue). */
export type PlatformOrgStatus = 'ACTIVE' | 'ARREARS' | 'DELETED';

export interface DuplicateMatchItem {
  orgCodeShort?: string;
  customerName?: string;
  email?: string;
  phone?: string;
  orgFullName?: string;
  matchReason?: string;
  /** Enriched by frontend when not from API: org id from platform-orgs. */
  orgId?: number;
  /** Enriched: ACTIVE | ARREARS | DELETED. */
  status?: PlatformOrgStatus;
  /** Enriched: false = Disabled. */
  isActive?: boolean;
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
  /** Called when hits are enriched with platform status (for footer context). */
  onHitsEnriched?: (hits: DuplicateMatchItem[]) => void;
  /** When set, hit cards are clickable and this index is highlighted. */
  selectedHitIndex?: number | null;
  /** When user clicks a hit card. */
  onSelectHit?: (index: number) => void;
}

/** Resolve platform status label for display (已启用/已禁用/已欠费). Same mapping used in Platform list/detail/edit. */
export function getPlatformStatusDisplay(hit: DuplicateMatchItem): '已启用' | '已禁用' | '已欠费' {
  const status = hit.status;
  const isActive = hit.isActive !== false;
  if (status === 'ARREARS') return '已欠费';
  if (status === 'DELETED' || !isActive) return '已禁用';
  return '已启用';
}

/** Same mapping for org row (status + isActive from API). Use in list/detail/edit so status is not always "启用". */
export function getPlatformStatusLabelForOrg(row: { status?: string | null; isActive?: boolean | null }): '已启用' | '已禁用' | '已欠费' | '已删除' {
  const status = row.status;
  const isActive = row.isActive !== false;
  if (status === 'DELETED') return '已删除';
  if (status === 'ARREARS') return '已欠费';
  if (!isActive) return '已禁用';
  return '已启用';
}

interface PlatformOrgRow {
  id: number;
  orgCodeShort?: string;
  status?: string;
  isActive?: boolean;
}

/**
 * Shows duplicate-hit analysis: summary + list of matching org cards.
 * Controlled: pass `hits` to display existing matches (e.g. in DuplicateCheckModal).
 * Fetch mode: omit `hits` and pass platform + customerName/email/contactPhone/orgFullName to fetch and show results; call `onHitsLoaded` when done.
 * Enriches hits with platform status (Enabled/Disabled/Overdue) from GET platform-orgs when not present.
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
  onHitsEnriched,
  selectedHitIndex = null,
  onSelectHit,
}: HitAnalysisPanelProps) {
  const [fetchedHits, setFetchedHits] = useState<DuplicateMatchItem[]>([]);
  const [loading, setLoading] = useState(!controlledHits);
  const [enrichedHits, setEnrichedHits] = useState<DuplicateMatchItem[]>([]);

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

  // Enrich hits with platform status from GET /v1/platform-orgs when not already present
  useEffect(() => {
    if (!platform || hits.length === 0) {
      setEnrichedHits(hits);
      return;
    }
    const needEnrich = hits.some((h) => h.status === undefined && h.orgCodeShort);
    if (!needEnrich) {
      setEnrichedHits(hits);
      onHitsEnriched?.(hits);
      return;
    }
    let cancelled = false;
    request
      .get<PlatformOrgRow[] | { content?: PlatformOrgRow[] }>('/v1/platform-orgs', { params: { platform } })
      .then(({ data }) => {
        if (cancelled) return;
        const list = Array.isArray(data) ? data : (data as { content?: PlatformOrgRow[] })?.content ?? [];
        const byCode = new Map<string, PlatformOrgRow>();
        list.forEach((row) => {
          const code = row.orgCodeShort?.trim();
          if (code) byCode.set(code, row);
        });
        const next = hits.map((h) => {
          const code = h.orgCodeShort?.trim();
          const row = code ? byCode.get(code) : undefined;
          if (!row) return h;
          return {
            ...h,
            orgId: row.id,
            status: (row.status as DuplicateMatchItem['status']) ?? 'ACTIVE',
            isActive: row.isActive !== false,
          };
        });
        setEnrichedHits(next);
        onHitsEnriched?.(next);
      })
      .catch(() => {
        setEnrichedHits(hits);
        onHitsEnriched?.(hits);
      });
    return () => { cancelled = true; };
  }, [platform, hits, onHitsEnriched]);

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

  const displayHits = enrichedHits.length > 0 ? enrichedHits : hits;
  const displayLabel = platformLabel ?? platform;
  const conflictFields = new Set<string>();
  displayHits.forEach((h) => {
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

  const statusLabel = (item: DuplicateMatchItem) => getPlatformStatusDisplay(item);
  /** Colored text for status only (not button-like). 已启用=green, 已禁用=gray, 已欠费=red. */
  const statusTextStyle = (item: DuplicateMatchItem): React.CSSProperties => {
    const s = statusLabel(item);
    if (s === '已欠费') return { color: '#cf1322', fontWeight: 500 };
    if (s === '已禁用') return { color: '#8c8c8c', fontWeight: 500 };
    return { color: '#389e0d', fontWeight: 500 };
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
      {displayHits.map((item, idx) => {
        const selected = selectedHitIndex === idx;
        const cardStyle: React.CSSProperties = {
          marginBottom: 12,
          padding: 8,
          background: selected ? '#e6f7ff' : '#fff',
          borderRadius: 4,
          border: selected ? '2px solid #1890ff' : '1px solid #ffccc7',
          cursor: onSelectHit ? 'pointer' : undefined,
        };
        return (
          <div
            key={idx}
            style={cardStyle}
            onClick={() => onSelectHit?.(idx)}
            role={onSelectHit ? 'button' : undefined}
          >
            <div style={{ marginBottom: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span><strong>机构代码/简称</strong>: <span style={hitLineStyle(item.matchReason, 'orgCode')}>{item.orgCodeShort ?? '—'}</span></span>
              <span style={{ cursor: 'default', userSelect: 'none', ...statusTextStyle(item) }}>目前状态：{statusLabel(item)}</span>
            </div>
            <div style={{ marginBottom: 4 }}><strong>客户</strong>: <span style={hitLineStyle(item.matchReason, 'customer')}>{item.customerName ?? '—'}</span></div>
            <div style={{ marginBottom: 4 }}><strong>机构全称</strong>: <span style={hitLineStyle(item.matchReason, 'orgName')}>{item.orgFullName ?? '—'}</span></div>
            <div style={{ marginBottom: 4 }}><strong>邮箱</strong>: <span style={hitLineStyle(item.matchReason, 'email')}>{item.email ?? '—'}</span></div>
            <div style={{ marginBottom: 0 }}><strong>电话</strong>: <span style={hitLineStyle(item.matchReason, 'phone')}>{item.phone ?? '—'}</span></div>
          </div>
        );
      })}
    </Card>
  );
}
