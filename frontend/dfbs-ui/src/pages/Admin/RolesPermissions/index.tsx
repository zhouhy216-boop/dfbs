/**
 * 角色与权限 — permission model v1 + role templates & permission binding (allowlist only).
 * Left: role list (CRUD). Right: permission tree draft + Save/Reset.
 */
import { useCallback, useEffect, useState } from 'react';
import { Table, Tag, message, Button, Space, Modal, Form, Input, Popconfirm, Checkbox, Switch, Row, Col, Tabs, Select } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  fetchPermissionTree,
  fetchRoles,
  createRole,
  updateRole,
  deleteRole,
  fetchRolePermissions,
  saveRoleTemplate,
  searchUsers,
  getAccountOverride,
  saveAccountOverride,
  getTestVision,
  setTestVision,
  getTestAccounts,
  resetTestAccounts,
  type PermissionTreeResponse,
  type ActionItem,
  type ModuleNode,
  type RoleResponse,
  type UserSummary,
  type AccountOverrideResponse,
  type KitAccountSummary,
} from './permService';
import { useVisionStore } from '@/shared/stores/useVisionStore';

type LoadState = 'loading' | 'success' | 'error' | 'forbidden';

const MACHINE_CODE_MESSAGES: Record<string, string> = {
  PERM_ROLE_KEY_EXISTS: '角色 key 已存在',
  PERM_INVALID_PERMISSION_KEY: '权限 key 无效（模块或动作不存在）',
  PERM_ROLE_NOT_FOUND: '角色不存在或已删除，请刷新列表',
  PERM_USER_NOT_FOUND: '账号不存在，请刷新列表',
  PERM_ROLE_DISABLED: '只能分配已启用的角色模板',
};

function getErrorMessage(err: { response?: { data?: { message?: string; machineCode?: string } } }): string {
  const data = err.response?.data;
  const code = data?.machineCode;
  if (code && MACHINE_CODE_MESSAGES[code]) return MACHINE_CODE_MESSAGES[code];
  return data?.message ?? '操作失败，请重试';
}

export default function RolesPermissionsPage() {
  const [state, setState] = useState<LoadState>('loading');
  const [treeData, setTreeData] = useState<PermissionTreeResponse | null>(null);
  const [roles, setRoles] = useState<RoleResponse[]>([]);
  const [enabledOnly, setEnabledOnly] = useState(true);
  const [selectedRoleId, setSelectedRoleId] = useState<number | null>(null);
  const [savedLabel, setSavedLabel] = useState('');
  const [draftLabel, setDraftLabel] = useState('');
  const [savedEnabled, setSavedEnabled] = useState(true);
  const [draftEnabled, setDraftEnabled] = useState(true);
  const [savedPermissions, setSavedPermissions] = useState<Set<string>>(new Set());
  const [draftPermissions, setDraftPermissions] = useState<Set<string>>(new Set());
  const [saveLoading, setSaveLoading] = useState(false);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<RoleResponse | null>(null);

  // --- 按账号覆盖 (Step-04) ---
  const [activeTab, setActiveTab] = useState<string>('templates');
  const [userSearchQuery, setUserSearchQuery] = useState('');
  const [userSearchResults, setUserSearchResults] = useState<UserSummary[]>([]);
  const [userSearchLoading, setUserSearchLoading] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserSummary | null>(null);
  const [overrideLoading, setOverrideLoading] = useState(false);
  const [overrideSaveLoading, setOverrideSaveLoading] = useState(false);
  const [rolesForAccount, setRolesForAccount] = useState<RoleResponse[]>([]);
  const [templateKeys, setTemplateKeys] = useState<Set<string>>(new Set());
  const [savedOverride, setSavedOverride] = useState<AccountOverrideResponse | null>(null);
  const [draftRoleTemplateId, setDraftRoleTemplateId] = useState<number | null>(null);
  const [draftAddKeys, setDraftAddKeys] = useState<Set<string>>(new Set());
  const [draftRemoveKeys, setDraftRemoveKeys] = useState<Set<string>>(new Set());

  // --- Role-Vision (test-only) ---
  const [testUtilitiesAvailable, setTestUtilitiesAvailable] = useState(false);
  const [visionProbeDone, setVisionProbeDone] = useState(false);
  const [visionApplying, setVisionApplying] = useState(false);
  const [visionUserSearchResults, setVisionUserSearchResults] = useState<UserSummary[]>([]);
  const [visionUserSearchQuery, setVisionUserSearchQuery] = useState('');
  const [visionUserSearchLoading, setVisionUserSearchLoading] = useState(false);
  const [kitAccounts, setKitAccounts] = useState<KitAccountSummary[]>([]);
  const [kitResetLoading, setKitResetLoading] = useState(false);
  const visionState = useVisionStore((s) => s.vision);
  const setVision = useVisionStore((s) => s.setVision);

  const loadTreeAndRoles = useCallback(() => {
    setState('loading');
    Promise.all([fetchPermissionTree(), fetchRoles(enabledOnly)])
      .then(([tree, roleList]) => {
        setTreeData(tree);
        setRoles(roleList);
        setState('success');
      })
      .catch((err: { response?: { status: number } }) => {
        if (err.response?.status === 403) setState('forbidden');
        else {
          setState('error');
          message.error('加载失败，请重试');
        }
      });
  }, [enabledOnly]);

  useEffect(() => {
    loadTreeAndRoles();
  }, [loadTreeAndRoles]);

  // Probe test utilities (Role-Vision) on mount
  useEffect(() => {
    getTestVision()
      .then((res) => {
        setTestUtilitiesAvailable(true);
        setVision(res.mode === 'USER' && res.userId != null ? { mode: 'USER', userId: res.userId } : { mode: 'OFF' });
      })
      .catch(() => setTestUtilitiesAvailable(false))
      .finally(() => setVisionProbeDone(true));
  }, []);

  useEffect(() => {
    if (selectedRoleId == null) {
      setSavedLabel('');
      setDraftLabel('');
      setSavedEnabled(true);
      setDraftEnabled(true);
      setSavedPermissions(new Set());
      setDraftPermissions(new Set());
      return;
    }
    const role = roles.find((r) => r.id === selectedRoleId);
    const label = role?.label ?? '';
    const enabled = role?.enabled !== false;
    setSavedLabel(label);
    setDraftLabel(label);
    setSavedEnabled(enabled);
    setDraftEnabled(enabled);
    fetchRolePermissions(selectedRoleId)
      .then((keys) => {
        const set = new Set(keys);
        setSavedPermissions(set);
        setDraftPermissions(new Set(set));
      })
      .catch((err: { response?: { data?: { machineCode?: string } } }) => {
        if (err.response?.data?.machineCode === 'PERM_ROLE_NOT_FOUND') {
          message.error(getErrorMessage(err));
          loadTreeAndRoles();
          setSelectedRoleId(null);
        } else {
          message.error('加载角色权限失败');
        }
      });
  }, [selectedRoleId]);

  useEffect(() => {
    if (selectedRoleId == null) return;
    const role = roles.find((r) => r.id === selectedRoleId);
    if (role) {
      setSavedLabel(role.label);
      setDraftLabel(role.label);
      setSavedEnabled(role.enabled !== false);
      setDraftEnabled(role.enabled !== false);
    }
  }, [roles, selectedRoleId]);

  const handleCreateRole = (values: { roleKey: string; label: string }) => {
    createRole(values.roleKey.trim(), values.label?.trim() || values.roleKey.trim())
      .then((newRole) => {
        setRoles((prev) => [...prev, newRole]);
        setCreateModalOpen(false);
        message.success('角色已创建');
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const handleUpdateRole = (id: number, label: string) => {
    updateRole(id, label)
      .then((updated) => {
        setRoles((prev) => prev.map((r) => (r.id === id ? { ...updated, enabled: updated.enabled !== false } : r)));
        setEditingRole(null);
        message.success('已更新');
      })
      .catch((err: { response?: { data?: { machineCode?: string } } }) => {
        message.error(getErrorMessage(err));
        if (err.response?.data?.machineCode === 'PERM_ROLE_NOT_FOUND') loadTreeAndRoles();
      });
  };

  const handleDeleteRole = (id: number) => {
    deleteRole(id)
      .then(() => {
        setRoles((prev) => prev.filter((r) => r.id !== id));
        if (selectedRoleId === id) setSelectedRoleId(null);
        message.success('已删除');
      })
      .catch((err: { response?: { data?: { machineCode?: string } } }) => {
        message.error(getErrorMessage(err));
        if (err.response?.data?.machineCode === 'PERM_ROLE_NOT_FOUND') loadTreeAndRoles();
      });
  };

  const toggleDraft = (key: string) => {
    setDraftPermissions((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  };

  const handleSave = () => {
    if (selectedRoleId == null) return;
    setSaveLoading(true);
    saveRoleTemplate(selectedRoleId, draftLabel.trim() || draftLabel, draftEnabled, Array.from(draftPermissions))
      .then((updated) => {
        setRoles((prev) =>
          prev.map((r) => (r.id === selectedRoleId ? { ...r, label: updated.label, enabled: updated.enabled !== false } : r)),
        );
        setSavedLabel(draftLabel);
        setDraftLabel(draftLabel);
        setSavedEnabled(draftEnabled);
        setDraftEnabled(draftEnabled);
        setSavedPermissions(new Set(draftPermissions));
        setDraftPermissions(new Set(draftPermissions));
        message.success('已保存');
      })
      .catch((err: { response?: { data?: { machineCode?: string } } }) => {
        message.error(getErrorMessage(err));
        if (err.response?.data?.machineCode === 'PERM_ROLE_NOT_FOUND') {
          loadTreeAndRoles();
          setSelectedRoleId(null);
        }
      })
      .finally(() => setSaveLoading(false));
  };

  const handleReset = () => {
    setDraftLabel(savedLabel);
    setDraftEnabled(savedEnabled);
    setDraftPermissions(new Set(savedPermissions));
    message.info('已还原为上次保存的状态');
  };

  const isDirty =
    selectedRoleId != null &&
    (draftLabel !== savedLabel ||
      draftEnabled !== savedEnabled ||
      draftPermissions.size !== savedPermissions.size ||
      Array.from(draftPermissions).some((k) => !savedPermissions.has(k)) ||
      Array.from(savedPermissions).some((k) => !draftPermissions.has(k)));

  // Load roles for account-override template dropdown when tab is account
  useEffect(() => {
    if (activeTab === 'account') {
      fetchRoles(true).then(setRolesForAccount).catch(() => setRolesForAccount([]));
    }
  }, [activeTab]);

  // When selected user changes, load override
  useEffect(() => {
    if (selectedUser == null) {
      setSavedOverride(null);
      setDraftRoleTemplateId(null);
      setDraftAddKeys(new Set());
      setDraftRemoveKeys(new Set());
      setTemplateKeys(new Set());
      return;
    }
    setOverrideLoading(true);
    getAccountOverride(selectedUser.id)
      .then((res) => {
        setSavedOverride(res);
        setDraftRoleTemplateId(res.roleTemplateId ?? null);
        setDraftAddKeys(new Set(res.addKeys ?? []));
        setDraftRemoveKeys(new Set(res.removeKeys ?? []));
      })
      .catch((err: { response?: { data?: { machineCode?: string } } }) => {
        message.error(getErrorMessage(err));
        if (err.response?.data?.machineCode === 'PERM_USER_NOT_FOUND') {
          setSelectedUser(null);
          setUserSearchResults([]);
        }
      })
      .finally(() => setOverrideLoading(false));
  }, [selectedUser?.id]);

  // Load template keys when draft template changes (for effective preview)
  useEffect(() => {
    if (draftRoleTemplateId == null) {
      setTemplateKeys(new Set());
      return;
    }
    fetchRolePermissions(draftRoleTemplateId)
      .then((keys) => setTemplateKeys(new Set(keys)))
      .catch(() => setTemplateKeys(new Set()));
  }, [draftRoleTemplateId]);

  const effectivePreview = new Set<string>(
    [...templateKeys, ...draftAddKeys].filter((k) => !draftRemoveKeys.has(k)),
  );
  const savedAddSet = new Set(savedOverride?.addKeys ?? []);
  const savedRemoveSet = new Set(savedOverride?.removeKeys ?? []);
  const overrideDirty =
    selectedUser != null &&
    savedOverride != null &&
    (draftRoleTemplateId !== (savedOverride.roleTemplateId ?? null) ||
      draftAddKeys.size !== savedAddSet.size ||
      draftRemoveKeys.size !== savedRemoveSet.size ||
      [...draftAddKeys].some((k) => !savedAddSet.has(k)) ||
      [...draftRemoveKeys].some((k) => !savedRemoveSet.has(k)));

  const handleUserSearch = (q: string) => {
    setUserSearchQuery(q);
    if (!q.trim()) {
      setUserSearchResults([]);
      return;
    }
    setUserSearchLoading(true);
    searchUsers(q.trim())
      .then(setUserSearchResults)
      .catch(() => setUserSearchResults([]))
      .finally(() => setUserSearchLoading(false));
  };

  const setOverrideKeyState = (key: string, state: 'none' | 'add' | 'remove') => {
    setDraftAddKeys((prev) => {
      const next = new Set(prev);
      if (state === 'add') next.add(key);
      else next.delete(key);
      return next;
    });
    setDraftRemoveKeys((prev) => {
      const next = new Set(prev);
      if (state === 'remove') next.add(key);
      else next.delete(key);
      return next;
    });
  };

  const handleSaveOverride = () => {
    if (selectedUser == null) return;
    setOverrideSaveLoading(true);
    saveAccountOverride(selectedUser.id, {
      roleTemplateId: draftRoleTemplateId,
      addKeys: Array.from(draftAddKeys),
      removeKeys: Array.from(draftRemoveKeys),
    })
      .then((res) => {
        setSavedOverride(res);
        setDraftRoleTemplateId(res.roleTemplateId ?? null);
        setDraftAddKeys(new Set(res.addKeys ?? []));
        setDraftRemoveKeys(new Set(res.removeKeys ?? []));
        setTemplateKeys(new Set());
        if (res.roleTemplateId != null) {
          fetchRolePermissions(res.roleTemplateId).then((keys) => setTemplateKeys(new Set(keys)));
        }
        message.success('已保存');
      })
      .catch((err: { response?: { data?: { machineCode?: string } } }) => {
        message.error(getErrorMessage(err));
        if (err.response?.data?.machineCode === 'PERM_USER_NOT_FOUND') {
          setSelectedUser(null);
        }
      })
      .finally(() => setOverrideSaveLoading(false));
  };

  const handleResetOverride = () => {
    if (savedOverride == null) return;
    setDraftRoleTemplateId(savedOverride.roleTemplateId ?? null);
    setDraftAddKeys(new Set(savedOverride.addKeys ?? []));
    setDraftRemoveKeys(new Set(savedOverride.removeKeys ?? []));
    message.info('已还原为上次保存的状态');
  };

  if (state === 'loading') {
    return (
      <div style={{ padding: 24 }}>
        <h2>角色与权限</h2>
        <p>加载中...</p>
      </div>
    );
  }
  if (state === 'forbidden') {
    return (
      <div style={{ padding: 24 }}>
        <h2>角色与权限</h2>
        <p>无权限</p>
      </div>
    );
  }
  if (state === 'error' || !treeData) {
    return (
      <div style={{ padding: 24 }}>
        <h2>角色与权限</h2>
        <p>加载失败，请重试</p>
      </div>
    );
  }

  const actionColumns: ColumnsType<ActionItem> = [
    { title: '动作 key', dataIndex: 'key', key: 'key', width: 140 },
    { title: '中文名', dataIndex: 'label', key: 'label' },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h2>角色与权限</h2>

      <section style={{ marginTop: 24 }}>
        <h3>默认动作（v1）</h3>
        <Table<ActionItem>
          rowKey="key"
          size="small"
          pagination={false}
          columns={actionColumns}
          dataSource={treeData.actions}
        />
      </section>

      <section style={{ marginTop: 32 }}>
        <h3>权限树（模块 → 动作）</h3>
        <p style={{ color: '#666', fontSize: 12 }}>keyFormat: {treeData.keyFormat}</p>
        <div style={{ marginTop: 12 }}>
          {treeData.modules.length === 0 ? (
            <p style={{ color: '#999' }}>暂无模块</p>
          ) : (
            treeData.modules.map((mod) => (
              <ModuleNodeBlock key={mod.key} node={mod} />
            ))
          )}
        </div>
      </section>

      {visionProbeDone && testUtilitiesAvailable && (
        <section style={{ marginTop: 24, padding: 12, border: '1px dashed #faad14', borderRadius: 8, background: '#fffbe6' }}>
          <h3 style={{ marginTop: 0 }}>Role-Vision（测试）</h3>
          {visionState?.mode === 'USER' && visionState.userId != null && (
            <p style={{ color: '#d48806', marginBottom: 12 }}>
              当前以用户 #{visionState.userId} 的权限查看菜单与按钮；后端接口仍按登录用户鉴权。
            </p>
          )}
          <Space wrap align="center">
            <span style={{ fontSize: 12 }}>视角：</span>
            <Select
              style={{ width: 120 }}
              value={visionState?.mode ?? 'OFF'}
              options={[
                { label: '关闭', value: 'OFF' },
                { label: '按用户', value: 'USER' },
              ]}
              onChange={(val) => setVision(val === 'USER' ? { mode: 'USER', userId: visionState?.userId } : { mode: 'OFF' })}
            />
            {visionState?.mode === 'USER' && (
              <Select
                placeholder="搜索用户"
                showSearch
                filterOption={false}
                onSearch={(q) => {
                  setVisionUserSearchQuery(q);
                  if (!q.trim()) {
                    setVisionUserSearchResults([]);
                    return;
                  }
                  setVisionUserSearchLoading(true);
                  searchUsers(q)
                    .then(setVisionUserSearchResults)
                    .finally(() => setVisionUserSearchLoading(false));
                }}
                onSelect={(id: number) => {
                  const u = visionUserSearchResults.find((r) => r.id === id);
                  if (u) setVision({ mode: 'USER', userId: u.id });
                }}
                loading={visionUserSearchLoading}
                style={{ width: 260 }}
                value={visionState?.userId ?? undefined}
                notFoundContent={visionUserSearchQuery.trim() ? '无结果' : '输入关键词搜索'}
                options={visionUserSearchResults.map((u) => ({
                  value: u.id,
                  label: `${u.username}${u.nickname ? ` (${u.nickname})` : ''} #${u.id}`,
                }))}
                allowClear
                onClear={() => setVision({ mode: 'USER' })}
              />
            )}
            <Button
              type="primary"
              size="small"
              loading={visionApplying}
              onClick={() => {
                const mode = visionState?.mode ?? 'OFF';
                const userId = mode === 'USER' ? visionState?.userId : undefined;
                if (mode === 'USER' && userId == null) {
                  message.warning('请选择要查看的用户');
                  return;
                }
                setVisionApplying(true);
                setTestVision({ mode, userId })
                  .then((res) => {
                    setVision(res.mode === 'USER' && res.userId != null ? { mode: 'USER', userId: res.userId } : { mode: 'OFF' });
                    message.success('已应用');
                  })
                  .catch(() => message.error('设置失败'))
                  .finally(() => setVisionApplying(false));
              }}
            >
              应用
            </Button>
          </Space>
          <div style={{ marginTop: 16 }}>
            <Button
              size="small"
              loading={kitResetLoading}
              onClick={() => {
                setKitResetLoading(true);
                resetTestAccounts()
                  .then((list) => {
                    setKitAccounts(list);
                    message.success(`已生成/重置 ${list.length} 个测试账号`);
                  })
                  .catch(() => message.error('重置失败'))
                  .finally(() => setKitResetLoading(false));
              }}
            >
              生成/重置测试账号（4个）
            </Button>
            {kitAccounts.length > 0 && (
              <div style={{ marginTop: 8, fontSize: 12 }}>
                {kitAccounts.map((a) => (
                  <div key={a.username}>
                    {a.username}（{a.nickname}）— id: {a.userId}，有效权限数: {a.effectiveKeyCount}
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>
      )}

      <section style={{ marginTop: 32 }}>
        <h3>角色与权限绑定</h3>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'templates',
              label: '角色模板',
              children: (
                <Row gutter={24} style={{ marginTop: 12 }}>
                  <Col span={8}>
            <div style={{ border: '1px solid #eee', borderRadius: 4, padding: 12 }}>
              <Space style={{ marginBottom: 8 }} wrap>
                <Button type="primary" size="small" onClick={() => setCreateModalOpen(true)}>
                  新建角色
                </Button>
                <Checkbox
                  checked={!enabledOnly}
                  onChange={(e) => setEnabledOnly(!e.target.checked)}
                >
                  显示全部（含停用）
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
                        <div style={{ fontSize: 12, color: '#666' }}>{r.roleKey}</div>
                        <Tag color={r.enabled !== false ? 'green' : 'default'} style={{ marginTop: 4 }}>
                          {r.enabled !== false ? '已启用' : '已停用'}
                        </Tag>
                      </div>
                    ),
                  },
                  {
                    title: '操作',
                    key: 'actions',
                    width: 100,
                    render: (_, r) => (
                      <Space size="small">
                        <Button
                          type="link"
                          size="small"
                          onClick={() => setEditingRole(r)}
                        >
                          编辑
                        </Button>
                        <Popconfirm
                          title="确定删除该角色？"
                          onConfirm={() => handleDeleteRole(r.id)}
                        >
                          <Button type="link" size="small" danger>
                            删除
                          </Button>
                        </Popconfirm>
                      </Space>
                    ),
                  },
                ]}
                onRow={(r) => ({
                  onClick: () => setSelectedRoleId(r.id),
                  style: {
                    cursor: 'pointer',
                    background: selectedRoleId === r.id ? '#e6f7ff' : undefined,
                  },
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
                    <Button
                      type="primary"
                      onClick={handleSave}
                      loading={saveLoading}
                      disabled={!isDirty}
                    >
                      保存
                    </Button>
                    <Button onClick={handleReset} disabled={!isDirty}>
                      还原
                    </Button>
                  </Space>
                  <div style={{ marginBottom: 12 }}>
                    <Space align="center" style={{ marginBottom: 8 }}>
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
                  <p style={{ fontSize: 12, color: '#666' }}>
                    勾选即加入该角色权限（仅点击「保存」后生效）
                  </p>
                  {treeData.modules.length === 0 ? (
                    <p style={{ color: '#999' }}>暂无模块，无法配置权限</p>
                  ) : (
                    <div style={{ marginTop: 8 }}>
                      {treeData.modules.map((mod) => (
                        <PermissionModuleBlock
                          key={mod.key}
                          node={mod}
                          draft={draftPermissions}
                          onToggle={toggleDraft}
                        />
                      ))}
                    </div>
                  )}
                </>
              )}
            </div>
          </Col>
        </Row>
              ),
            },
            {
              key: 'account',
              label: '按账号覆盖',
              children: (
                <div style={{ marginTop: 12 }}>
                  <div style={{ marginBottom: 12 }}>
                    <Space wrap align="center">
                      <span style={{ fontSize: 12 }}>搜索账号：</span>
                      <Select
                        placeholder="输入用户名或昵称搜索"
                        showSearch
                        filterOption={false}
                        onSearch={handleUserSearch}
                        onSelect={(id: number) => {
                          const u = userSearchResults.find((r) => r.id === id) ?? selectedUser;
                          if (u) setSelectedUser(u);
                        }}
                        loading={userSearchLoading}
                        style={{ width: 280 }}
                        value={selectedUser?.id ?? undefined}
                        notFoundContent={userSearchQuery.trim() ? '无结果' : '输入关键词搜索'}
                        options={[
                          ...(selectedUser && !userSearchResults.some((r) => r.id === selectedUser.id)
                            ? [{ id: selectedUser.id, username: selectedUser.username, nickname: selectedUser.nickname }]
                            : []),
                          ...userSearchResults,
                        ].map((u) => ({
                          value: u.id,
                          label: `${u.username}${u.nickname ? ` (${u.nickname})` : ''} #${u.id}`,
                        }))}
                        allowClear
                        onClear={() => {
                          setSelectedUser(null);
                          setUserSearchQuery('');
                          setUserSearchResults([]);
                        }}
                      />
                    </Space>
                  </div>
                  {selectedUser == null ? (
                    <p style={{ color: '#999' }}>请先搜索并选择一个账号</p>
                  ) : overrideLoading ? (
                    <p>加载中...</p>
                  ) : (
                    <>
                      <p style={{ marginBottom: 8 }}>
                        账号：<strong>{selectedUser.username}</strong>
                        {selectedUser.nickname ? `（${selectedUser.nickname}）` : ''} ID: {selectedUser.id}
                      </p>
                      <Space style={{ marginBottom: 12 }} wrap>
                        <Button
                          type="primary"
                          onClick={handleSaveOverride}
                          loading={overrideSaveLoading}
                          disabled={!overrideDirty}
                        >
                          保存
                        </Button>
                        <Button onClick={handleResetOverride} disabled={!overrideDirty}>
                          还原
                        </Button>
                      </Space>
                      <div style={{ marginBottom: 16 }}>
                        <span style={{ fontSize: 12, marginRight: 8 }}>分配角色模板：</span>
                        <Select
                          placeholder="选择模板（仅已启用）"
                          style={{ width: 220 }}
                          value={draftRoleTemplateId ?? undefined}
                          onChange={(v) => setDraftRoleTemplateId(v ?? null)}
                          allowClear
                          options={rolesForAccount.map((r) => ({ value: r.id, label: `${r.label} (${r.roleKey})` }))}
                        />
                      </div>
                      <p style={{ fontSize: 12, color: '#666', marginBottom: 8 }}>
                        每个权限：默认（继承模板）/ 添加 / 移除（移除优先）
                      </p>
                      {treeData.modules.length === 0 ? (
                        <p style={{ color: '#999' }}>暂无模块</p>
                      ) : (
                        <div style={{ marginBottom: 16 }}>
                          {treeData.modules.map((mod) => (
                            <OverridePermissionModuleBlock
                              key={mod.key}
                              node={mod}
                              draftAdd={draftAddKeys}
                              draftRemove={draftRemoveKeys}
                              onSetState={setOverrideKeyState}
                            />
                          ))}
                        </div>
                      )}
                      <div>
                        <span style={{ fontSize: 12, fontWeight: 500 }}>当前有效权限预览（模板 + 添加 - 移除）：</span>
                        <div style={{ marginTop: 4, maxHeight: 120, overflow: 'auto', padding: 8, background: '#fafafa', borderRadius: 4 }}>
                          {Array.from(effectivePreview).length === 0 ? (
                            <span style={{ color: '#999' }}>无</span>
                          ) : (
                            Array.from(effectivePreview)
                              .sort()
                              .map((k) => (
                                <Tag key={k} style={{ margin: 2 }}>
                                  {k}
                                </Tag>
                              ))
                          )}
                        </div>
                      </div>
                    </>
                  )}
                </div>
              ),
            },
          ]}
        />
      </section>

      <Modal
        title="新建角色"
        open={createModalOpen}
        onCancel={() => setCreateModalOpen(false)}
        footer={null}
      >
        <Form layout="vertical" onFinish={handleCreateRole}>
          <Form.Item name="roleKey" label="角色 key" rules={[{ required: true, message: '请输入 roleKey' }]}>
            <Input placeholder="例如：PLATFORM_EDITOR" />
          </Form.Item>
          <Form.Item name="label" label="显示名称">
            <Input placeholder="例如：平台编辑" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                创建
              </Button>
              <Button onClick={() => setCreateModalOpen(false)}>取消</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="编辑角色"
        open={editingRole != null}
        onCancel={() => setEditingRole(null)}
        footer={null}
      >
        {editingRole && (
          <Form
            layout="vertical"
            initialValues={{ label: editingRole.label }}
            onFinish={(v) => {
              handleUpdateRole(editingRole.id, v.label);
            }}
          >
            <Form.Item name="label" label="显示名称" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit">
                  保存
                </Button>
                <Button onClick={() => setEditingRole(null)}>取消</Button>
              </Space>
            </Form.Item>
          </Form>
        )}
      </Modal>
    </div>
  );
}

function ModuleNodeBlock({ node }: { node: ModuleNode }) {
  return (
    <div style={{ marginBottom: 16, padding: 12, border: '1px solid #eee', borderRadius: 4 }}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>
        {node.label}（{node.key}）
      </div>
      <div>
        {node.actions.map((actionKey) => (
          <Tag key={actionKey} style={{ marginBottom: 4 }}>
            {actionKey}
          </Tag>
        ))}
      </div>
      {node.children?.length > 0 &&
        node.children.map((child) => <ModuleNodeBlock key={child.key} node={child} />)}
    </div>
  );
}

function PermissionModuleBlock({
  node,
  draft,
  onToggle,
}: {
  node: ModuleNode;
  draft: Set<string>;
  onToggle: (key: string) => void;
}) {
  return (
    <div style={{ marginBottom: 16, padding: 12, border: '1px solid #eee', borderRadius: 4 }}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>
        {node.label}（{node.key}）
      </div>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
        {node.actions.map((actionKey) => {
          const key = `${node.key}:${actionKey}`;
          return (
            <Checkbox
              key={key}
              checked={draft.has(key)}
              onChange={() => onToggle(key)}
            >
              {actionKey}
            </Checkbox>
          );
        })}
      </div>
      {node.children?.length > 0 &&
        node.children.map((child) => (
          <div key={child.key} style={{ marginLeft: 16, marginTop: 8 }}>
            <PermissionModuleBlock node={child} draft={draft} onToggle={onToggle} />
          </div>
        ))}
    </div>
  );
}

function OverridePermissionModuleBlock({
  node,
  draftAdd,
  draftRemove,
  onSetState,
}: {
  node: ModuleNode;
  draftAdd: Set<string>;
  draftRemove: Set<string>;
  onSetState: (key: string, state: 'none' | 'add' | 'remove') => void;
}) {
  return (
    <div style={{ marginBottom: 16, padding: 12, border: '1px solid #eee', borderRadius: 4 }}>
      <div style={{ fontWeight: 600, marginBottom: 8 }}>
        {node.label}（{node.key}）
      </div>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center' }}>
        {node.actions.map((actionKey) => {
          const key = `${node.key}:${actionKey}`;
          const state = draftRemove.has(key) ? 'remove' : draftAdd.has(key) ? 'add' : 'none';
          return (
            <Space key={key} size={4}>
              <span style={{ fontSize: 12 }}>{actionKey}:</span>
              <Select
                size="small"
                value={state}
                onChange={(v) => onSetState(key, v as 'none' | 'add' | 'remove')}
                options={[
                  { value: 'none', label: '默认' },
                  { value: 'add', label: '添加' },
                  { value: 'remove', label: '移除', title: '移除优先' },
                ]}
                style={{ width: 72 }}
                status={state === 'remove' ? 'error' : undefined}
              />
            </Space>
          );
        })}
      </div>
      {node.children?.length > 0 &&
        node.children.map((child) => (
          <div key={child.key} style={{ marginLeft: 16, marginTop: 8 }}>
            <OverridePermissionModuleBlock node={child} draftAdd={draftAdd} draftRemove={draftRemove} onSetState={onSetState} />
          </div>
        ))}
    </div>
  );
}
