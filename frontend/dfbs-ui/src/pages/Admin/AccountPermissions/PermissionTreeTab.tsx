/**
 * 权限管理 Tab — allowlist only. View tree + module CRUD (all modules editable via id from tree).
 * Uses GET/POST/PUT/DELETE /api/v1/admin/perm/permission-tree and /modules*. 403 → 无权限.
 */
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Checkbox, message, Space, Switch, Table, Tag, Tooltip } from 'antd';
import {
  createModule,
  deleteModule,
  fetchPermissionTree,
  setModuleActions,
  updateModule,
  type ActionItem,
  type ModuleNode,
  type ModuleResponse,
  type PermissionTreeResponse,
} from '../RolesPermissions/permService';

function getErrorMessage(err: { response?: { data?: { message?: string; machineCode?: string } } }): string {
  const data = err.response?.data;
  if (data?.machineCode === 'PERM_MODULE_HAS_CHILDREN') return '该模块下有子模块，请先删除子模块';
  if (data?.machineCode === 'PERM_FORBIDDEN') return '无权限';
  return data?.message ?? '操作失败，请重试';
}

function findModuleActionsByKey(nodes: ModuleNode[], moduleKey: string): string[] {
  for (const n of nodes || []) {
    if (n.key === moduleKey) return n.actions ?? [];
    const inChild = findModuleActionsByKey(n.children ?? [], moduleKey);
    if (inChild.length > 0) return inChild;
  }
  return [];
}

/** Flatten tree to list of modules with id (for editing all). */
function flattenModules(nodes: ModuleNode[]): Array<ModuleResponse & { enabled: boolean }> {
  const out: Array<ModuleResponse & { enabled: boolean }> = [];
  function walk(list: ModuleNode[]) {
    for (const n of list || []) {
      if (n.id != null) {
        out.push({
          id: n.id,
          moduleKey: n.key,
          label: n.label,
          parentId: n.parentId ?? null,
          enabled: n.enabled !== false,
        });
      }
      if (n.children?.length) walk(n.children);
    }
  }
  walk(nodes);
  return out;
}

function TreeDisplay({ nodes, depth = 0 }: { nodes: ModuleNode[]; depth?: number }) {
  if (!nodes?.length) return null;
  return (
    <ul style={{ margin: 0, paddingLeft: depth ? 20 : 0, listStyle: 'none' }}>
      {nodes.map((n) => (
        <li key={n.key} style={{ marginBottom: 8 }}>
          <div style={{ padding: '4px 8px', background: depth ? '#fafafa' : '#f0f0f0', borderRadius: 4 }}>
            <strong>{n.label}</strong>（{n.key}）
            {n.enabled === false && (
              <Tag color="default" style={{ marginLeft: 8 }}>已停用</Tag>
            )}
            {n.actions?.length > 0 && (
              <span style={{ marginLeft: 8, fontSize: 12, color: '#666' }}>
                {n.actions.join(', ')}
              </span>
            )}
          </div>
          {n.children?.length > 0 && <TreeDisplay nodes={n.children} depth={depth + 1} />}
        </li>
      ))}
    </ul>
  );
}

export default function PermissionTreeTab() {
  const [tree, setTree] = useState<PermissionTreeResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [forbidden, setForbidden] = useState(false);
  const [createKey, setCreateKey] = useState('');
  const [createLabel, setCreateLabel] = useState('');
  const [createParentId, setCreateParentId] = useState<number | null>(null);
  const [createEnabled, setCreateEnabled] = useState(true);
  const [createLoading, setCreateLoading] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editLabel, setEditLabel] = useState('');
  const [editParentId, setEditParentId] = useState<number | null>(null);
  const [editEnabled, setEditEnabled] = useState(true);
  const [actionsModalId, setActionsModalId] = useState<number | null>(null);
  const [draftActionKeys, setDraftActionKeys] = useState<Set<string>>(new Set());

  const allModules = useMemo(() => (tree?.modules ? flattenModules(tree.modules) : []), [tree?.modules]);

  const loadTree = useCallback(() => {
    setLoading(true);
    fetchPermissionTree()
      .then((data) => {
        setTree(data);
        setForbidden(false);
      })
      .catch((err: { response?: { status: number } }) => {
        if (err.response?.status === 403) {
          setForbidden(true);
          setTree(null);
        } else {
          message.error('加载权限树失败');
        }
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadTree();
  }, [loadTree]);

  const handleCreate = () => {
    if (!createKey.trim()) {
      message.warning('请输入模块 key');
      return;
    }
    setCreateLoading(true);
    createModule(createKey.trim(), createLabel.trim() || createKey.trim(), createParentId, createEnabled)
      .then(() => {
        setCreateKey('');
        setCreateLabel('');
        setCreateParentId(null);
        message.success('模块已创建');
        loadTree();
      })
      .catch((err) => message.error(getErrorMessage(err)))
      .finally(() => setCreateLoading(false));
  };

  const handleUpdate = (id: number, label: string, parentId: number | null, enabled?: boolean) => {
    updateModule(id, label, parentId, enabled)
      .then(() => {
        setEditingId(null);
        message.success('已更新');
        loadTree();
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const handleToggleEnabled = (mod: ModuleResponse & { enabled: boolean }) => {
    updateModule(mod.id, mod.label, mod.parentId, !mod.enabled)
      .then(() => {
        message.success(mod.enabled ? '已停用' : '已启用');
        loadTree();
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const handleDelete = (id: number) => {
    deleteModule(id)
      .then(() => {
        setActionsModalId(null);
        setEditingId(null);
        message.success('已删除');
        loadTree();
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const handleSetActions = (id: number, actionKeys: string[]) => {
    setModuleActions(id, actionKeys)
      .then(() => {
        setActionsModalId(null);
        message.success('已保存动作');
        loadTree();
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  if (loading) return <div style={{ padding: 24 }}>加载中...</div>;
  if (forbidden) return <div style={{ padding: 24, color: '#cf1322' }}>无权限</div>;
  if (!tree) return <div style={{ padding: 24 }}>加载失败</div>;

  const openActionsModal = (mod: ModuleResponse) => {
    const currentKeys = tree ? findModuleActionsByKey(tree.modules, mod.moduleKey) : [];
    setActionsModalId(mod.id);
    setDraftActionKeys(new Set(currentKeys));
  };

  const startEdit = (mod: ModuleResponse & { enabled: boolean }) => {
    setEditingId(mod.id);
    setEditLabel(mod.label);
    setEditParentId(mod.parentId);
    setEditEnabled(mod.enabled);
  };

  return (
    <div style={{ marginTop: 12 }}>
      <h4 style={{ marginTop: 0 }}>权限树</h4>
      <div style={{ marginBottom: 16, padding: 12, border: '1px solid #eee', borderRadius: 4 }}>
        <TreeDisplay nodes={tree.modules} />
      </div>

      <h4>新建模块</h4>
      <Space wrap style={{ marginBottom: 16 }}>
        <input
          placeholder="模块 key"
          value={createKey}
          onChange={(e) => setCreateKey(e.target.value)}
          style={{ width: 160, padding: '4px 8px' }}
        />
        <input
          placeholder="显示名称"
          value={createLabel}
          onChange={(e) => setCreateLabel(e.target.value)}
          style={{ width: 140, padding: '4px 8px' }}
        />
        <select
          value={createParentId ?? ''}
          onChange={(e) => setCreateParentId(e.target.value === '' ? null : Number(e.target.value))}
          style={{ padding: '4px 8px' }}
        >
          <option value="">根节点</option>
          {allModules.map((m) => (
            <option key={m.id} value={m.id}>
              {m.label} ({m.moduleKey})
            </option>
          ))}
        </select>
        <span style={{ fontSize: 12 }}>启用</span>
        <Switch checked={createEnabled} onChange={setCreateEnabled} />
        <Button type="primary" onClick={handleCreate} loading={createLoading}>
          创建
        </Button>
      </Space>

      <h4>模块列表（可编辑全部）</h4>
      <p style={{ fontSize: 12, color: '#666', marginBottom: 8 }}>
        停用仅影响可见性（v1），不删除数据。
      </p>
      {allModules.length === 0 ? (
        <p style={{ color: '#999' }}>暂无模块；可创建新模块。</p>
      ) : (
        <Table<ModuleResponse & { enabled: boolean }>
          size="small"
          dataSource={allModules}
          rowKey="id"
          pagination={false}
          columns={[
            { title: 'ID', dataIndex: 'id', width: 60 },
            { title: 'key', dataIndex: 'moduleKey', width: 160 },
            { title: '显示名称', dataIndex: 'label' },
            { title: '父节点 ID', dataIndex: 'parentId', width: 90, render: (v) => v ?? '—' },
            {
              title: '状态',
              key: 'enabled',
              width: 120,
              render: (_, mod) => (
                <Space size="small">
                  <Tag color={mod.enabled ? 'green' : 'default'}>{mod.enabled ? '已启用' : '已停用'}</Tag>
                  <Tooltip title="停用仅影响可见性（v1），不删除数据">
                    <Switch
                      size="small"
                      checked={mod.enabled}
                      onChange={() => handleToggleEnabled(mod)}
                    />
                  </Tooltip>
                </Space>
              ),
            },
            {
              title: '操作',
              key: 'actions',
              width: 220,
              render: (_, mod) => (
                <Space size="small">
                  <Button type="link" size="small" onClick={() => startEdit(mod)}>
                    编辑
                  </Button>
                  <Button type="link" size="small" onClick={() => openActionsModal(mod)}>
                    设置动作
                  </Button>
                  <Button type="link" size="small" danger onClick={() => handleDelete(mod.id)}>
                    删除
                  </Button>
                </Space>
              ),
            },
          ]}
        />
      )}

      {editingId != null && (() => {
        const mod = allModules.find((m) => m.id === editingId);
        if (!mod) return null;
        return (
          <div style={{ marginTop: 16, padding: 12, border: '1px solid #eee', borderRadius: 4 }}>
            <h4>编辑模块</h4>
            <p style={{ fontSize: 12, color: '#666', marginBottom: 8 }}>
              不能将父级设为自己或子节点；无效选择时保存会提示错误。
            </p>
            <Space wrap align="center" style={{ marginBottom: 8 }}>
              <input
                value={editLabel}
                onChange={(e) => setEditLabel(e.target.value)}
                placeholder="显示名称"
                style={{ width: 180, padding: '4px 8px' }}
              />
              <select
                value={editParentId ?? ''}
                onChange={(e) => setEditParentId(e.target.value === '' ? null : Number(e.target.value))}
                style={{ padding: '4px 8px' }}
              >
                <option value="">根节点</option>
                {allModules
                  .filter((m) => m.id !== editingId)
                  .map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.label} ({m.moduleKey})
                    </option>
                  ))}
              </select>
              <span style={{ fontSize: 12 }}>启用</span>
              <Switch checked={editEnabled} onChange={setEditEnabled} />
              <Button type="primary" size="small" onClick={() => handleUpdate(editingId, editLabel, editParentId, editEnabled)}>
                保存
              </Button>
              <Button size="small" onClick={() => setEditingId(null)}>
                取消
              </Button>
            </Space>
          </div>
        );
      })()}

      {actionsModalId != null && (() => {
        const mod = allModules.find((m) => m.id === actionsModalId);
        if (!mod || !tree) return null;
        const toggleAction = (key: string) => {
          setDraftActionKeys((prev) => {
            const next = new Set(prev);
            if (next.has(key)) next.delete(key);
            else next.add(key);
            return next;
          });
        };
        return (
          <div style={{ marginTop: 16, padding: 12, border: '1px solid #eee', borderRadius: 4 }}>
            <h4>设置动作：{mod.label}（{mod.moduleKey}）</h4>
            <p style={{ fontSize: 12, color: '#666' }}>勾选该模块可用的动作（保存后立即生效）</p>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, marginBottom: 12 }}>
              {(tree.actions || []).map((a: ActionItem) => (
                <Checkbox
                  key={a.key}
                  checked={draftActionKeys.has(a.key)}
                  onChange={() => toggleAction(a.key)}
                >
                  {a.label}（{a.key}）
                </Checkbox>
              ))}
            </div>
            <Space>
              <Button type="primary" size="small" onClick={() => handleSetActions(actionsModalId, Array.from(draftActionKeys))}>
                保存
              </Button>
              <Button size="small" onClick={() => setActionsModalId(null)}>
                取消
              </Button>
            </Space>
          </div>
        );
      })()}
    </div>
  );
}
