/**
 * 业务模块目录维护 UI（仅 allowlist 超管）。树 + 操作点 + 未归类认领。全中文。
 */
import { useCallback, useEffect, useState } from 'react';
import {
  Button,
  Input,
  message,
  Modal,
  Popconfirm,
  Space,
  Switch,
  Table,
  Tooltip,
  Typography,
} from 'antd';
import {
  ArrowDownOutlined,
  ArrowUpOutlined,
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
} from '@ant-design/icons';
import {
  claimOpPoints,
  createNode,
  deleteNode,
  getCatalog,
  reorderChildren,
  updateHandledOnly,
  updateNode,
  updateOpPoint,
  type CatalogNode,
  type CatalogResponse,
  type OpPoint,
} from './bizpermCatalogService';

function getErrorMessage(err: { response?: { data?: { message?: string; machineCode?: string }; status?: number } }): string {
  if (err.response?.status === 403) return '无权限（仅超管可维护）';
  return err.response?.data?.message ?? '操作失败，请重试';
}

function flattenNodes(nodes: CatalogNode[], out: CatalogNode[]): void {
  for (const n of nodes) {
    out.push(n);
    if (n.children?.length) flattenNodes(n.children, out);
  }
}

function findNodeById(nodes: CatalogNode[], id: number): CatalogNode | null {
  for (const n of nodes) {
    if (n.id === id) return n;
    const inChild = findNodeById(n.children ?? [], id);
    if (inChild) return inChild;
  }
  return null;
}

interface BizPermCatalogMaintenanceProps {
  onError?: (msg: string) => void;
}

export default function BizPermCatalogMaintenance({ onError }: BizPermCatalogMaintenanceProps) {
  const [catalog, setCatalog] = useState<CatalogResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedNodeId, setSelectedNodeId] = useState<number | null>(null);
  const [unclassifiedSearch, setUnclassifiedSearch] = useState('');
  const [claimSelectedKeys, setClaimSelectedKeys] = useState<string[]>([]);
  const [addFromUnclassifiedOpen, setAddFromUnclassifiedOpen] = useState(false);
  const [createChildOpen, setCreateChildOpen] = useState(false);
  const [createChildName, setCreateChildName] = useState('');
  const [createChildSubmitting, setCreateChildSubmitting] = useState(false);
  const [renameOpen, setRenameOpen] = useState(false);
  const [renameNodeId, setRenameNodeId] = useState<number | null>(null);
  const [renameValue, setRenameValue] = useState('');
  const [renameSubmitting, setRenameSubmitting] = useState(false);
  const [opEditCnName, setOpEditCnName] = useState<{ id: number; value: string } | null>(null);
  const [expandKeyRow, setExpandKeyRow] = useState<number | null>(null);

  const loadCatalog = useCallback(() => {
    setLoading(true);
    getCatalog()
      .then((data) => {
        setCatalog(data);
        setLoading(false);
      })
      .catch((err) => {
        const msg = getErrorMessage(err);
        message.error(msg);
        if (onError) onError(msg);
        setCatalog({ tree: [], unclassified: [] });
        setLoading(false);
      });
  }, [onError]);

  useEffect(() => {
    loadCatalog();
  }, [loadCatalog]);

  const flatNodes = catalog ? (() => { const out: CatalogNode[] = []; flattenNodes(catalog.tree, out); return out; })() : [];
  const selectedNode = selectedNodeId && catalog ? findNodeById(catalog.tree, selectedNodeId) : null;
  const unclassifiedFiltered =
    catalog?.unclassified.filter(
      (u) => !unclassifiedSearch.trim() || u.permissionKey.toLowerCase().includes(unclassifiedSearch.toLowerCase()) || (u.cnName ?? '').toLowerCase().includes(unclassifiedSearch.toLowerCase()),
    ) ?? [];

  const handleCreateChild = () => {
    if (!createChildName.trim()) {
      message.warning('请输入中文名称');
      return;
    }
    setCreateChildSubmitting(true);
    createNode({ cnName: createChildName.trim(), parentId: selectedNodeId ?? undefined })
      .then(() => {
        message.success('已添加');
        setCreateChildOpen(false);
        setCreateChildName('');
        loadCatalog();
      })
      .catch((err) => message.error(getErrorMessage(err)))
      .finally(() => setCreateChildSubmitting(false));
  };

  const handleRenameSubmit = () => {
    if (renameNodeId == null || !renameValue.trim()) return;
    setRenameSubmitting(true);
    updateNode(renameNodeId, { cnName: renameValue.trim() })
      .then(() => {
        message.success('已保存');
        setRenameOpen(false);
        setRenameNodeId(null);
        setRenameValue('');
        loadCatalog();
      })
      .catch((err) => message.error(getErrorMessage(err)))
      .finally(() => setRenameSubmitting(false));
  };

  const handleDeleteNode = (id: number) => {
    deleteNode(id)
      .then(() => {
        message.success('已删除');
        if (selectedNodeId === id) setSelectedNodeId(null);
        loadCatalog();
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const handleMoveNode = (nodeId: number, up: boolean) => {
    const parent = flatNodes.find((n) => (n.children ?? []).some((c) => c.id === nodeId)) ?? null;
    const list = parent ? (parent.children ?? []) : (catalog?.tree ?? []);
    const idx = list.findIndex((c) => c.id === nodeId);
    if (idx < 0) return;
    const newIdx = up ? idx - 1 : idx + 1;
    if (newIdx < 0 || newIdx >= list.length) return;
    const ordered = [...list.map((c) => c.id)];
    const a = ordered[idx];
    ordered[idx] = ordered[newIdx];
    ordered[newIdx] = a;
    const parentId = parent?.id ?? null;
    if (parentId == null) {
      message.warning('根节点顺序暂不支持调整');
      return;
    }
    reorderChildren(parentId, ordered)
      .then(() => {
        message.success('已移动');
        loadCatalog();
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const handleOpCnNameBlur = (op: OpPoint) => {
    if (op.id == null || opEditCnName == null || opEditCnName.id !== op.id) return;
    const value = opEditCnName.value.trim();
    setOpEditCnName(null);
    if (value === (op.cnName ?? '')) return;
    updateOpPoint(op.id, { cnName: value || undefined })
      .then(() => {
        message.success('已保存');
        loadCatalog();
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const handleOpMove = (nodeOps: OpPoint[], index: number, up: boolean) => {
    const newIndex = up ? index - 1 : index + 1;
    if (newIndex < 0 || newIndex >= nodeOps.length) return;
    const reordered = [...nodeOps];
    const t = reordered[index];
    reordered[index] = reordered[newIndex];
    reordered[newIndex] = t;
    Promise.all(reordered.map((op, i) => (op.id != null ? updateOpPoint(op.id, { sortOrder: i }) : Promise.resolve())))
      .then(() => {
        message.success('已移动');
        loadCatalog();
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const handleClaimToNode = () => {
    if (selectedNodeId == null) {
      message.warning('请先选择左侧节点');
      return;
    }
    if (claimSelectedKeys.length === 0) {
      message.warning('请勾选要认领的权限');
      return;
    }
    claimOpPoints({ nodeId: selectedNodeId, permissionKeys: claimSelectedKeys })
      .then(() => {
        message.success('已认领');
        setAddFromUnclassifiedOpen(false);
        setClaimSelectedKeys([]);
        loadCatalog();
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const handleHandledOnly = (op: OpPoint, checked: boolean) => {
    if (op.id == null) return;
    updateHandledOnly(op.id, checked)
      .then(() => {
        message.success(checked ? '已设为仅已处理' : '已取消');
        loadCatalog();
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const renderTreeNode = (node: CatalogNode, parent: CatalogNode | null, siblingIndex: number, siblingCount: number) => (
    <div key={node.id} style={{ marginLeft: parent ? 16 : 0, marginBottom: 4 }}>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 4,
          padding: '4px 8px',
          background: selectedNodeId === node.id ? '#e6f7ff' : undefined,
          borderRadius: 4,
          cursor: 'pointer',
        }}
        onClick={() => setSelectedNodeId(node.id)}
      >
        <span style={{ flex: 1, fontSize: 13 }}>{node.cnName}</span>
        <Space size={4} onClick={(e) => e.stopPropagation()}>
          <Tooltip title="新建子节点">
            <Button type="text" size="small" icon={<PlusOutlined />} onClick={() => { setSelectedNodeId(node.id); setCreateChildOpen(true); }} />
          </Tooltip>
          <Tooltip title="重命名">
            <Button type="text" size="small" icon={<EditOutlined />} onClick={() => { setRenameNodeId(node.id); setRenameValue(node.cnName); setRenameOpen(true); }} />
          </Tooltip>
          <Popconfirm title="确定删除该节点？有子节点或操作点时不可删。" onConfirm={() => handleDeleteNode(node.id)} okText="确定" cancelText="取消">
            <Tooltip title="删除">
              <Button type="text" size="small" danger icon={<DeleteOutlined />} onClick={(e) => e.stopPropagation()} />
            </Tooltip>
          </Popconfirm>
          <Tooltip title="上移">
            <Button type="text" size="small" icon={<ArrowUpOutlined />} disabled={siblingIndex === 0} onClick={(e) => { e.stopPropagation(); handleMoveNode(node.id, true); }} />
          </Tooltip>
          <Tooltip title="下移">
            <Button type="text" size="small" icon={<ArrowDownOutlined />} disabled={siblingIndex >= siblingCount - 1} onClick={(e) => { e.stopPropagation(); handleMoveNode(node.id, false); }} />
          </Tooltip>
        </Space>
      </div>
      {(node.children ?? []).map((child, i) => renderTreeNode(child, node, i, (node.children ?? []).length))}
    </div>
  );

  if (loading && !catalog) {
    return <Typography.Text type="secondary">加载中…</Typography.Text>;
  }

  const ops = selectedNode?.ops ?? [];

  return (
    <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', alignItems: 'flex-start' }}>
      {/* 左侧：目录树 */}
      <div style={{ minWidth: 280, maxWidth: 360, border: '1px solid #f0f0f0', borderRadius: 4, padding: 8, background: '#fff' }}>
        <p style={{ fontSize: 12, fontWeight: 600, marginBottom: 8 }}>目录树</p>
        <Space style={{ marginBottom: 8 }}>
          <Button size="small" type="primary" icon={<PlusOutlined />} onClick={() => { setSelectedNodeId(null); setCreateChildOpen(true); }}>
            新建根节点
          </Button>
        </Space>
        <div style={{ maxHeight: 320, overflow: 'auto' }}>
          {(catalog?.tree ?? []).map((root, i) => renderTreeNode(root, null, i, (catalog?.tree ?? []).length))}
        </div>
      </div>

      {/* 右侧：当前节点操作点 */}
      <div style={{ flex: 1, minWidth: 280, border: '1px solid #f0f0f0', borderRadius: 4, padding: 8, background: '#fff' }}>
        <p style={{ fontSize: 12, fontWeight: 600, marginBottom: 8 }}>
          {selectedNode ? `「${selectedNode.cnName}」操作点` : '请选择左侧节点'}
        </p>
        {selectedNode && (
          <Space style={{ marginBottom: 8 }}>
            <Button size="small" onClick={() => setAddFromUnclassifiedOpen(true)}>
              从未归类添加
            </Button>
          </Space>
        )}
        {selectedNode && (
          <Table
            size="small"
            pagination={false}
            rowKey={(op) => op.id ?? op.permissionKey}
            dataSource={ops}
            columns={[
              {
                title: '中文名称',
                dataIndex: 'cnName',
                width: 140,
                render: (_, op) =>
                  op.id != null && opEditCnName?.id === op.id ? (
                    <Input
                      size="small"
                      value={opEditCnName.value}
                      onChange={(e) => setOpEditCnName({ id: op.id, value: e.target.value })}
                      onBlur={() => handleOpCnNameBlur(op)}
                      onPressEnter={() => handleOpCnNameBlur(op)}
                      autoFocus
                    />
                  ) : (
                    <span onClick={() => op.id != null && setOpEditCnName({ id: op.id, value: op.cnName ?? '' })} style={{ cursor: op.id != null ? 'pointer' : 'default' }}>
                      {op.cnName || '—'}
                    </span>
                  ),
              },
              {
                title: '权限键',
                key: 'key',
                width: 140,
                render: (_, op) =>
                  op.id != null && expandKeyRow === op.id ? (
                    <span style={{ fontSize: 11, wordBreak: 'break-all' }}>
                      <Typography.Text copyable>{op.permissionKey}</Typography.Text>
                      <Button type="link" size="small" style={{ padding: '0 4px', height: 'auto' }} onClick={() => setExpandKeyRow(null)}>收起</Button>
                    </span>
                  ) : (
                    <Button type="link" size="small" style={{ padding: 0, height: 'auto' }} onClick={() => op.id != null && setExpandKeyRow(op.id)}>展开</Button>
                  ),
              },
              {
                title: '仅已处理',
                dataIndex: 'handledOnly',
                width: 90,
                render: (_, op) => <Switch size="small" checked={op.handledOnly} onChange={(v) => handleHandledOnly(op, v)} />,
              },
              {
                title: '排序',
                key: 'order',
                width: 80,
                render: (_, __, index) => (
                  <Space>
                    <Button type="text" size="small" icon={<ArrowUpOutlined />} disabled={index === 0} onClick={() => handleOpMove(ops, index, true)} />
                    <Button type="text" size="small" icon={<ArrowDownOutlined />} disabled={index >= ops.length - 1} onClick={() => handleOpMove(ops, index, false)} />
                  </Space>
                ),
              },
            ]}
          />
        )}
        {selectedNode && ops.length === 0 && <p style={{ fontSize: 12, color: '#999' }}>暂无操作点，可点击「从未归类添加」认领</p>}
      </div>

      {/* 未归类/待认领 */}
      <div style={{ minWidth: 260, maxWidth: 340, border: '1px solid #f0f0f0', borderRadius: 4, padding: 8, background: '#fff' }}>
        <p style={{ fontSize: 12, fontWeight: 600, marginBottom: 8 }}>未归类 / 待认领</p>
        <Input.Search
          size="small"
          placeholder="搜索权限键或中文名"
          value={unclassifiedSearch}
          onChange={(e) => setUnclassifiedSearch(e.target.value)}
          style={{ marginBottom: 8 }}
          allowClear
        />
        <div style={{ maxHeight: 280, overflow: 'auto' }}>
          {unclassifiedFiltered.length === 0 ? (
            <p style={{ fontSize: 12, color: '#999' }}>暂无未归类权限</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              {unclassifiedFiltered.map((u) => (
                <div key={u.id ?? u.permissionKey} style={{ fontSize: 12, display: 'flex', alignItems: 'center', gap: 8 }}>
                  <input
                    type="checkbox"
                    checked={claimSelectedKeys.includes(u.permissionKey)}
                    onChange={(e) => setClaimSelectedKeys((prev) => (e.target.checked ? [...prev, u.permissionKey] : prev.filter((k) => k !== u.permissionKey)))}
                  />
                  <span title={u.permissionKey}>{u.cnName || u.permissionKey}</span>
                </div>
              ))}
            </div>
          )}
        </div>
        <Button size="small" type="primary" style={{ marginTop: 8 }} disabled={!selectedNodeId || claimSelectedKeys.length === 0} onClick={handleClaimToNode}>
          一键认领到当前节点
        </Button>
      </div>

      <Modal title="新建子节点" open={createChildOpen} onCancel={() => { setCreateChildOpen(false); setCreateChildName(''); }} onOk={handleCreateChild} confirmLoading={createChildSubmitting} okText="确定" cancelText="取消">
        <p style={{ marginBottom: 8 }}>中文名称</p>
        <Input value={createChildName} onChange={(e) => setCreateChildName(e.target.value)} placeholder="请输入中文名称" />
      </Modal>

      <Modal title="重命名" open={renameOpen} onCancel={() => { setRenameOpen(false); setRenameNodeId(null); setRenameValue(''); }} onOk={handleRenameSubmit} confirmLoading={renameSubmitting} okText="确定" cancelText="取消">
        <p style={{ marginBottom: 8 }}>中文名称</p>
        <Input value={renameValue} onChange={(e) => setRenameValue(e.target.value)} placeholder="请输入中文名称" />
      </Modal>

      <Modal
        title="从未归类添加"
        open={addFromUnclassifiedOpen}
        onCancel={() => { setAddFromUnclassifiedOpen(false); setClaimSelectedKeys([]); }}
        footer={[
          <Button key="cancel" onClick={() => { setAddFromUnclassifiedOpen(false); setClaimSelectedKeys([]); }}>取消</Button>,
          <Button key="ok" type="primary" onClick={handleClaimToNode} disabled={claimSelectedKeys.length === 0}>认领到当前节点</Button>,
        ]}
      >
        <Input.Search size="small" placeholder="搜索" value={unclassifiedSearch} onChange={(e) => setUnclassifiedSearch(e.target.value)} style={{ marginBottom: 8 }} allowClear />
        <div style={{ maxHeight: 300, overflow: 'auto' }}>
          {unclassifiedFiltered.length === 0 ? (
            <p style={{ fontSize: 12, color: '#999' }}>暂无未归类权限</p>
          ) : (
            unclassifiedFiltered.map((u) => (
              <div key={u.id ?? u.permissionKey} style={{ marginBottom: 4 }}>
                <input
                  type="checkbox"
                  checked={claimSelectedKeys.includes(u.permissionKey)}
                  onChange={(e) => setClaimSelectedKeys((prev) => (e.target.checked ? [...prev, u.permissionKey] : prev.filter((k) => k !== u.permissionKey)))}
                />
                <span style={{ marginLeft: 8 }}>{u.cnName || u.permissionKey}</span>
                <Typography.Text copyable style={{ fontSize: 11, marginLeft: 4 }}>{u.permissionKey}</Typography.Text>
              </div>
            ))
          )}
        </div>
      </Modal>
    </div>
  );
}
