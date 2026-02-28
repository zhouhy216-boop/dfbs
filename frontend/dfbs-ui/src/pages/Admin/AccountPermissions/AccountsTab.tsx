/**
 * Accounts tab: search/select account, create account by binding person, enable/disable, reset password, template + override.
 * Uses /api/v1/admin/account-permissions/*.
 */
import { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Collapse, Drawer, Input, message, Modal, Select, Space, Switch, Table, Tag } from 'antd';
import {
  createAccount,
  getAccountOverride,
  getPeopleOptions,
  getRolePermissions,
  getRoles,
  getUser,
  saveAccountOverride,
  searchUsers,
  setAccountEnabled,
  resetPassword as apiResetPassword,
  type AccountOverrideResponse,
  type PersonOptionForBinding,
  type RoleResponse,
  type UserSummary,
} from './acctPermService';
import { fetchPermissionTree, type ModuleNode } from '../RolesPermissions/permService';
import { ModuleOverviewCards } from './ModuleOverviewCards';
import { PermissionMenuTree } from './PermissionMenuTree';
import { PermissionKeyPicker } from './PermissionKeyPicker';
import {
  buildModuleLabelMap,
  collectSubtreeKeys,
  getKeysForOp,
  groupKeysByModule,
  type ModuleNodeLike,
  type QuickOp,
} from './permissionQuickOps';

function getErrorMessage(err: { response?: { data?: { message?: string; machineCode?: string } } }): string {
  const data = err.response?.data;
  const code = data?.machineCode;
  if (code === 'PERM_FORBIDDEN') return '无权限';
  return data?.message ?? '操作失败，请重试';
}

function getEffectiveHasKey(
  key: string,
  templateKeys: Set<string>,
  draftAdd: Set<string>,
  draftRemove: Set<string>,
): boolean {
  return (templateKeys.has(key) || draftAdd.has(key)) && !draftRemove.has(key);
}

export default function AccountsTab() {
  const [userSearchQuery, setUserSearchQuery] = useState('');
  const [userSearchResults, setUserSearchResults] = useState<UserSummary[]>([]);
  const [userSearchLoading, setUserSearchLoading] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserSummary | null>(null);
  const [overrideLoading, setOverrideLoading] = useState(false);
  const [overrideSaveLoading, setOverrideSaveLoading] = useState(false);
  const [savedOverride, setSavedOverride] = useState<AccountOverrideResponse | null>(null);
  const [draftRoleTemplateId, setDraftRoleTemplateId] = useState<number | null>(null);
  const [draftAddKeys, setDraftAddKeys] = useState<Set<string>>(new Set());
  const [draftRemoveKeys, setDraftRemoveKeys] = useState<Set<string>>(new Set());
  const [roles, setRoles] = useState<RoleResponse[]>([]);
  const [rolesEnabledOnly, setRolesEnabledOnly] = useState(true);
  const [templateKeys, setTemplateKeys] = useState<Set<string>>(new Set());
  const [treeData, setTreeData] = useState<{ modules: ModuleNode[] } | null>(null);
  const [treeUnavailable, setTreeUnavailable] = useState(false);
  // Create account modal
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [createPersonQuery, setCreatePersonQuery] = useState('');
  const [createPersonOptions, setCreatePersonOptions] = useState<PersonOptionForBinding[]>([]);
  const [createPersonLoading, setCreatePersonLoading] = useState(false);
  const [createSelectedPersonId, setCreateSelectedPersonId] = useState<number | null>(null);
  const [createUsername, setCreateUsername] = useState('');
  const [createNickname, setCreateNickname] = useState('');
  const [createRoleTemplateId, setCreateRoleTemplateId] = useState<number | null>(null);
  const [createSubmitting, setCreateSubmitting] = useState(false);
  // Reset password modal
  const [resetPwdModalOpen, setResetPwdModalOpen] = useState(false);
  const [resetPwdValue, setResetPwdValue] = useState('');
  const [resetPwdSubmitting, setResetPwdSubmitting] = useState(false);
  // Override diff UX: add/remove picker modals, effective drawer, advanced editor collapsed
  const [addPickerOpen, setAddPickerOpen] = useState(false);
  const [removePickerOpen, setRemovePickerOpen] = useState(false);
  const [addPickerSelected, setAddPickerSelected] = useState<Set<string>>(new Set());
  const [removePickerSelected, setRemovePickerSelected] = useState<Set<string>>(new Set());
  const [effectiveDrawerOpen, setEffectiveDrawerOpen] = useState(false);
  const [effectiveSearch, setEffectiveSearch] = useState('');
  const [advancedEditorOpen, setAdvancedEditorOpen] = useState(false);
  const [accountDetailDrawerOpen, setAccountDetailDrawerOpen] = useState(false);

  const loadRoles = useCallback((enabledOnly: boolean) => {
    getRoles(enabledOnly).then(setRoles).catch(() => setRoles([]));
  }, []);

  useEffect(() => {
    loadRoles(rolesEnabledOnly);
  }, [rolesEnabledOnly, loadRoles]);

  useEffect(() => {
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
  }, []);

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
      .catch((err) => {
        message.error(getErrorMessage(err));
        if (err.response?.data?.machineCode === 'PERM_FORBIDDEN') {
          message.error('无权限');
        }
        if (err.response?.data?.machineCode === 'PERM_USER_NOT_FOUND') {
          setSelectedUser(null);
          setUserSearchResults([]);
        }
      })
      .finally(() => setOverrideLoading(false));
  }, [selectedUser?.id]);

  useEffect(() => {
    if (draftRoleTemplateId == null) {
      setTemplateKeys(new Set());
      return;
    }
    getRolePermissions(draftRoleTemplateId)
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
      .catch((err) => {
        setUserSearchResults([]);
        message.error(getErrorMessage(err));
      })
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

  const handleQuickOpOverride = (node: ModuleNodeLike, op: QuickOp) => {
    const subKeys = collectSubtreeKeys(node);
    const toSet = getKeysForOp(node, op);
    setDraftRemoveKeys((prev) => {
      const next = new Set(prev);
      subKeys.forEach((k) => (toSet.includes(k) ? next.delete(k) : next.add(k)));
      return next;
    });
    setDraftAddKeys((prev) => {
      const next = new Set(prev);
      subKeys.forEach((k) => (toSet.includes(k) ? next.add(k) : next.delete(k)));
      return next;
    });
  };

  const getMenuCheckedOverride = (node: ModuleNodeLike) =>
    getEffectiveHasKey(`${node.key}:VIEW`, templateKeys, draftAddKeys, draftRemoveKeys);
  const onMenuCheckOverride = (node: ModuleNodeLike, checked: boolean) => {
    const subKeys = collectSubtreeKeys(node);
    const viewKeys = getKeysForOp(node, 'readonly');
    if (checked) {
      setDraftRemoveKeys((prev) => {
        const next = new Set(prev);
        viewKeys.forEach((k) => next.delete(k));
        return next;
      });
      setDraftAddKeys((prev) => {
        const next = new Set(prev);
        viewKeys.forEach((k) => next.add(k));
        subKeys.forEach((k) => {
          if (!viewKeys.includes(k)) next.delete(k);
        });
        return next;
      });
    } else {
      setDraftRemoveKeys((prev) => {
        const next = new Set(prev);
        subKeys.forEach((k) => next.add(k));
        return next;
      });
      setDraftAddKeys((prev) => {
        const next = new Set(prev);
        subKeys.forEach((k) => next.delete(k));
        return next;
      });
    }
  };

  const handleSave = () => {
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
          getRolePermissions(res.roleTemplateId).then((keys) => setTemplateKeys(new Set(keys)));
        }
        message.success('已保存');
      })
      .catch((err) => {
        message.error(getErrorMessage(err));
        if (err.response?.data?.machineCode === 'PERM_FORBIDDEN') {
          message.error('无权限');
        }
      })
      .finally(() => setOverrideSaveLoading(false));
  };

  const handleReset = () => {
    if (savedOverride == null) return;
    setDraftRoleTemplateId(savedOverride.roleTemplateId ?? null);
    setDraftAddKeys(new Set(savedOverride.addKeys ?? []));
    setDraftRemoveKeys(new Set(savedOverride.removeKeys ?? []));
    message.info('已还原为上次保存的状态');
  };

  const moduleLabelMap = useMemo(
    () => buildModuleLabelMap(treeData?.modules ?? []),
    [treeData?.modules],
  );
  const templateLabel =
    draftRoleTemplateId != null ? roles.find((r) => r.id === draftRoleTemplateId)?.label ?? `模板 #${draftRoleTemplateId}` : '未选模板';

  const handleAddPickerOk = () => {
    const keys = Array.from(addPickerSelected);
    if (keys.length > 0) {
      setDraftAddKeys((prev) => new Set([...prev, ...keys]));
      setDraftRemoveKeys((prev) => {
        const next = new Set(prev);
        keys.forEach((k) => next.delete(k));
        return next;
      });
    }
    setAddPickerOpen(false);
    setAddPickerSelected(new Set());
  };

  const handleRemovePickerOk = () => {
    const keys = Array.from(removePickerSelected);
    if (keys.length > 0) {
      setDraftRemoveKeys((prev) => new Set([...prev, ...keys]));
      setDraftAddKeys((prev) => {
        const next = new Set(prev);
        keys.forEach((k) => next.delete(k));
        return next;
      });
    }
    setRemovePickerOpen(false);
    setRemovePickerSelected(new Set());
  };

  const addGroups = useMemo(() => groupKeysByModule(Array.from(draftAddKeys), moduleLabelMap), [draftAddKeys, moduleLabelMap]);
  const removeGroups = useMemo(() => groupKeysByModule(Array.from(draftRemoveKeys), moduleLabelMap), [draftRemoveKeys, moduleLabelMap]);
  const effectiveFiltered = useMemo(() => {
    const list = Array.from(effectivePreview).sort();
    if (!effectiveSearch.trim()) return list;
    const q = effectiveSearch.trim().toLowerCase();
    return list.filter((k) => k.toLowerCase().includes(q));
  }, [effectivePreview, effectiveSearch]);
  const effectiveGrouped = useMemo(
    () => groupKeysByModule(effectiveFiltered, moduleLabelMap),
    [effectiveFiltered, moduleLabelMap],
  );

  // Create account: load person options when modal open and query changes
  useEffect(() => {
    if (!createModalOpen) return;
    setCreatePersonLoading(true);
    getPeopleOptions(createPersonQuery.trim())
      .then(setCreatePersonOptions)
      .catch(() => setCreatePersonOptions([]))
      .finally(() => setCreatePersonLoading(false));
  }, [createModalOpen, createPersonQuery]);

  const handleCreateAccountOpen = () => {
    setCreateModalOpen(true);
    setCreatePersonQuery('');
    setCreateSelectedPersonId(null);
    setCreateUsername('');
    setCreateNickname('');
    setCreateRoleTemplateId(null);
  };

  const handleCreateAccountSubmit = () => {
    if (createSelectedPersonId == null) {
      message.warning('请选择人员');
      return;
    }
    if (!createUsername.trim()) {
      message.warning('请输入用户名');
      return;
    }
    setCreateSubmitting(true);
    createAccount({
      orgPersonId: createSelectedPersonId,
      username: createUsername.trim(),
      nickname: createNickname.trim() || undefined,
      roleTemplateId: createRoleTemplateId ?? undefined,
    })
      .then((res) => {
        message.success('账号已创建');
        setCreateModalOpen(false);
        const newUser: UserSummary = {
          id: res.id,
          username: res.username,
          nickname: res.nickname ?? undefined,
          enabled: res.enabled ?? true,
        };
        setSelectedUser(newUser);
        setUserSearchResults((prev) =>
          prev.some((u) => u.id === res.id) ? prev : [newUser, ...prev],
        );
        setUserSearchQuery(res.username);
        getAccountOverride(res.id).then((overrideRes) => {
          setSavedOverride(overrideRes);
          setDraftRoleTemplateId(overrideRes.roleTemplateId ?? null);
          setDraftAddKeys(new Set(overrideRes.addKeys ?? []));
          setDraftRemoveKeys(new Set(overrideRes.removeKeys ?? []));
        }).catch(() => {});
      })
      .catch((err: { response?: { data?: { message?: string; machineCode?: string } } }) => {
        const code = err.response?.data?.machineCode;
        const msg = err.response?.data?.message ?? '创建失败';
        if (code === 'ACCTPERM_PERSON_ALREADY_BOUND') {
          message.error('该人员已绑定账号，无法重复创建');
        } else if (code === 'ACCTPERM_USERNAME_EXISTS') {
          message.error('用户名已存在');
        } else if (code === 'ACCTPERM_PERSON_NOT_FOUND' || code === 'ACCTPERM_PERSON_NOT_ACTIVE') {
          message.error(msg || '人员不存在或已停用');
        } else {
          message.error(msg);
        }
      })
      .finally(() => setCreateSubmitting(false));
  };

  const handleSetEnabled = (enabled: boolean) => {
    if (selectedUser == null) return;
    setAccountEnabled(selectedUser.id, enabled)
      .then(() => {
        message.success(enabled ? '已启用' : '已停用');
        return getUser(selectedUser.id);
      })
      .then((u) => {
        setSelectedUser(u);
        setUserSearchResults((prev) =>
          prev.map((r) => (r.id === u.id ? { ...r, enabled: u.enabled } : r)),
        );
      })
      .catch((err) => message.error(getErrorMessage(err)));
  };

  const handleResetPasswordOpen = () => {
    setResetPwdModalOpen(true);
    setResetPwdValue('');
  };

  const handleResetPasswordSubmit = () => {
    if (selectedUser == null) return;
    setResetPwdSubmitting(true);
    apiResetPassword(selectedUser.id, resetPwdValue.trim() || undefined)
      .then(() => {
        message.success(resetPwdValue.trim() ? '密码已重置' : '已重置为默认密码');
        setResetPwdModalOpen(false);
      })
      .catch((err) => message.error(getErrorMessage(err)))
      .finally(() => setResetPwdSubmitting(false));
  };

  const roleLabelForUser = selectedUser && draftRoleTemplateId != null
    ? roles.find((r) => r.id === draftRoleTemplateId)?.label ?? savedOverride?.roleTemplateKey ?? '—'
    : '—';

  return (
    <div style={{ marginTop: 12 }}>
      <div style={{ marginBottom: 12 }}>
        <Space wrap align="center">
          <Button type="primary" onClick={handleCreateAccountOpen}>
            创建账号（绑定人员）
          </Button>
          <span style={{ fontSize: 12 }}>搜索账号：</span>
          <Input.Search
            placeholder="输入用户名或昵称搜索"
            value={userSearchQuery}
            onChange={(e) => setUserSearchQuery(e.target.value)}
            onSearch={(q) => handleUserSearch(q ?? userSearchQuery)}
            loading={userSearchLoading}
            style={{ width: 280 }}
            allowClear
          />
        </Space>
      </div>

      {userSearchResults.length > 0 && (
        <div style={{ marginBottom: 16 }}>
          <Table<UserSummary>
            size="small"
            dataSource={userSearchResults}
            rowKey="id"
            pagination={false}
            scroll={{ x: 500 }}
            locale={{ emptyText: '无结果，请输入关键词搜索' }}
            onRow={(record) => ({
              onClick: () => {
                setSelectedUser(record);
                setAccountDetailDrawerOpen(true);
              },
              style: { cursor: 'pointer', background: selectedUser?.id === record.id ? '#e6f7ff' : undefined },
            })}
            columns={[
              { title: 'ID', dataIndex: 'id', width: 72, render: (v: number) => v },
              { title: '用户名', dataIndex: 'username', ellipsis: true },
              { title: '昵称', dataIndex: 'nickname', ellipsis: true },
              {
                title: '状态',
                dataIndex: 'enabled',
                width: 80,
                render: (v: boolean | undefined) => (v !== false ? '启用' : '停用'),
              },
              {
                title: '操作',
                key: 'action',
                width: 80,
                render: (_, record) => (
                  <Button type="link" size="small" onClick={(e) => { e.stopPropagation(); setSelectedUser(record); setAccountDetailDrawerOpen(true); }}>
                    详情
                  </Button>
                ),
              },
            ]}
          />
        </div>
      )}

      {userSearchResults.length === 0 && userSearchQuery.trim() === '' && (
        <p style={{ color: '#999' }}>请输入用户名或昵称搜索账号，点击表格行打开详情编辑。</p>
      )}
      {userSearchQuery.trim() !== '' && userSearchResults.length === 0 && !userSearchLoading && (
        <p style={{ color: '#999' }}>无匹配账号</p>
      )}

      <Modal
        title="创建账号（绑定人员）"
        open={createModalOpen}
        onCancel={() => setCreateModalOpen(false)}
        onOk={handleCreateAccountSubmit}
        confirmLoading={createSubmitting}
        okText="创建"
        destroyOnClose
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16, paddingTop: 8 }}>
          <div>
            <span style={{ display: 'block', marginBottom: 4, fontSize: 12 }}>选择人员（必选）</span>
            <Select
              placeholder="输入姓名/组织/职位搜索"
              showSearch
              filterOption={false}
              onSearch={setCreatePersonQuery}
              loading={createPersonLoading}
              style={{ width: '100%' }}
              value={createSelectedPersonId ?? undefined}
              onChange={(v) => setCreateSelectedPersonId(v ?? null)}
              notFoundContent={createPersonQuery.trim() ? '无结果' : '输入关键词搜索'}
              options={createPersonOptions.map((p) => ({
                value: p.personId,
                label: [p.name, p.orgUnitLabel, p.title].filter(Boolean).join(' · ') || p.name,
              }))}
              allowClear
            />
          </div>
          <div>
            <span style={{ display: 'block', marginBottom: 4, fontSize: 12 }}>用户名（必填）</span>
            <Input
              value={createUsername}
              onChange={(e) => setCreateUsername(e.target.value)}
              placeholder="登录用用户名"
            />
          </div>
          <div>
            <span style={{ display: 'block', marginBottom: 4, fontSize: 12 }}>昵称（选填）</span>
            <Input
              value={createNickname}
              onChange={(e) => setCreateNickname(e.target.value)}
              placeholder="显示名称"
            />
          </div>
          <div>
            <span style={{ display: 'block', marginBottom: 4, fontSize: 12 }}>角色模板（选填）</span>
            <Select
              placeholder="选择角色模板"
              style={{ width: '100%' }}
              value={createRoleTemplateId ?? undefined}
              onChange={(v) => setCreateRoleTemplateId(v ?? null)}
              allowClear
              options={roles.filter((r) => r.enabled !== false).map((r) => ({ value: r.id, label: `${r.label} (${r.roleKey})` }))}
            />
          </div>
        </div>
      </Modal>

      <Modal
        title="重置密码"
        open={resetPwdModalOpen}
        onCancel={() => setResetPwdModalOpen(false)}
        onOk={handleResetPasswordSubmit}
        confirmLoading={resetPwdSubmitting}
        okText="确定"
      >
        <div style={{ paddingTop: 8 }}>
          <span style={{ fontSize: 12 }}>新密码（留空则重置为默认密码）：</span>
          <Input.Password
            value={resetPwdValue}
            onChange={(e) => setResetPwdValue(e.target.value)}
            placeholder="留空=默认密码"
            style={{ marginTop: 8 }}
          />
        </div>
      </Modal>

      <Drawer
        title={selectedUser ? `账号详情 — ${selectedUser.username}` : '账号详情'}
        open={accountDetailDrawerOpen && selectedUser != null}
        onClose={() => setAccountDetailDrawerOpen(false)}
        width={720}
        destroyOnClose={false}
      >
        {selectedUser && (
          overrideLoading ? (
            <p>加载中...</p>
          ) : (
            <>
              <div style={{ marginBottom: 16, padding: 12, background: '#fafafa', borderRadius: 4 }}>
                <p style={{ fontSize: 12, fontWeight: 600, marginBottom: 8 }}>账号信息</p>
                <p style={{ marginBottom: 4 }}><strong>用户名</strong>：{selectedUser.username}</p>
                <p style={{ marginBottom: 4 }}><strong>昵称</strong>：{selectedUser.nickname ?? '—'}</p>
                <p style={{ marginBottom: 4 }}><strong>ID</strong>：{selectedUser.id}</p>
                <p style={{ marginBottom: 4 }}><strong>状态</strong>：{selectedUser.enabled !== false ? '启用' : '停用'}</p>
                <p style={{ marginBottom: 4 }}><strong>角色</strong>：{roleLabelForUser}</p>
                <p style={{ marginBottom: 4 }}><strong>岗位</strong>：—</p>
                <p style={{ marginBottom: 0 }}><strong>部门</strong>：—</p>
              </div>
              <Space style={{ marginBottom: 12 }} wrap align="center">
                <span style={{ fontSize: 12 }}>启用：</span>
                <Switch
                  checked={selectedUser.enabled !== false}
                  onChange={handleSetEnabled}
                />
                <Button size="small" onClick={handleResetPasswordOpen}>
                  重置密码
                </Button>
              </Space>

              {treeUnavailable && (
                <p style={{ color: '#faad14', marginBottom: 12 }}>
                  无权限管理角色/权限，仅可启用/停用与重置密码。
                </p>
              )}

              {!treeUnavailable && (
                <>
          <Space style={{ marginBottom: 12 }} wrap>
            <Button type="primary" onClick={handleSave} loading={overrideSaveLoading} disabled={!overrideDirty}>
              保存
            </Button>
            <Button onClick={handleReset} disabled={!overrideDirty}>
              还原
            </Button>
          </Space>

          <div style={{ marginBottom: 16 }}>
            <Space wrap>
              <span style={{ fontSize: 12 }}>分配角色模板：</span>
              <Select
                placeholder={rolesEnabledOnly ? '选择模板（仅已启用）' : '选择模板（含停用）'}
                style={{ width: 260 }}
                value={draftRoleTemplateId ?? undefined}
                onChange={(v) => setDraftRoleTemplateId(v ?? null)}
                allowClear
                options={roles.map((r) => ({ value: r.id, label: `${r.label} (${r.roleKey})` }))}
              />
              <Button type="link" size="small" onClick={() => setRolesEnabledOnly(!rolesEnabledOnly)}>
                {rolesEnabledOnly ? '显示全部（含停用）' : '仅已启用'}
              </Button>
            </Space>
          </div>

          {/* 差异视图（默认） */}
          <div style={{ marginBottom: 16 }}>
            <p style={{ fontSize: 12, fontWeight: 600, marginBottom: 12 }}>差异视图</p>
            <div style={{ marginBottom: 12, padding: 12, background: '#fafafa', borderRadius: 4 }}>
              <p style={{ fontSize: 12, fontWeight: 500, marginBottom: 4 }}>1. 继承自模板（只读）</p>
              <p style={{ fontSize: 12, color: '#666' }}>
                当前模板：{templateLabel}
                {draftRoleTemplateId != null && <span>（共 {templateKeys.size} 项继承）</span>}
              </p>
            </div>
            <div style={{ marginBottom: 12, padding: 12, background: '#f6ffed', borderRadius: 4, border: '1px solid #b7eb8f' }}>
              <p style={{ fontSize: 12, fontWeight: 500, marginBottom: 8 }}>2. 追加权限（Add）</p>
              {!treeUnavailable && (
                <Button type="primary" size="small" onClick={() => { setAddPickerSelected(new Set()); setAddPickerOpen(true); }} style={{ marginBottom: 8 }}>
                  添加权限
                </Button>
              )}
              {addGroups.length === 0 ? (
                <p style={{ fontSize: 12, color: '#999' }}>无</p>
              ) : (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                  {addGroups.map((g) => (
                    <div key={g.moduleKey} style={{ marginBottom: 8 }}>
                      <span style={{ fontSize: 12, fontWeight: 500 }}>{g.label}：</span>
                      {g.keys.map((k) => (
                        <Tag key={k} color="green" style={{ margin: 2 }} title={k}>
                          {k.includes(':') ? k.split(':')[1] : k}
                        </Tag>
                      ))}
                    </div>
                  ))}
                </div>
              )}
            </div>
            <div style={{ marginBottom: 12, padding: 12, background: '#fff2f0', borderRadius: 4, border: '1px solid #ffccc7' }}>
              <p style={{ fontSize: 12, fontWeight: 500, marginBottom: 4 }}>3. 移除权限（Remove，移除优先）</p>
              {!treeUnavailable && (
                <Button size="small" danger onClick={() => { setRemovePickerSelected(new Set()); setRemovePickerOpen(true); }} style={{ marginBottom: 8 }}>
                  移除权限
                </Button>
              )}
              {removeGroups.length === 0 ? (
                <p style={{ fontSize: 12, color: '#999' }}>无</p>
              ) : (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                  {removeGroups.map((g) => (
                    <div key={g.moduleKey} style={{ marginBottom: 8 }}>
                      <span style={{ fontSize: 12, fontWeight: 500 }}>{g.label}：</span>
                      {g.keys.map((k) => (
                        <Tag key={k} color="red" style={{ margin: 2 }} title={k}>
                          {k.includes(':') ? k.split(':')[1] : k}
                        </Tag>
                      ))}
                    </div>
                  ))}
                </div>
              )}
            </div>
            <div style={{ padding: 12, background: '#fafafa', borderRadius: 4 }}>
              <p style={{ fontSize: 12, fontWeight: 500, marginBottom: 4 }}>最终生效</p>
              <p style={{ fontSize: 12, color: '#666' }}>
                共 {effectivePreview.size} 项
                <Button type="link" size="small" onClick={() => setEffectiveDrawerOpen(true)} style={{ paddingLeft: 8 }}>
                  展开查看全部有效权限
                </Button>
              </p>
            </div>
          </div>

          {/* 高级编辑（可选，默认折叠） */}
          <Collapse
            activeKey={advancedEditorOpen ? 'advanced' : undefined}
            onChange={(keys) => setAdvancedEditorOpen(keys.includes('advanced'))}
            items={[
              {
                key: 'advanced',
                label: '高级编辑（可选）',
                children: treeUnavailable ? (
                  <p style={{ color: '#faad14' }}>无权限查看权限树，无法使用高级编辑。</p>
                ) : (
                  <>
                    <ModuleOverviewCards
                      modules={treeData?.modules ?? []}
                      onQuickOp={handleQuickOpOverride}
                      draftKeys={new Set([...templateKeys, ...draftAddKeys].filter((k) => !draftRemoveKeys.has(k)))}
                    />
                    <p style={{ fontSize: 12, color: '#666', marginBottom: 8 }}>
                      菜单树勾选即授予该菜单至少「查看」；每个权限：默认（继承）/ 添加 / 移除（移除优先）
                    </p>
                    {treeData?.modules?.length ? (
                      <PermissionMenuTree
                        modules={treeData.modules}
                        getMenuChecked={getMenuCheckedOverride}
                        onMenuCheck={onMenuCheckOverride}
                        mode="override"
                        override={{
                          templateKeys,
                          draftAdd: draftAddKeys,
                          draftRemove: draftRemoveKeys,
                          onSetState: setOverrideKeyState,
                        }}
                      />
                    ) : (
                      <p style={{ color: '#999' }}>暂无模块</p>
                    )}
                  </>
                ),
              },
            ]}
          />

                </>
              )}

          {/* 添加权限 弹窗 */}
          <Modal
            title="添加权限"
            open={addPickerOpen}
            onCancel={() => { setAddPickerOpen(false); setAddPickerSelected(new Set()); }}
            onOk={handleAddPickerOk}
            okText="确定"
            width={560}
            destroyOnClose
          >
            <PermissionKeyPicker
              modules={treeData?.modules ?? []}
              selectedKeys={addPickerSelected}
              onSelectionChange={setAddPickerSelected}
              title="勾选要追加的权限，确定后加入「追加权限」并自动从「移除」中剔除。"
            />
          </Modal>

          {/* 移除权限 弹窗 */}
          <Modal
            title="移除权限"
            open={removePickerOpen}
            onCancel={() => { setRemovePickerOpen(false); setRemovePickerSelected(new Set()); }}
            onOk={handleRemovePickerOk}
            okText="确定"
            width={560}
            destroyOnClose
          >
            <PermissionKeyPicker
              modules={treeData?.modules ?? []}
              selectedKeys={removePickerSelected}
              onSelectionChange={setRemovePickerSelected}
              title="勾选要从有效权限中移除的项，确定后加入「移除权限」（移除优先）。"
            />
          </Modal>

          {/* 最终生效 抽屉 */}
          <Drawer
            title="全部有效权限（保存后生效）"
            open={effectiveDrawerOpen}
            onClose={() => setEffectiveDrawerOpen(false)}
            width={400}
          >
            <Input.Search
              placeholder="搜索权限键"
              value={effectiveSearch}
              onChange={(e) => setEffectiveSearch(e.target.value)}
              allowClear
              style={{ marginBottom: 12 }}
            />
            <div style={{ maxHeight: '70vh', overflow: 'auto' }}>
              {effectiveGrouped.map((g) => (
                <div key={g.moduleKey} style={{ marginBottom: 12 }}>
                  <p style={{ fontWeight: 600, marginBottom: 4 }}>{g.label}</p>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                    {g.keys.map((k) => (
                      <Tag key={k} title={k}>{k.includes(':') ? k.split(':')[1] : k}</Tag>
                    ))}
                  </div>
                </div>
              ))}
              {effectiveGrouped.length === 0 && (
                <p style={{ color: '#999' }}>{effectiveSearch ? '无匹配' : '无'}</p>
              )}
            </div>
          </Drawer>
            </>
          )
        )}
      </Drawer>
    </div>
  );
}
