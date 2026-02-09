import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Button,
  Card,
  Checkbox,
  Dropdown,
  Form,
  Input,
  message,
  Modal,
  Select,
  Space,
  Spin,
  Table,
  Tabs,
  Tag,
  Tree,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { DataNode } from 'antd/es/tree';
import type { MenuProps } from 'antd';
import { CaretDownOutlined, CaretRightOutlined, DownOutlined, PlusOutlined } from '@ant-design/icons';
import {
  getOrgTree,
  getNode,
  getNodeImpact,
  createNode,
  updateNode,
  moveNode,
  disableNode,
  enableNode,
  listEnabledLevels,
  searchPeopleByOrg,
  getPerson,
  createPerson,
  updatePerson,
  disablePerson,
  enablePerson,
  listJobLevels,
  getPositionCatalog,
  getPositionsByOrg,
  enablePosition,
  disablePosition,
  putPositionBindings,
  getPersonPositions,
  resetOrgStructureAll,
  type OrgTreeNode,
  type OrgNodeItem,
  type OrgLevelItem,
  type OrgPersonItem,
  type JobLevelItem,
  type PositionCatalogItem,
  type PositionsByOrgResponse,
  type EnabledPositionWithBindings,
} from '@/features/orgstructure/services/orgStructure';
import { OrgPersonSelect } from '@/features/orgstructure/components/OrgPersonSelect';
import { TypeToConfirmModal } from '@/shared/components/TypeToConfirmModal';

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  message.error(err?.response?.data?.message ?? err?.message ?? '操作失败');
}

/** Extended node for tree with levelId for "next level" default. */
interface TreeDataNode extends DataNode {
  key: string;
  levelId?: number;
  nodeId?: number;
  isEnabled?: boolean;
}

function treeToDataNodes(nodes: OrgTreeNode[]): TreeDataNode[] {
  return nodes.map((n) => {
    const title = n.name + (n.isEnabled ? '' : ' (停用)');
    const children = n.children?.length ? treeToDataNodes(n.children) : [];
    return {
      key: String(n.id),
      title,
      children: children.length ? children : undefined,
      isLeaf: children.length === 0,
      disabled: !n.isEnabled,
      levelId: n.levelId,
      nodeId: n.id,
      isEnabled: n.isEnabled,
    };
  });
}

/** Get keys of nodes that match search (and all ancestor keys for expand). */
function getSearchExpandAndSelectKeys(nodes: OrgTreeNode[], searchLower: string): { expandKeys: string[]; selectKey: string | null } {
  const expandKeys: string[] = [];
  let selectKey: string | null = null;
  function walk(ns: OrgTreeNode[], parentKeys: string[]) {
    for (const n of ns) {
      const key = String(n.id);
      const match = n.name.toLowerCase().includes(searchLower);
      if (match) {
        expandKeys.push(...parentKeys);
        if (!selectKey) selectKey = key;
      }
      if (n.children?.length) walk(n.children, [...parentKeys, key]);
    }
  }
  walk(nodes, []);
  return { expandKeys: [...new Set(expandKeys)], selectKey };
}

/** Get path names from root to node (for display). Returns [] if not found. */
function getPathToNode(nodes: OrgTreeNode[], nodeId: number): string[] {
  for (const n of nodes) {
    if (n.id === nodeId) return [n.name];
    if (n.children?.length) {
      const sub = getPathToNode(n.children, nodeId);
      if (sub.length) return [n.name, ...sub];
    }
  }
  return [];
}

/** Flatten tree to { id, name, path } for org select. Root = single top-level node (公司). */
function flattenTreeForOrg(nodes: OrgTreeNode[], out: { id: number; name: string; path: string }[] = [], path = ''): { id: number; name: string; path: string }[] {
  for (const n of nodes) {
    const p = path ? `${path} / ${n.name}` : n.name;
    out.push({ id: n.id, name: n.name, path: p });
    if (n.children?.length) flattenTreeForOrg(n.children, out, p);
  }
  return out;
}

export default function OrgTreePage() {
  const [tree, setTree] = useState<OrgTreeNode[]>([]);
  const [loading, setLoading] = useState(true);
  const [includeDisabled, setIncludeDisabled] = useState(false);
  const [searchValue, setSearchValue] = useState('');
  const [expandedKeys, setExpandedKeys] = useState<string[]>([]);
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const [detail, setDetail] = useState<OrgNodeItem | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [levels, setLevels] = useState<OrgLevelItem[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [moveModalOpen, setMoveModalOpen] = useState(false);
  const [moveTargetId, setMoveTargetId] = useState<number | null>(null);
  const [impact, setImpact] = useState<{ descendantNodeCount: number; personCountInSubtree: number } | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<{ levelId: number; parentId?: number; name: string; remark?: string; isEnabled?: boolean }>();
  const [editForm] = Form.useForm<{ name: string; remark?: string; isEnabled?: boolean }>();
  const [editNodeModalOpen, setEditNodeModalOpen] = useState(false);
  const [editNodeId, setEditNodeId] = useState<number | null>(null);
  /** Create modal: locked level/parent display (not editable). */
  const [createParentPath, setCreateParentPath] = useState<string>('');
  const [createParentId, setCreateParentId] = useState<number | null>(null);
  const [createLevelId, setCreateLevelId] = useState<number | undefined>(undefined);
  const [createLevelDisplayName, setCreateLevelDisplayName] = useState<string>('');
  const [createNoNextLevel, setCreateNoNextLevel] = useState(false);
  /** People in right panel (by selected org subtree or all when root). */
  const [peopleContent, setPeopleContent] = useState<OrgPersonItem[]>([]);
  const [peopleTotal, setPeopleTotal] = useState(0);
  const [peopleLoading, setPeopleLoading] = useState(false);
  const [peoplePage, setPeoplePage] = useState(1);
  const [peoplePageSize, setPeoplePageSize] = useState(20);
  const [peopleKeyword, setPeopleKeyword] = useState('');
  const [peopleKeywordApplied, setPeopleKeywordApplied] = useState('');
  const [peopleActiveOnly, setPeopleActiveOnly] = useState(true);
  const [personModalOpen, setPersonModalOpen] = useState(false);
  const [personEditingId, setPersonEditingId] = useState<number | null>(null);
  const [personSubmitting, setPersonSubmitting] = useState(false);
  const [jobLevels, setJobLevels] = useState<JobLevelItem[]>([]);
  const [orgOptionsForPerson, setOrgOptionsForPerson] = useState<{ id: number; name: string; path: string }[]>([]);
  const [personForm] = Form.useForm<{
    name: string;
    phone: string;
    email?: string;
    remark?: string;
    jobLevelId: number;
    primaryOrgNodeId: number;
    secondaryOrgNodeIds?: number[];
  }>();
  /** Right panel tab: 人员 | 职位配置 */
  const [rightTabKey, setRightTabKey] = useState<string>('people');
  /** Position config tab */
  const [positionsByOrg, setPositionsByOrg] = useState<PositionsByOrgResponse | null>(null);
  const [positionsLoading, setPositionsLoading] = useState(false);
  const [positionCatalog, setPositionCatalog] = useState<PositionCatalogItem[]>([]);
  const [enablePositionModalOpen, setEnablePositionModalOpen] = useState(false);
  const [enablePositionSelectedIds, setEnablePositionSelectedIds] = useState<number[]>([]);
  const [bindingsSaving, setBindingsSaving] = useState<Record<string, boolean>>({});
  /** Draft person ids per position (key: orgNodeId-positionId) for 职位配置 Save */
  const [bindingDraft, setBindingDraft] = useState<Record<string, number[]>>({});
  /** Person 任职列表 (when editing person) */
  const [personAssignments, setPersonAssignments] = useState<{ orgNodeId: number; orgNodeNamePath: string; positionDisplayName: string; positionShortName: string | null; isPartTime: boolean }[]>([]);
  /** 清空测试数据 modal */
  const [resetAllModalOpen, setResetAllModalOpen] = useState(false);
  const [resetAllSubmitting, setResetAllSubmitting] = useState(false);

  const fetchTree = useCallback(async () => {
    setLoading(true);
    try {
      const data = await getOrgTree(includeDisabled);
      setTree(data);
    } catch (e) {
      showError(e);
    } finally {
      setLoading(false);
    }
  }, [includeDisabled]);

  useEffect(() => {
    fetchTree();
  }, [fetchTree]);

  /** Default selection: when tree loads with at least one root, auto-select the first root so People panel shows immediately. */
  useEffect(() => {
    if (!loading && tree.length > 0 && selectedKey == null) {
      setSelectedKey(String(tree[0].id));
    }
  }, [loading, tree, selectedKey]);

  useEffect(() => {
    (async () => {
      try {
        const data = await listEnabledLevels();
        setLevels(data);
      } catch {
        setLevels([]);
      }
    })();
  }, []);

  useEffect(() => {
    listJobLevels().then(setJobLevels).catch(() => setJobLevels([]));
    getOrgTree(false).then((t) => setOrgOptionsForPerson(flattenTreeForOrg(t))).catch(() => setOrgOptionsForPerson([]));
    getPositionCatalog().then(setPositionCatalog).catch(() => setPositionCatalog([]));
  }, []);

  /** Fetch positions when 职位配置 tab is active and detail exists */
  const fetchPositionsByOrg = useCallback(async () => {
    if (!detail || rightTabKey !== 'positions') return;
    setPositionsLoading(true);
    try {
      const data = await getPositionsByOrg(detail.id);
      setPositionsByOrg(data);
    } catch (e) {
      showError(e);
      setPositionsByOrg(null);
    } finally {
      setPositionsLoading(false);
    }
  }, [detail, rightTabKey]);

  useEffect(() => {
    fetchPositionsByOrg();
  }, [fetchPositionsByOrg]);

  useEffect(() => {
    setBindingDraft({});
  }, [detail?.id]);

  /** Root = top-level node (parentId conceptually null; in our tree, roots are the top-level array). When selected, show all people. */
  const isSelectedRoot = detail != null && tree.length > 0 && tree.some((n) => n.id === detail.id);

  const fetchPeople = useCallback(async () => {
    if (!detail) return;
    setPeopleLoading(true);
    try {
      const res = await searchPeopleByOrg({
        orgNodeId: isSelectedRoot ? undefined : detail.id,
        includeDescendants: true,
        includeSecondaries: true,
        activeOnly: peopleActiveOnly,
        keyword: peopleKeywordApplied || undefined,
        page: peoplePage,
        size: peoplePageSize,
      });
      setPeopleContent(res.content ?? []);
      setPeopleTotal(res.totalElements ?? 0);
    } catch (e) {
      showError(e);
    } finally {
      setPeopleLoading(false);
    }
  }, [detail, isSelectedRoot, peopleActiveOnly, peopleKeywordApplied, peoplePage, peoplePageSize]);

  useEffect(() => {
    if (detail) fetchPeople();
    else {
      setPeopleContent([]);
      setPeopleTotal(0);
    }
  }, [detail, fetchPeople]);

  useEffect(() => {
    if (!detail) setPeopleKeywordApplied('');
  }, [detail]);

  useEffect(() => {
    if (selectedKey == null) {
      setDetail(null);
      return;
    }
    const id = Number(selectedKey);
    if (!Number.isFinite(id)) return;
    setDetailLoading(true);
    getNode(id)
      .then(setDetail)
      .catch(() => setDetail(null))
      .finally(() => setDetailLoading(false));
  }, [selectedKey]);

  useEffect(() => {
    if (!searchValue.trim()) return;
    const searchLower = searchValue.trim().toLowerCase();
    const { expandKeys: nextExpand, selectKey: nextSelect } = getSearchExpandAndSelectKeys(tree, searchLower);
    setExpandedKeys((prev) => [...new Set([...prev, ...nextExpand])]);
    if (nextSelect) setSelectedKey(nextSelect);
  }, [searchValue, tree]);

  const treeData = useMemo(() => treeToDataNodes(tree), [tree]);

  /** Root level = 公司. Next level after parent (by orderIndex); undefined if no next enabled level. */
  const getLevelIdForCreate = useCallback((parentLevelId?: number): number | undefined => {
    if (!levels.length) return undefined;
    if (parentLevelId == null) {
      const company = levels.find((l) => l.displayName === '公司');
      return company?.id ?? levels[0]?.id;
    }
    const parentLevel = levels.find((l) => l.id === parentLevelId);
    const parentOrder = parentLevel?.orderIndex ?? 0;
    const next = levels.find((l) => l.orderIndex > parentOrder);
    return next?.id;
  }, [levels]);

  const openCreate = useCallback((parentId?: number, parentLevelId?: number, parentPath?: string) => {
    const levelId = getLevelIdForCreate(parentLevelId);
    const levelDisplayName = levelId != null ? levels.find((l) => l.id === levelId)?.displayName ?? '' : '';
    const isRoot = parentId == null;
    setCreateParentPath(isRoot ? '根节点（公司）' : (parentPath ?? (parentId != null ? getPathToNode(tree, parentId).join(' / ') : '根节点（公司）')));
    setCreateParentId(parentId ?? null);
    setCreateLevelId(levelId ?? undefined);
    setCreateLevelDisplayName(levelDisplayName);
    setCreateNoNextLevel(!isRoot && levelId == null);
    form.setFieldsValue({
      name: '',
      remark: '',
      isEnabled: true,
    });
    setModalOpen(true);
  }, [form, getLevelIdForCreate, levels, tree]);

  const openCreateRoot = () => openCreate(undefined, undefined, '根节点（公司）');

  const openAddChild = (nodeId: number, levelId: number) => {
    const path = getPathToNode(tree, nodeId);
    openCreate(nodeId, levelId, path.length ? path.join(' / ') : undefined);
  };

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      if (createLevelId == null || !Number.isFinite(createLevelId)) {
        message.error('已没有可用的下级层级，请先在层级配置启用/新增层级');
        return;
      }
      setSubmitting(true);
      await createNode({
        levelId: createLevelId,
        parentId: createParentId,
        name: values.name?.trim() ?? '',
        remark: values.remark?.trim() || undefined,
        isEnabled: values.isEnabled ?? true,
      });
      message.success('新建成功');
      setModalOpen(false);
      fetchTree();
    } catch (e) {
      if (e instanceof Error && e.message?.includes('validateFields')) return;
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const openEditNode = (nodeId: number) => {
    setEditNodeId(nodeId);
    if (detail?.id === nodeId) {
      editForm.setFieldsValue({ name: detail.name, remark: detail.remark ?? '', isEnabled: detail.isEnabled ?? true });
      setEditNodeModalOpen(true);
    } else {
      getNode(nodeId).then((n) => {
        editForm.setFieldsValue({ name: n.name, remark: n.remark ?? '', isEnabled: n.isEnabled ?? true });
        setEditNodeModalOpen(true);
      }).catch(showError);
    }
  };

  const handleEditNodeSave = async () => {
    if (editNodeId == null) return;
    try {
      const values = await editForm.validateFields();
      setSubmitting(true);
      await updateNode(editNodeId, {
        name: values.name?.trim(),
        remark: values.remark?.trim(),
        isEnabled: values.isEnabled ?? true,
      });
      message.success('已保存');
      setEditNodeModalOpen(false);
      setEditNodeId(null);
      fetchTree();
      if (selectedKey === String(editNodeId)) {
        getNode(editNodeId).then(setDetail).catch(() => {});
      }
    } catch (e) {
      if (e instanceof Error && e.message?.includes('validateFields')) return;
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const openMove = async (id: number) => {
    setMoveTargetId(id);
    try {
      const impactData = await getNodeImpact(id);
      setImpact(impactData);
    } catch {
      setImpact(null);
    }
    setMoveModalOpen(true);
  };

  const handleMove = async (newParentId: number | null) => {
    if (moveTargetId == null) return;
    try {
      setSubmitting(true);
      await moveNode(moveTargetId, newParentId);
      message.success('移动成功');
      setMoveModalOpen(false);
      setMoveTargetId(null);
      setImpact(null);
      fetchTree();
    } catch (e) {
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDisable = (id: number) => {
    Modal.confirm({
      title: '确认停用',
      content: '停用后该节点将不在默认树中显示。若其下存在启用中的下级或关联人员，将无法停用。',
      onOk: async () => {
        try {
          await disableNode(id);
          message.success('已停用');
          if (selectedKey === String(id)) setSelectedKey(null);
          fetchTree();
        } catch (e) {
          showError(e);
        }
      },
    });
  };

  const handleEnable = async (id: number) => {
    try {
      await enableNode(id);
      message.success('已启用');
      fetchTree();
    } catch (e) {
      showError(e);
    }
  };

  const openCreatePerson = () => {
    personForm.setFieldsValue({
      name: '',
      phone: '',
      email: '',
      remark: '',
      jobLevelId: jobLevels[0]?.id,
      primaryOrgNodeId: isSelectedRoot ? undefined : detail?.id,
      secondaryOrgNodeIds: [],
    });
    setPersonEditingId(null);
    setPersonAssignments([]);
    setPersonModalOpen(true);
  };

  const openEditPerson = async (id: number) => {
    try {
      const [row, assignments] = await Promise.all([
        getPerson(id),
        getPersonPositions(id),
      ]);
      personForm.setFieldsValue({
        name: row.name,
        phone: row.phone,
        email: row.email ?? '',
        remark: row.remark ?? '',
        jobLevelId: row.jobLevelId,
        primaryOrgNodeId: row.primaryOrgNodeId ?? undefined,
        secondaryOrgNodeIds: row.secondaryOrgNodeIds ?? [],
      });
      setPersonEditingId(id);
      setPersonAssignments(assignments.map((a) => ({
        orgNodeId: a.orgNodeId,
        orgNodeNamePath: a.orgNodeNamePath,
        positionDisplayName: a.positionDisplayName,
        positionShortName: a.positionShortName,
        isPartTime: a.isPartTime,
      })));
      setPersonModalOpen(true);
    } catch (e) {
      showError(e);
    }
  };

  const handlePersonSubmit = async () => {
    try {
      const values = await personForm.validateFields();
      setPersonSubmitting(true);
      if (personEditingId == null) {
        await createPerson({
          name: values.name?.trim() ?? '',
          phone: values.phone?.trim() ?? '',
          email: values.email?.trim() || undefined,
          remark: values.remark?.trim() || undefined,
          jobLevelId: values.jobLevelId,
          primaryOrgNodeId: values.primaryOrgNodeId,
          secondaryOrgNodeIds: values.secondaryOrgNodeIds ?? [],
        });
        message.success('新建成功');
      } else {
        await updatePerson(personEditingId, {
          name: values.name?.trim(),
          phone: values.phone?.trim(),
          email: values.email?.trim() || undefined,
          remark: values.remark?.trim() || undefined,
          jobLevelId: values.jobLevelId,
          primaryOrgNodeId: values.primaryOrgNodeId,
          secondaryOrgNodeIds: values.secondaryOrgNodeIds ?? [],
        });
        message.success('更新成功');
      }
      setPersonModalOpen(false);
      fetchPeople();
    } catch (e) {
      if (e instanceof Error && e.message?.includes('validateFields')) return;
      showError(e);
    } finally {
      setPersonSubmitting(false);
    }
  };

  const handleDisablePerson = (row: OrgPersonItem) => {
    Modal.confirm({
      title: '确认停用',
      content: `确定停用「${row.name}」？`,
      onOk: async () => {
        try {
          await disablePerson(row.id);
          message.success('已停用');
          fetchPeople();
        } catch (e) {
          showError(e);
        }
      },
    });
  };

  const handleEnablePerson = async (row: OrgPersonItem) => {
    try {
      await enablePerson(row.id);
      message.success('已启用');
      fetchPeople();
    } catch (e) {
      showError(e);
    }
  };

  const handleSaveBindings = async (positionId: number, personIds: number[]) => {
    if (!detail) return;
    const key = `${detail.id}-${positionId}`;
    setBindingsSaving((prev) => ({ ...prev, [key]: true }));
    try {
      await putPositionBindings(detail.id, positionId, personIds);
      message.success('已保存');
      fetchPositionsByOrg();
    } catch (e) {
      showError(e);
    } finally {
      setBindingsSaving((prev) => ({ ...prev, [key]: false }));
    }
  };

  const handleEnablePositionsConfirm = async () => {
    if (!detail || enablePositionSelectedIds.length === 0) {
      setEnablePositionModalOpen(false);
      return;
    }
    try {
      for (const positionId of enablePositionSelectedIds) {
        await enablePosition(detail.id, positionId);
      }
      message.success('已启用 ' + enablePositionSelectedIds.length + ' 个职位');
      setEnablePositionModalOpen(false);
      setEnablePositionSelectedIds([]);
      fetchPositionsByOrg();
    } catch (e) {
      showError(e);
    }
  };

  const handleDisablePosition = (positionId: number) => {
    if (!detail) return;
    Modal.confirm({
      title: '确认停用职位',
      content: '若该职位已绑定人员，将无法停用，需先解除绑定。',
      onOk: async () => {
        try {
          await disablePosition(detail!.id, positionId);
          message.success('已停用');
          fetchPositionsByOrg();
        } catch (e) {
          showError(e);
        }
      },
    });
  };

  const renderTreeNodeTitle = (nodeData: TreeDataNode) => {
    const nodeId = nodeData.nodeId ?? Number(nodeData.key);
    const levelId = nodeData.levelId;
    const menuItems: MenuProps['items'] = [
      { key: 'edit', label: '编辑', onClick: () => openEditNode(nodeId) },
      { key: 'move', label: '移动', onClick: () => openMove(nodeId) },
      {
        key: 'disable',
        label: nodeData.isEnabled ? '停用' : '启用',
        onClick: () => (nodeData.isEnabled ? handleDisable(nodeId) : handleEnable(nodeId)),
      },
    ];
    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: 4, width: '100%' }}>
        <span
          style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', cursor: 'pointer', minWidth: 0 }}
          onClick={(e) => { e.stopPropagation(); setSelectedKey(nodeData.key as string); }}
        >
          {nodeData.title}
        </span>
        <Button
          type="link"
          size="small"
          icon={<PlusOutlined />}
          onClick={(e) => { e.stopPropagation(); openAddChild(nodeId, levelId); }}
          style={{ padding: '0 4px', flexShrink: 0 }}
          title="添加下级"
        />
        <Dropdown menu={{ items: menuItems }} trigger={['click']} onClick={(e) => e.stopPropagation()}>
          <Button type="text" size="small" icon={<DownOutlined />} style={{ padding: '0 2px', flexShrink: 0 }} />
        </Dropdown>
      </div>
    );
  };

  return (
    <div style={{ padding: 24 }}>
      <div style={{ marginBottom: 16 }}>
        <h2 style={{ margin: 0, fontSize: 18 }}>组织架构</h2>
      </div>
      <div style={{ display: 'flex', gap: 16, height: 'calc(100vh - 180px)' }}>
      <Card title="组织树" style={{ width: 400, flexShrink: 0 }}>
        <Alert
          type="info"
          showIcon={false}
          message={
            <span style={{ fontSize: 12 }}>
              选中组织后右侧展示人员与职位配置；点「+」可添加下级组织；「…」菜单中可编辑、移动、停用/启用。
            </span>
          }
          style={{ marginBottom: 12 }}
        />
        <Space direction="vertical" style={{ width: '100%' }}>
          <Input.Search
            placeholder="搜索组织名称…（自动定位）"
            allowClear
            value={searchValue}
            onSearch={setSearchValue}
            onChange={(e) => setSearchValue(e.target.value)}
          />
          <Checkbox
            checked={includeDisabled}
            onChange={(e) => setIncludeDisabled(e.target.checked)}
          >
            显示停用
          </Checkbox>
          {!loading && tree.length === 0 ? (
            <Button type="primary" size="large" block onClick={openCreateRoot}>
              创建根节点（公司）
            </Button>
          ) : (
            <Button type="primary" size="small" onClick={openCreateRoot}>
              新建根节点
            </Button>
          )}
          <Button size="small" danger onClick={() => setResetAllModalOpen(true)}>
            清空测试数据
          </Button>
        </Space>
        <TypeToConfirmModal
          open={resetAllModalOpen}
          onCancel={() => setResetAllModalOpen(false)}
          title="清空组织架构测试数据"
          description={
            <>
              <p style={{ marginBottom: 8 }}><strong>将清空（仅组织架构域）：</strong></p>
              <ul style={{ marginBottom: 8, paddingLeft: 20 }}>
                <li>职位绑定、职位启用配置</li>
                <li>人员归属、组织人员</li>
                <li>组织节点、变更记录、层级</li>
              </ul>
              <p style={{ marginBottom: 8 }}><strong>将恢复：</strong>默认层级 + 根节点「公司」。</p>
              <p style={{ marginBottom: 0 }}><strong>不会触及：</strong>账号、权限、职级字典、职位目录等。</p>
            </>
          }
          confirmText="RESET"
          transformInput={(s) => s.toUpperCase()}
          onConfirm={async () => {
            setResetAllSubmitting(true);
            try {
              await resetOrgStructureAll({ confirmText: 'RESET' });
              message.success('已清空组织架构测试数据');
              setResetAllModalOpen(false);
              setSelectedKey(null);
              setDetail(null);
              await fetchTree();
              listEnabledLevels().then(setLevels);
              getOrgTree(false).then((t) => setOrgOptionsForPerson(flattenTreeForOrg(t)));
            } catch (e) {
              showError(e);
              throw e;
            } finally {
              setResetAllSubmitting(false);
            }
          }}
          okText="确认清空"
          danger
          loading={resetAllSubmitting}
        />
        <Spin spinning={loading}>
          {tree.length > 0 && (
            <>
              <style>{`.org-tree-large-switcher .ant-tree-switcher { width: 24px; min-width: 24px; height: 24px; display: inline-flex !important; align-items: center; justify-content: center; }`}</style>
              <Tree
                className="org-tree-large-switcher"
                style={{ marginTop: 12 }}
                treeData={treeData}
                expandedKeys={expandedKeys}
                onExpand={(keys) => setExpandedKeys(keys as string[])}
                selectedKeys={selectedKey ? [selectedKey] : []}
                onSelect={(_, { node }) => setSelectedKey(node.key as string)}
                titleRender={(node) => renderTreeNodeTitle(node as TreeDataNode)}
                blockNode
                switcherIcon={({ expanded }) => (expanded ? <CaretDownOutlined style={{ fontSize: 14 }} /> : <CaretRightOutlined style={{ fontSize: 14 }} />)}
              />
            </>
          )}
        </Spin>
      </Card>
      <Card title={detail ? `组织：${detail.name}` : '选择组织'} style={{ flex: 1, minWidth: 0 }}>
        {detailLoading && !detail && <Spin style={{ display: 'block', margin: '24px auto' }} />}
        {!detail && !detailLoading && (
          <p style={{ color: '#666', marginBottom: 0 }}>请在左侧选择组织</p>
        )}
        {detail && !detailLoading && (
          <Tabs
            activeKey={rightTabKey}
            onChange={setRightTabKey}
            items={[
              {
                key: 'people',
                label: '人员',
                children: (
                  <>
                    <h4 style={{ marginBottom: 12 }}>
                      {isSelectedRoot ? '全部人员' : '本节点及下级人员'}
                    </h4>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, marginBottom: 16 }}>
            <Card
              size="small"
              style={{
                width: 140,
                height: 100,
                cursor: detail ? 'pointer' : 'not-allowed',
                background: detail ? '#f6ffed' : '#f5f5f5',
                borderColor: detail ? '#b7eb8f' : '#d9d9d9',
              }}
              onClick={() => {
                if (detail) openCreatePerson();
                else message.info('请先在左侧选择组织');
              }}
            >
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
                <PlusOutlined style={{ fontSize: 28, color: detail ? '#52c41a' : '#bfbfbf' }} />
                <span style={{ marginTop: 8, color: detail ? '#52c41a' : '#bfbfbf' }}>添加人员</span>
              </div>
            </Card>
            {detail ? peopleContent.map((p) => (
              <Card
                key={p.id}
                size="small"
                style={{ width: 140, height: 100, cursor: 'pointer' }}
                onClick={() => openEditPerson(p.id)}
              >
                <div style={{ fontSize: 13 }}>
                  <div style={{ fontWeight: 600 }}>{p.name}</div>
                  <div style={{ color: '#666', marginTop: 4 }}>{p.phone || '—'}</div>
                  <div style={{ color: '#666', fontSize: 12 }}>{p.email || p.jobLevelDisplayName || '—'}</div>
                  <div style={{ marginTop: 4 }}>{p.isActive ? <span style={{ color: '#52c41a' }}>在岗</span> : <span style={{ color: '#999' }}>停用</span>}</div>
                </div>
              </Card>
            )) : null}
          </div>
          {detail ? (
            <>
              <Space style={{ marginBottom: 8 }}>
                <Input.Search
                  placeholder="姓名/手机/邮箱"
                  allowClear
                  value={peopleKeyword}
                  onChange={(e) => setPeopleKeyword(e.target.value)}
                  onSearch={(v) => { setPeopleKeywordApplied(v); setPeoplePage(1); }}
                  style={{ width: 200 }}
                />
                <Checkbox checked={peopleActiveOnly} onChange={(e) => { setPeopleActiveOnly(e.target.checked); setPeoplePage(1); }}>
                  仅在岗
                </Checkbox>
              </Space>
              <Table<OrgPersonItem>
                rowKey="id"
                size="small"
                dataSource={peopleContent}
                loading={peopleLoading}
                pagination={{
                  current: peoplePage,
                  pageSize: peoplePageSize,
                  total: peopleTotal,
                  showSizeChanger: true,
                  showTotal: (t) => `共 ${t} 条`,
                  onChange: (p, ps) => { setPeoplePage(p); if (ps) setPeoplePageSize(ps); },
                }}
                columns={([
                  { title: '姓名', dataIndex: 'name', width: 90 },
                  { title: '电话', dataIndex: 'phone', width: 110 },
                  { title: '邮箱', dataIndex: 'email', ellipsis: true, render: (v: string) => v || '—' },
                  { title: '职级', dataIndex: 'jobLevelDisplayName', width: 80, render: (v: string) => v || '—' },
                  { title: '主归属', dataIndex: 'primaryOrgNodeId', width: 100, render: (id: number) => orgOptionsForPerson.find((o) => o.id === id)?.name ?? id ?? '—' },
                  { title: '状态', dataIndex: 'isActive', width: 60, render: (v: boolean) => (v ? '在岗' : '停用') },
                  {
                    title: '操作',
                    key: 'action',
                    width: 120,
                    render: (_, row) => (
                      <Space size="small">
                        <Button type="link" size="small" onClick={() => openEditPerson(row.id)}>编辑</Button>
                        {row.isActive ? (
                          <Button type="link" size="small" danger onClick={() => handleDisablePerson(row)}>停用</Button>
                        ) : (
                          <Button type="link" size="small" onClick={() => handleEnablePerson(row)}>启用</Button>
                        )}
                      </Space>
                    ),
                  },
                ]) as ColumnsType<OrgPersonItem>}
              />
            </>
          ) : null}
                  </>
                ),
              },
              {
                key: 'positions',
                label: '职位配置',
                children: (
                  <>
                    <p style={{ color: '#666', fontSize: 12, marginBottom: 12 }}>
                      为本组织启用职位并绑定人员；主归属不在本组织的显示为「兼任」。
                    </p>
                    <Button type="primary" size="small" style={{ marginBottom: 16 }} onClick={() => { setEnablePositionSelectedIds([]); setEnablePositionModalOpen(true); }}>
                      启用职位
                    </Button>
                    <Spin spinning={positionsLoading}>
                      {positionsByOrg?.enabledPositions && positionsByOrg.enabledPositions.length > 0 ? (
                        <Space direction="vertical" style={{ width: '100%' }}>
                          {positionsByOrg.enabledPositions.map((pos: EnabledPositionWithBindings) => {
                            const key = `${positionsByOrg.orgNodeId}-${pos.positionId}`;
                            const saving = bindingsSaving[key];
                            return (
                              <Card size="small" key={key} title={<span>{pos.displayName}{pos.shortName ? ` (${pos.shortName})` : ''}</span>}>
                                <Space direction="vertical" style={{ width: '100%' }}>
                                  <div>
                                    <span style={{ marginRight: 8 }}>绑定人员：</span>
                                    <OrgPersonSelect
                                      value={bindingDraft[key] ?? pos.boundPeople.map((p) => p.id)}
                                      onChange={(ids) => setBindingDraft((prev) => ({ ...prev, [key]: ids }))}
                                      placeholder="选择人员（可多选）"
                                    />
                                    <Button type="primary" size="small" loading={saving} style={{ marginLeft: 8 }} onClick={() => handleSaveBindings(pos.positionId, bindingDraft[key] ?? pos.boundPeople.map((p) => p.id))}>
                                      保存
                                    </Button>
                                    <Button size="small" style={{ marginLeft: 8 }} onClick={() => handleDisablePosition(pos.positionId)}>停用职位</Button>
                                  </div>
                                  {pos.boundPeople.length > 0 && (
                                    <div style={{ fontSize: 12, color: '#666' }}>
                                      {pos.boundPeople.map((p) => (
                                        <Tag key={p.id}>{p.name}{p.isPartTime ? ' 兼任' : ''}</Tag>
                                      ))}
                                    </div>
                                  )}
                                </Space>
                              </Card>
                            );
                          })}
                        </Space>
                      ) : !positionsLoading && positionsByOrg ? (
                        <p style={{ color: '#999' }}>暂无已启用职位，请点击「启用职位」从公司职位库中选择。</p>
                      ) : null}
                    </Spin>
                  </>
                ),
              },
            ]}
          />
        )}
      </Card>
      </div>

      <Modal
        title="新建节点"
        open={modalOpen}
        onOk={handleCreate}
        onCancel={() => setModalOpen(false)}
        confirmLoading={submitting}
        destroyOnClose
        okButtonProps={{
          disabled: levels.length === 0 || createNoNextLevel,
        }}
      >
        {levels.length === 0 && (
          <Alert
            type="warning"
            message="请先在层级配置启用至少一个层级"
            style={{ marginBottom: 12 }}
          />
        )}
        {createNoNextLevel && (
          <Alert
            type="warning"
            message="已没有可用的下级层级，请先在层级配置启用/新增层级"
            style={{ marginBottom: 12 }}
          />
        )}
        <Form form={form} layout="vertical">
          <Form.Item label="上级">
            <span style={{ color: 'rgba(0,0,0,0.88)' }}>{createParentPath}</span>
          </Form.Item>
          <Form.Item label="层级" extra="层级由入口自动确定">
            <span style={{ color: 'rgba(0,0,0,0.88)' }}>{createLevelDisplayName || '—'}</span>
          </Form.Item>
          <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="isEnabled" label="启用" valuePropName="checked">
            <Checkbox defaultChecked />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="编辑组织"
        open={editNodeModalOpen}
        onOk={handleEditNodeSave}
        onCancel={() => { setEditNodeModalOpen(false); setEditNodeId(null); }}
        confirmLoading={submitting}
        destroyOnClose
      >
        <Form form={editForm} layout="vertical">
          <Form.Item name="name" label="名称" rules={[{ required: true, message: '请输入名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="isEnabled" label="启用" valuePropName="checked">
            <Checkbox />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={personEditingId == null ? '新建人员' : '编辑人员'}
        open={personModalOpen}
        onOk={handlePersonSubmit}
        onCancel={() => setPersonModalOpen(false)}
        confirmLoading={personSubmitting}
        width={520}
        destroyOnClose
      >
        <Form form={personForm} layout="vertical">
          <Form.Item name="name" label="姓名" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="手机" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="jobLevelId" label="职级" rules={[{ required: true }]}>
            <Select placeholder="选择职级" options={jobLevels.map((j) => ({ label: j.displayName, value: j.id }))} />
          </Form.Item>
          <Form.Item name="primaryOrgNodeId" label="主归属组织" rules={[{ required: true }]}>
            <Select
              placeholder="选择组织"
              showSearch
              optionFilterProp="label"
              options={orgOptionsForPerson.map((o) => ({ label: o.path, value: o.id }))}
            />
          </Form.Item>
          <Form.Item name="secondaryOrgNodeIds" label="次要归属（可多选）">
            <Select mode="multiple" placeholder="选填" allowClear options={orgOptionsForPerson.map((o) => ({ label: o.path, value: o.id }))} />
          </Form.Item>
          {personEditingId != null && personAssignments.length > 0 && (
            <Form.Item label="任职列表">
              <ul style={{ margin: 0, paddingLeft: 20, fontSize: 13 }}>
                {personAssignments.map((a, i) => (
                  <li key={i}>
                    {a.orgNodeNamePath} — {a.positionShortName ?? a.positionDisplayName}
                    {a.isPartTime && <Tag color="blue" style={{ marginLeft: 8 }}>兼任</Tag>}
                  </li>
                ))}
              </ul>
            </Form.Item>
          )}
        </Form>
      </Modal>

      <Modal
        title="启用职位"
        open={enablePositionModalOpen}
        onOk={handleEnablePositionsConfirm}
        onCancel={() => { setEnablePositionModalOpen(false); setEnablePositionSelectedIds([]); }}
        okText="确定启用"
        destroyOnClose
      >
        <p style={{ marginBottom: 12, color: '#666', fontSize: 12 }}>从公司职位库中选择要在此组织启用的职位（已启用的不显示）：</p>
        <Checkbox.Group
          style={{ display: 'flex', flexDirection: 'column', gap: 8 }}
          value={enablePositionSelectedIds}
          onChange={(vals) => setEnablePositionSelectedIds(vals as number[])}
        >
          {(positionCatalog || []).filter((c) => !positionsByOrg?.enabledPositions?.some((p) => p.positionId === c.id)).map((c) => (
            <Checkbox key={c.id} value={c.id}>
              {c.displayName}{c.shortName ? ` (${c.shortName})` : ''}
            </Checkbox>
          ))}
        </Checkbox.Group>
        {positionCatalog.length > 0 && positionsByOrg?.enabledPositions && positionCatalog.filter((c) => !positionsByOrg?.enabledPositions?.some((p) => p.positionId === c.id)).length === 0 && (
          <p style={{ marginTop: 12, color: '#999', fontSize: 12 }}>当前已启用全部职位库中的职位。</p>
        )}
      </Modal>

      <Modal
        title="移动节点"
        open={moveModalOpen}
        onCancel={() => { setMoveModalOpen(false); setMoveTargetId(null); setImpact(null); }}
        footer={null}
        destroyOnClose
      >
        {impact != null && (
          <p>
            此操作将影响 <strong>{impact.descendantNodeCount}</strong> 个下级节点、
            <strong>{impact.personCountInSubtree}</strong> 名在岗/启用人员归属。请在新位置选择上级节点（输入节点ID，0 表示根）。
          </p>
        )}
        <Form
          onFinish={(v) => {
            const raw = v.newParentId;
            if (raw === '' || raw === undefined || raw === null || Number(raw) === 0) handleMove(null);
            else handleMove(Number(raw));
          }}
          layout="vertical"
        >
          <Form.Item name="newParentId" label="新上级节点ID（0或空=根）">
            <Input type="number" placeholder="0 或 节点ID" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting}>
              确认移动
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
