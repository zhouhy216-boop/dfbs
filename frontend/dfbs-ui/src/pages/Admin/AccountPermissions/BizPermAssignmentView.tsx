/**
 * 按账号直接分配：以业务模块目录为清单勾选，保存为 roleTemplateId=null、addKeys=勾选、removeKeys=[]。
 * 支持每操作点数据范围：全量 / 仅已处理（仅当目录 op.handledOnly 时可选）。全中文。
 */
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Button, Checkbox, Input, message, Modal, Segmented, Space, Tag } from 'antd';
import { getCatalogRead, getAccountScopes, setAccountScopes } from './bizpermCatalogService';
import { saveAccountOverride, type AccountOverrideResponse } from './acctPermService';
import type { CatalogNode, CatalogResponse } from './bizpermCatalogService';

function getErrorMessage(err: { response?: { data?: { message?: string }; status?: number } }): string {
  if (err.response?.status === 403) return '无权限';
  return err.response?.data?.message ?? '操作失败，请重试';
}

interface FlatOp {
  permissionKey: string;
  cnName: string | null;
  handledOnly: boolean;
  nodeCnName: string;
  isUnclassified: boolean;
}

function flattenCatalog(catalog: CatalogResponse): FlatOp[] {
  const out: FlatOp[] = [];
  function walk(nodes: CatalogNode[], parentLabel: string) {
    for (const n of nodes) {
      const label = n.cnName || parentLabel;
      for (const op of n.ops ?? []) {
        out.push({
          permissionKey: op.permissionKey,
          cnName: op.cnName,
          handledOnly: op.handledOnly,
          nodeCnName: label,
          isUnclassified: false,
        });
      }
      if (n.children?.length) walk(n.children, label);
    }
  }
  walk(catalog.tree, '');
  for (const op of catalog.unclassified ?? []) {
    out.push({
      permissionKey: op.permissionKey,
      cnName: op.cnName,
      handledOnly: op.handledOnly,
      nodeCnName: '',
      isUnclassified: true,
    });
  }
  return out;
}

const SCOPE_ALL = 'ALL';
const SCOPE_HANDLED_ONLY = 'HANDLED_ONLY';

function renderNodeOps(
  node: CatalogNode,
  flatOps: FlatOp[],
  filteredOps: FlatOp[],
  draftCheckedKeys: Set<string>,
  toggleKey: (k: string) => void,
  draftScopes: Record<string, string>,
  setDraftScope: (key: string, scope: string) => void,
  marginLeft = 0,
): React.ReactNode {
  return (
    <div key={node.id} style={{ marginLeft, marginBottom: 8 }}>
      <p style={{ fontSize: 12, fontWeight: 500, color: '#333', marginBottom: 4 }}>{node.cnName}</p>
      {(node.ops ?? []).map((op) => {
        const inFiltered = filteredOps.some((f) => f.permissionKey === op.permissionKey && !f.isUnclassified);
        if (!inFiltered) return null;
        const checked = draftCheckedKeys.has(op.permissionKey);
        const scope = draftScopes[op.permissionKey] || SCOPE_ALL;
        const showScopeSwitch = op.handledOnly && checked;
        return (
          <div key={op.permissionKey} style={{ display: 'flex', alignItems: 'center', gap: 8, marginLeft: 12, marginBottom: 4, flexWrap: 'wrap' }}>
            <Checkbox checked={checked} onChange={() => toggleKey(op.permissionKey)} />
            <span style={{ fontSize: 12 }}>{op.cnName || op.permissionKey}</span>
            {showScopeSwitch ? (
              <Segmented
                size="small"
                options={[
                  { label: '全量', value: SCOPE_ALL },
                  { label: '仅已处理', value: SCOPE_HANDLED_ONLY },
                ]}
                value={scope}
                onChange={(v) => typeof v === 'string' && setDraftScope(op.permissionKey, v)}
              />
            ) : op.handledOnly ? (
              <Tag color="orange">仅已处理</Tag>
            ) : (
              <span style={{ fontSize: 11, color: '#999' }}>仅全量（不支持仅已处理）</span>
            )}
          </div>
        );
      })}
      {(node.children ?? []).map((child) =>
        renderNodeOps(child, flatOps, filteredOps, draftCheckedKeys, toggleKey, draftScopes, setDraftScope, marginLeft + 12),
      )}
    </div>
  );
}

interface BizPermAssignmentViewProps {
  userId: number;
  savedOverride: AccountOverrideResponse | null;
  onSaveSuccess: () => void;
}

export default function BizPermAssignmentView({ userId, savedOverride, onSaveSuccess }: BizPermAssignmentViewProps) {
  const [catalog, setCatalog] = useState<CatalogResponse | null>(null);
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [draftCheckedKeys, setDraftCheckedKeys] = useState<Set<string>>(new Set());
  const [lastLoadedCheckedKeys, setLastLoadedCheckedKeys] = useState<Set<string>>(new Set());
  const [draftScopes, setDraftScopes] = useState<Record<string, string>>({});
  const [lastLoadedScopes, setLastLoadedScopes] = useState<Record<string, string>>({});
  const [searchQuery, setSearchQuery] = useState('');
  const [filterOnlyChecked, setFilterOnlyChecked] = useState(false);
  const [filterOnlyUnclassified, setFilterOnlyUnclassified] = useState(false);
  const [filterOnlyHandledOnly, setFilterOnlyHandledOnly] = useState(false);
  const [filterOnlyScopeHandledOnly, setFilterOnlyScopeHandledOnly] = useState(false);
  const [saveLoading, setSaveLoading] = useState(false);
  const [confirmSaveOpen, setConfirmSaveOpen] = useState(false);

  const loadCatalog = useCallback(() => {
    setCatalogLoading(true);
    getCatalogRead()
      .then(setCatalog)
      .catch((err) => {
        message.error(getErrorMessage(err));
        setCatalog({ tree: [], unclassified: [] });
      })
      .finally(() => setCatalogLoading(false));
  }, []);

  useEffect(() => {
    loadCatalog();
  }, [loadCatalog]);

  useEffect(() => {
    if (savedOverride == null) {
      setDraftCheckedKeys(new Set());
      setLastLoadedCheckedKeys(new Set());
      setDraftScopes({});
      setLastLoadedScopes({});
      return;
    }
    const add = new Set(savedOverride.addKeys ?? []);
    setDraftCheckedKeys(add);
    setLastLoadedCheckedKeys(add);
  }, [savedOverride?.userId, savedOverride?.addKeys]);

  useEffect(() => {
    if (!userId) return;
    getAccountScopes(userId)
      .then((res) => {
        const scopes = res.scopes ?? {};
        setLastLoadedScopes(scopes);
        setDraftScopes(scopes);
      })
      .catch(() => {
        setLastLoadedScopes({});
        setDraftScopes({});
      });
  }, [userId]);

  const hasTemplateWarning = savedOverride != null && savedOverride.roleTemplateId != null;
  const dirty = useMemo(() => {
    if (lastLoadedCheckedKeys.size !== draftCheckedKeys.size) return true;
    for (const k of draftCheckedKeys) if (!lastLoadedCheckedKeys.has(k)) return true;
    for (const k of lastLoadedCheckedKeys) if (!draftCheckedKeys.has(k)) return true;
    for (const k of Object.keys(lastLoadedScopes)) {
      if (lastLoadedScopes[k] === SCOPE_HANDLED_ONLY && draftScopes[k] !== SCOPE_HANDLED_ONLY) return true;
    }
    for (const k of Object.keys(draftScopes)) {
      if (draftScopes[k] === SCOPE_HANDLED_ONLY && lastLoadedScopes[k] !== SCOPE_HANDLED_ONLY) return true;
    }
    return false;
  }, [draftCheckedKeys, lastLoadedCheckedKeys, draftScopes, lastLoadedScopes]);

  const flatOps = useMemo(() => (catalog ? flattenCatalog(catalog) : []), [catalog]);
  const filteredOps = useMemo(() => {
    let list = flatOps;
    const q = searchQuery.trim().toLowerCase();
    if (q) {
      list = list.filter(
        (op) =>
          (op.nodeCnName && op.nodeCnName.toLowerCase().includes(q)) ||
          (op.cnName && op.cnName.toLowerCase().includes(q)) ||
          op.permissionKey.toLowerCase().includes(q),
      );
    }
    if (filterOnlyChecked) list = list.filter((op) => draftCheckedKeys.has(op.permissionKey));
    if (filterOnlyUnclassified) list = list.filter((op) => op.isUnclassified);
    if (filterOnlyHandledOnly) list = list.filter((op) => op.handledOnly);
    if (filterOnlyScopeHandledOnly) list = list.filter((op) => draftCheckedKeys.has(op.permissionKey) && (draftScopes[op.permissionKey] === SCOPE_HANDLED_ONLY));
    return list;
  }, [flatOps, searchQuery, filterOnlyChecked, filterOnlyUnclassified, filterOnlyHandledOnly, filterOnlyScopeHandledOnly, draftCheckedKeys, draftScopes]);

  const handledOnlyScopeCount = useMemo(() => {
    return flatOps.filter((op) => op.handledOnly && draftCheckedKeys.has(op.permissionKey) && (draftScopes[op.permissionKey] === SCOPE_HANDLED_ONLY)).length;
  }, [flatOps, draftCheckedKeys, draftScopes]);

  const toggleKey = (key: string) => {
    setDraftCheckedKeys((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  };

  const setDraftScope = useCallback((key: string, scope: string) => {
    setDraftScopes((prev) => ({ ...prev, [key]: scope }));
  }, []);

  const handleSave = () => {
    if (hasTemplateWarning) {
      setConfirmSaveOpen(true);
      return;
    }
    doSave();
  };

  const doSave = () => {
    setConfirmSaveOpen(false);
    setSaveLoading(true);
    const overridePayload = { roleTemplateId: null, addKeys: Array.from(draftCheckedKeys), removeKeys: [] as string[] };
    const supportsHandledOnly = new Set(flatOps.filter((o) => o.handledOnly).map((o) => o.permissionKey));
    const scopeUpdates: Array<{ permissionKey: string; scope: string }> = [];
    for (const key of draftCheckedKeys) {
      if (supportsHandledOnly.has(key) && draftScopes[key] === SCOPE_HANDLED_ONLY) {
        scopeUpdates.push({ permissionKey: key, scope: SCOPE_HANDLED_ONLY });
      }
    }
    for (const key of Object.keys(lastLoadedScopes)) {
      if (lastLoadedScopes[key] === SCOPE_HANDLED_ONLY && (!draftCheckedKeys.has(key) || draftScopes[key] !== SCOPE_HANDLED_ONLY)) {
        scopeUpdates.push({ permissionKey: key, scope: SCOPE_ALL });
      }
    }
    saveAccountOverride(userId, overridePayload)
      .then((res) => {
        setLastLoadedCheckedKeys(new Set(res.addKeys ?? []));
        if (scopeUpdates.length > 0) {
          return setAccountScopes(userId, scopeUpdates).then((scopeRes) => {
            const scopes = scopeRes.scopes ?? {};
            setLastLoadedScopes(scopes);
            setDraftScopes(scopes);
          });
        }
      })
      .then(() => {
        onSaveSuccess();
        message.success('已保存（权限与数据范围）');
      })
      .catch((err) => message.error(getErrorMessage(err)))
      .finally(() => setSaveLoading(false));
  };

  const handleRestore = () => {
    setDraftCheckedKeys(new Set(lastLoadedCheckedKeys));
    setDraftScopes({ ...lastLoadedScopes });
    message.info('已还原为上次加载状态');
  };

  if (catalogLoading && !catalog) {
    return <span style={{ color: '#666' }}>加载目录中…</span>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
      {hasTemplateWarning && (
        <Alert
          type="warning"
          message="当前账号使用角色模板 + 覆盖；本视图保存将切换为「按账号直接分配」并清除模板依赖。"
          showIcon
        />
      )}

      <Input.Search
        placeholder="搜索模块名、操作名或权限键"
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        allowClear
        style={{ maxWidth: 320 }}
      />

      <Space wrap>
        <Checkbox checked={filterOnlyChecked} onChange={(e) => setFilterOnlyChecked(e.target.checked)}>
          只看已勾选
        </Checkbox>
        <Checkbox checked={filterOnlyUnclassified} onChange={(e) => setFilterOnlyUnclassified(e.target.checked)}>
          只看未归类
        </Checkbox>
        <Checkbox checked={filterOnlyHandledOnly} onChange={(e) => setFilterOnlyHandledOnly(e.target.checked)}>
          只看仅已处理
        </Checkbox>
        <Checkbox checked={filterOnlyScopeHandledOnly} onChange={(e) => setFilterOnlyScopeHandledOnly(e.target.checked)}>
          只看仅已处理范围
        </Checkbox>
      </Space>
      <p style={{ fontSize: 12, color: '#666' }}>
        仅已处理范围：{handledOnlyScopeCount} 项（仅对支持的操作点生效）
      </p>

      <Space>
        <Button type="primary" onClick={handleSave} loading={saveLoading} disabled={!dirty}>
          保存
        </Button>
        <Button onClick={handleRestore} disabled={!dirty}>
          还原
        </Button>
      </Space>

      <div style={{ maxHeight: 360, overflow: 'auto', border: '1px solid #f0f0f0', borderRadius: 4, padding: 8, background: '#fff' }}>
        {catalog?.tree && catalog.tree.length > 0 && (
          <div style={{ marginBottom: 12 }}>
            <p style={{ fontSize: 12, fontWeight: 600, marginBottom: 8 }}>目录节点下操作点</p>
            {catalog.tree.map((node) =>
              renderNodeOps(node, flatOps, filteredOps, draftCheckedKeys, toggleKey, draftScopes, setDraftScope),
            )}
          </div>
        )}
        {catalog?.unclassified && catalog.unclassified.length > 0 && (
          <div>
            <p style={{ fontSize: 12, fontWeight: 600, marginBottom: 8 }}>未归类 / 待认领</p>
            {catalog.unclassified.map((op) => {
              const inFiltered = filteredOps.some((f) => f.permissionKey === op.permissionKey && f.isUnclassified);
              if (!inFiltered) return null;
              const checked = draftCheckedKeys.has(op.permissionKey);
              const scope = draftScopes[op.permissionKey] || SCOPE_ALL;
              const showScopeSwitch = op.handledOnly && checked;
              return (
                <div key={op.id ?? op.permissionKey} style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4, flexWrap: 'wrap' }}>
                  <Checkbox checked={checked} onChange={() => toggleKey(op.permissionKey)} />
                  <span style={{ fontSize: 12 }}>{op.cnName || op.permissionKey}</span>
                  {showScopeSwitch ? (
                    <Segmented
                      size="small"
                      options={[
                        { label: '全量', value: SCOPE_ALL },
                        { label: '仅已处理', value: SCOPE_HANDLED_ONLY },
                      ]}
                      value={scope}
                      onChange={(v) => typeof v === 'string' && setDraftScope(op.permissionKey, v)}
                    />
                  ) : op.handledOnly ? (
                    <Tag color="orange">仅已处理</Tag>
                  ) : (
                    <span style={{ fontSize: 11, color: '#999' }}>仅全量（不支持仅已处理）</span>
                  )}
                </div>
              );
            })}
          </div>
        )}
        {(!catalog?.tree?.length && !catalog?.unclassified?.length) && (
          <p style={{ fontSize: 12, color: '#999' }}>暂无目录数据</p>
        )}
      </div>

      <Modal
        title="确认保存"
        open={confirmSaveOpen}
        onCancel={() => setConfirmSaveOpen(false)}
        onOk={doSave}
        okText="确定保存"
        cancelText="取消"
      >
        <p>当前账号使用角色模板 + 覆盖；本视图保存将切换为「按账号直接分配」并清除模板依赖。确定保存？</p>
      </Modal>
    </div>
  );
}
