/**
 * Role Templates tab: list/create/edit/delete, enable/disable, permission binding (draft + Save + Reset).
 * Uses /api/v1/admin/account-permissions/*. Permission tree: allowlist only (else show message, allow only rename/enable).
 */
import { useCallback, useEffect, useState } from 'react';
import { Button, Checkbox, Col, Input, message, Popconfirm, Row, Space, Switch, Table, Tag } from 'antd';
import {
  cloneRole,
  createRole,
  deleteRole,
  getRolePermissions,
  getRoles,
  saveRoleTemplate,
  updateRole,
  type RoleResponse,
} from './acctPermService';
import { useIsPermSuperAdmin } from '@/shared/components/PermSuperAdminGuard';
import { fetchPermissionTree, type ModuleNode } from '../RolesPermissions/permService';
import { ModuleOverviewCards } from './ModuleOverviewCards';
import { PermissionMenuTree } from './PermissionMenuTree';
import { collectSubtreeKeys, getKeysForOp, type ModuleNodeLike, type QuickOp } from './permissionQuickOps';

function getErrorMessage(err: { response?: { data?: { message?: string; machineCode?: string } } }): string {
  const data = err.response?.data;
  const code = data?.machineCode;
  if (code === 'PERM_FORBIDDEN') return '无权限';
  return data?.message ?? '操作失败，请重试';
}

export default function RoleTemplatesTab() {
  const { allowed: permAllowed } = useIsPermSuperAdmin();
  const [roles, setRoles] = useState<RoleResponse[]>([]);
  const [enabledOnly, setEnabledOnly] = useState(true);
  const [selectedRoleId, setSelectedRoleId] = useState<number | null>(null);
  const [treeData, setTreeData] = useState<{ modules: ModuleNode[] } | null>(null);
  const [treeUnavailable, setTreeUnavailable] = useState(false);
  const [savedLabel, setSavedLabel] = useState('');
  const [draftLabel, setDraftLabel] = useState('');
  const [savedEnabled, setSavedEnabled] = useState(true);
  const [draftEnabled, setDraftEnabled] = useState(true);
  const [savedPermissions, setSavedPermissions] = useState<Set<string>>(new Set());
  const [draftPermissions, setDraftPermissions] = useState<Set<string>>(new Set());
  const [savedDescription, setSavedDescription] = useState('');
  const [draftDescription, setDraftDescription] = useState('');
  const [showRoleKey, setShowRoleKey] = useState(false);
  const [saveLoading, setSaveLoading] = useState(false);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<RoleResponse | null>(null);
  const [cloneLoadingId, setCloneLoadingId] = useState<number | null>(null);

  const loadRoles = useCallback(() => {
    getRoles(enabledOnly).then(setRoles).catch(() => setRoles([]));
  }, [enabledOnly]);

  useEffect(() => {
    loadRoles();
  }, [loadRoles]);

  useEffect(() => {
    if (!permAllowed) {
      setTreeUnavailable(true);
      setTreeData({ modules: [] });
      return;
    }
    fetchPermissionTree()
      .then((data) => {
        setTreeData(data);
        setTreeUnavailable(false);
      })
      .catch((err: { response?: { status: number } }) => {
        if (err.response?.status === 403) {
          setTreeUnavailable(true);
          setTreeData({ modules: [] });
        } else {
          setTreeData({ modules: [] });
        }
      });
  }, [permAllowed]);

  useEffect(() => {
    if (selectedRoleId == null) {
      setSavedLabel('');
      setDraftLabel('');
      setSavedEnabled(true);
      setDraftEnabled(true);
      setSavedDescription('');
      setDraftDescription('');
      setSavedPermissions(new Set());
      setDraftPermissions(new Set());
      return;
    }
    const role = roles.find((r) => r.id === selectedRoleId);
    const label = role?.label ?? '';
    const enabled = role?.enabled !== false;
    const desc = role?.description ?? '';
    setSavedLabel(label);
    setDraftLabel(label);
    setSavedEnabled(enabled);
    setDraftEnabled(enabled);
    setSavedDescription(desc);
    setDraftDescription(desc);
    getRolePermissions(selectedRoleId)
      .then((keys) => {
        const set = new Set(keys);
        setSavedPermissions(set);
        setDraftPermissions(new Set(set));
      })
      .catch((err) => {
        message.error(getErrorMessage(err));
        if (err.response?.data?.machineCode === 'PERM_ROLE_NOT_FOUND') {
          loadRoles();
          setSelectedRoleId(null);
        }
      });
  }, [selectedRoleId, roles]);

  useEffect(() => {
    if (selectedRoleId == null) return;
    const role = roles.find((r) => r.id === selectedRoleId);
    if (role) {
      setSavedLabel(role.label);
      setDraftLabel(role.label);
      setSavedEnabled(role.enabled !== false);
      setDraftEnabled(role.enabled !== false);
      setSavedDescription(role.description ?? '');
      setDraftDescription(role.description ?? '');
    }
  }, [roles, selectedRoleId]);

  const isDirty =
    selectedRoleId != null &&
    (draftLabel !== savedLabel ||
      draftEnabled !== savedEnabled ||
      draftDescription !== savedDescription ||
      draftPermissions.size !== savedPermissions.size ||
      Array.from(draftPermissions).some((k) => !savedPermissions.has(k)) ||
      Array.from(savedPermissions).some((k) => !draftPermissions.has(k)));

  const toggleDraft = (key: string) => {
    setDraftPermissions((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  };

  const handleQuickOpRole = (node: ModuleNodeLike, op: QuickOp) => {
    const subKeys = collectSubtreeKeys(node);
    const toSet = getKeysForOp(node, op);
    setDraftPermissions((prev) => {
      const next = new Set(prev);
      subKeys.forEach((k) => next.delete(k));
      toSet.forEach((k) => next.add(k));
      return next;
    });
  };

  const getMenuCheckedRole = (node: ModuleNodeLike) => draftPermissions.has(`${node.key}:VIEW`);
  const onMenuCheckRole = (node: ModuleNodeLike, checked: boolean) => {
    const subKeys = collectSubtreeKeys(node);
    if (checked) {
      const viewKeys = getKeysForOp(node, 'readonly');
      setDraftPermissions((prev) => {
        const next = new Set(prev);
        subKeys.forEach((k) => next.delete(k));
        viewKeys.forEach((k) => next.add(k));
        return next;
      });
    } else {
      setDraftPermissions((prev) => {
        const next = new Set(prev);
        subKeys.forEach((k) => next.delete(k));
        return next;
      });
    }
  };

  const handleCreateRole = (values: { label: string; description?: string; enabled?: boolean }) => {
    createRole({
      label: values.label?.trim() || '',
      description: values.description?.trim() || undefined,
      enabled: values.enabled !== false,
    })
      .then((newRole) => {
        setRoles((prev) => [...prev, newRole]);
        setCreateModalOpen(false);
        setSelectedRoleId(newRole.id);
        message.success('角色已创建');
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const handleCloneRole = (id: number) => {
    setCloneLoadingId(id);
    cloneRole(id)
      .then((newRole) => {
        setRoles((prev) => [...prev, newRole]);
        setSelectedRoleId(newRole.id);
        message.success('已克隆为新角色');
      })
      .catch((err) => message.error(getErrorMessage(err)))
      .finally(() => setCloneLoadingId(null));
  };

  const handleUpdateRole = (id: number, label: string, enabled: boolean) => {
    updateRole(id, label, enabled)
      .then((updated) => {
        setRoles((prev) => prev.map((r) => (r.id === id ? { ...updated, enabled: updated.enabled !== false } : r)));
        setEditingRole(null);
        message.success('已更新');
      })
      .catch((err) => {
        message.error(getErrorMessage(err));
        if (err.response?.data?.machineCode === 'PERM_ROLE_NOT_FOUND') loadRoles();
      });
  };

  const handleDeleteRole = (id: number) => {
    deleteRole(id)
      .then(() => {
        setRoles((prev) => prev.filter((r) => r.id !== id));
        if (selectedRoleId === id) setSelectedRoleId(null);
        message.success('已删除');
      })
      .catch((err) => {
        message.error(getErrorMessage(err));
        if (err.response?.data?.machineCode === 'PERM_ROLE_NOT_FOUND') loadRoles();
      });
  };

  const handleSave = () => {
    if (selectedRoleId == null) return;
    setSaveLoading(true);
    const permissionKeys = Array.from(draftPermissions);
    const desc = draftDescription.trim() || undefined;
    saveRoleTemplate(selectedRoleId, draftLabel.trim() || draftLabel, draftEnabled, permissionKeys, desc)
      .then((updated) => {
        setRoles((prev) =>
          prev.map((r) =>
            r.id === selectedRoleId
              ? { ...r, label: updated.label, enabled: updated.enabled !== false, description: updated.description }
              : r,
          ),
        );
        setSavedLabel(draftLabel);
        setSavedEnabled(draftEnabled);
        setSavedDescription(draftDescription);
        setSavedPermissions(new Set(draftPermissions));
        setDraftPermissions(new Set(draftPermissions));
        message.success('已保存');
      })
      .catch((err) => message.error(getErrorMessage(err)))
      .finally(() => setSaveLoading(false));
  };

  const handleReset = () => {
    setDraftLabel(savedLabel);
    setDraftEnabled(savedEnabled);
    setDraftDescription(savedDescription);
    setDraftPermissions(new Set(savedPermissions));
    message.info('已还原为上次保存的状态');
  };

  return (
    <div style={{ marginTop: 12 }}>
      <Row gutter={24}>
        <Col span={8}>
          <div style={{ border: '1px solid #eee', borderRadius: 4, padding: 12 }}>
            <Space style={{ marginBottom: 8 }} wrap>
              <Button type="primary" size="small" onClick={() => setCreateModalOpen(true)}>
                新建角色
              </Button>
              <Checkbox checked={!enabledOnly} onChange={(e) => setEnabledOnly(!e.target.checked)}>
                显示全部（含停用）
              </Checkbox>
              <Checkbox checked={showRoleKey} onChange={(e) => setShowRoleKey(e.target.checked)}>
                显示内部Key
              </Checkbox>
            </Space>
            <Table<RoleResponse>
              size="small"
              pagination={false}
              dataSource={roles}
              rowKey="id"
              locale={{ emptyText: '暂无角色' }}
              columns={[
                {
                  title: '角色',
                  key: 'role',
                  render: (_, r) => (
                    <div>
                      <div style={{ fontWeight: 500 }}>{r.label}</div>
                      {showRoleKey && <div style={{ fontSize: 12, color: '#666' }}>{r.roleKey}</div>}
                      <Tag color={r.enabled !== false ? 'green' : 'default'} style={{ marginTop: 4 }}>
                        {r.enabled !== false ? '已启用' : '已停用'}
                      </Tag>
                    </div>
                  ),
                },
                {
                  title: '操作',
                  key: 'actions',
                  width: 140,
                  render: (_, r) => (
                    <Space size="small">
                      <Button
                        type="link"
                        size="small"
                        onClick={(e) => { e.stopPropagation(); handleCloneRole(r.id); }}
                        loading={cloneLoadingId === r.id}
                      >
                        克隆
                      </Button>
                      <Button type="link" size="small" onClick={(e) => { e.stopPropagation(); setEditingRole(r); }}>
                        编辑
                      </Button>
                      <Popconfirm title="确定删除该角色？" onConfirm={(e) => { e?.stopPropagation(); handleDeleteRole(r.id); }}>
                        <Button type="link" size="small" danger onClick={(e) => e.stopPropagation()}>
                          删除
                        </Button>
                      </Popconfirm>
                    </Space>
                  ),
                },
              ]}
              onRow={(r) => ({
                onClick: () => setSelectedRoleId(r.id),
                style: { cursor: 'pointer', background: selectedRoleId === r.id ? '#e6f7ff' : undefined },
              })}
            />
          </div>
        </Col>
        <Col span={16}>
          <div style={{ border: '1px solid #eee', borderRadius: 4, padding: 12 }}>
            {selectedRoleId == null ? (
              <p style={{ color: '#999' }}>请从左侧选择一个角色</p>
            ) : (
              <>
                <Space style={{ marginBottom: 12 }} wrap>
                  <Button type="primary" onClick={handleSave} loading={saveLoading} disabled={!isDirty}>
                    保存
                  </Button>
                  <Button onClick={handleReset} disabled={!isDirty}>
                    还原
                  </Button>
                </Space>
                <div style={{ marginBottom: 12 }}>
                  <Space align="center" wrap style={{ marginBottom: 8 }}>
                    <span style={{ fontSize: 12 }}>显示名称：</span>
                    <Input
                      value={draftLabel}
                      onChange={(e) => setDraftLabel(e.target.value)}
                      placeholder="角色显示名称"
                      style={{ width: 200 }}
                    />
                    <span style={{ fontSize: 12 }}>启用：</span>
                    <Switch checked={draftEnabled} onChange={setDraftEnabled} />
                  </Space>
                </div>
                <div style={{ marginBottom: 12 }}>
                  <span style={{ fontSize: 12, display: 'block', marginBottom: 4 }}>描述（选填）</span>
                  <Input.TextArea
                    value={draftDescription}
                    onChange={(e) => setDraftDescription(e.target.value)}
                    placeholder="角色描述"
                    rows={2}
                    style={{ width: '100%', maxWidth: 400 }}
                  />
                </div>
                {treeUnavailable && (
                  <p style={{ color: '#faad14', marginBottom: 12 }}>
                    无权限查看权限树（需超级管理员）。仅可修改显示名称与启用状态。
                  </p>
                )}
                {!treeUnavailable && (
                  <>
                    <ModuleOverviewCards
                      modules={treeData?.modules ?? []}
                      onQuickOp={handleQuickOpRole}
                      draftKeys={draftPermissions}
                    />
                    <p style={{ fontSize: 12, color: '#666' }}>菜单树勾选即授予该菜单至少「查看」；仅点击「保存」后生效</p>
                    {!treeData || treeData.modules.length === 0 ? (
                      <p style={{ color: '#999' }}>暂无模块，无法配置权限</p>
                    ) : (
                      <PermissionMenuTree
                        modules={treeData.modules}
                        getMenuChecked={getMenuCheckedRole}
                        onMenuCheck={onMenuCheckRole}
                        mode="role"
                        role={{ draft: draftPermissions, onToggle: toggleDraft }}
                      />
                    )}
                  </>
                )}
              </>
            )}
          </div>
        </Col>
      </Row>

      {/* 新建角色 */}
      {createModalOpen && (
        <CreateRoleModal
          open={createModalOpen}
          onClose={() => setCreateModalOpen(false)}
          onOk={handleCreateRole}
        />
      )}

      {/* 编辑角色（仅名称，快速编辑） */}
      {editingRole && (
        <EditRoleModal
          role={editingRole}
          onClose={() => setEditingRole(null)}
          onOk={(label, enabled) => {
            handleUpdateRole(editingRole.id, label, enabled);
          }}
        />
      )}
    </div>
  );
}

function CreateRoleModal({
  open,
  onClose,
  onOk,
}: {
  open: boolean;
  onClose: () => void;
  onOk: (values: { label: string; description?: string; enabled?: boolean }) => void;
}) {
  const [label, setLabel] = useState('');
  const [description, setDescription] = useState('');
  const [enabled, setEnabled] = useState(true);
  const handleSubmit = () => {
    if (!label.trim()) {
      message.warning('请输入名称');
      return;
    }
    onOk({ label: label.trim(), description: description.trim() || undefined, enabled });
    setLabel('');
    setDescription('');
    setEnabled(true);
  };
  if (!open) return null;
  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div style={{ background: '#fff', padding: 24, borderRadius: 8, minWidth: 360 }}>
        <h3 style={{ marginTop: 0 }}>新建角色</h3>
        <div style={{ marginBottom: 12 }}>
          <label style={{ display: 'block', marginBottom: 4, fontSize: 12 }}>名称（必填）</label>
          <Input value={label} onChange={(e) => setLabel(e.target.value)} placeholder="例如：平台编辑" />
        </div>
        <div style={{ marginBottom: 12 }}>
          <label style={{ display: 'block', marginBottom: 4, fontSize: 12 }}>描述（选填）</label>
          <Input.TextArea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="角色描述"
            rows={2}
          />
        </div>
        <div style={{ marginBottom: 16 }}>
          <Space align="center">
            <span style={{ fontSize: 12 }}>启用</span>
            <Switch checked={enabled} onChange={setEnabled} />
          </Space>
        </div>
        <Space>
          <Button type="primary" onClick={handleSubmit}>
            创建
          </Button>
          <Button onClick={onClose}>取消</Button>
        </Space>
      </div>
    </div>
  );
}

function EditRoleModal({
  role,
  onClose,
  onOk,
}: {
  role: RoleResponse;
  onClose: () => void;
  onOk: (label: string, enabled: boolean) => void;
}) {
  const [label, setLabel] = useState(role.label);
  const [enabled, setEnabled] = useState(role.enabled !== false);
  useEffect(() => {
    setLabel(role.label);
    setEnabled(role.enabled !== false);
  }, [role.id, role.label, role.enabled]);
  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.45)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div style={{ background: '#fff', padding: 24, borderRadius: 8, minWidth: 360 }}>
        <h3 style={{ marginTop: 0 }}>编辑角色</h3>
        <div style={{ marginBottom: 12 }}>
          <label style={{ display: 'block', marginBottom: 4, fontSize: 12 }}>显示名称</label>
          <Input value={label} onChange={(e) => setLabel(e.target.value)} />
        </div>
        <div style={{ marginBottom: 16 }}>
          <Space>
            <span style={{ fontSize: 12 }}>启用</span>
            <Switch checked={enabled} onChange={setEnabled} />
          </Space>
        </div>
        <Space>
          <Button type="primary" onClick={() => onOk(label, enabled)}>
            保存
          </Button>
          <Button onClick={onClose}>取消</Button>
        </Space>
      </div>
    </div>
  );
}
