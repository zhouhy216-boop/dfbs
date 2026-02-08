import { useEffect, useState } from 'react';
import { Alert, Button, Form, Input, InputNumber, message, Modal, Space, Switch, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  listConfigurableLevels,
  createLevel,
  updateLevel,
  canResetLevels,
  resetLevelsToDefault,
  getResetAvailability,
  resetOrgStructureTooling,
  type OrgLevelItem,
  type ResetAvailabilityResult,
} from '@/features/orgstructure/services/orgStructure';
import { TypeToConfirmModal } from '@/shared/components/TypeToConfirmModal';

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  message.error(err?.response?.data?.message ?? err?.message ?? '操作失败');
}

export default function OrgLevelConfigPage() {
  const [list, setList] = useState<OrgLevelItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [canReset, setCanReset] = useState(false);
  const [resetMessage, setResetMessage] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [resetAvailability, setResetAvailability] = useState<ResetAvailabilityResult | null>(null);
  const [toolingResetModalOpen, setToolingResetModalOpen] = useState(false);
  const [form] = Form.useForm<{ orderIndex: number; displayName: string; isEnabled?: boolean }>();

  const fetchResetAvailability = async () => {
    try {
      const res = await getResetAvailability();
      setResetAvailability(res);
    } catch {
      setResetAvailability({ allowed: false, reason: '无法获取' });
    }
  };

  const fetchList = async () => {
    setLoading(true);
    try {
      const data = await listConfigurableLevels();
      setList(data);
    } catch (e) {
      showError(e);
    } finally {
      setLoading(false);
    }
  };

  const fetchCanReset = async () => {
    try {
      const res = await canResetLevels();
      setCanReset(res.canReset);
      setResetMessage(res.message ?? null);
    } catch {
      setCanReset(false);
      setResetMessage('无法检查');
    }
  };

  useEffect(() => {
    fetchList();
    fetchCanReset();
    fetchResetAvailability();
  }, []);

  const openCreate = () => {
    setEditingId(null);
    const nextOrder = list.length === 0 ? 2 : Math.max(...list.map((l) => l.orderIndex), 1) + 1;
    form.setFieldsValue({ orderIndex: Math.min(nextOrder, 8), displayName: '', isEnabled: true });
    setModalOpen(true);
  };

  const openEdit = (row: OrgLevelItem) => {
    setEditingId(row.id);
    form.setFieldsValue({
      orderIndex: row.orderIndex,
      displayName: row.displayName,
      isEnabled: row.isEnabled,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      if (editingId == null) {
        await createLevel({ orderIndex: values.orderIndex ?? 0, displayName: values.displayName?.trim() ?? '' });
        message.success('新建成功');
      } else {
        await updateLevel(editingId, {
          orderIndex: values.orderIndex,
          displayName: values.displayName?.trim(),
          isEnabled: values.isEnabled,
        });
        message.success('更新成功');
      }
      setModalOpen(false);
      fetchList();
      fetchCanReset();
    } catch (e) {
      if (e instanceof Error && e.message?.includes('validateFields')) return;
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleResetToDefault = () => {
    Modal.confirm({
      title: '重置为默认层级',
      content: '将恢复公司固定根层级下的默认层级（本部、部、课、系、班）。仅当当前未被任何组织节点或人员归属使用时可用。确定继续？',
      onOk: async () => {
        try {
          setSubmitting(true);
          await resetLevelsToDefault();
          message.success('已重置为默认层级');
          fetchList();
          fetchCanReset();
          fetchResetAvailability();
        } catch (e) {
          showError(e);
        } finally {
          setSubmitting(false);
        }
      },
    });
  };

  const handleToggleEnabled = async (row: OrgLevelItem, enabled: boolean) => {
    try {
      await updateLevel(row.id, { isEnabled: enabled });
      message.success(enabled ? '已启用' : '已停用');
      fetchList();
      fetchCanReset();
    } catch (e) {
      showError(e);
    }
  };

  const columns: ColumnsType<OrgLevelItem> = [
    {
      title: '层级顺序（公司下的第几层）',
      dataIndex: 'orderIndex',
      width: 160,
      render: (v: number) => `第${v}层`,
    },
    { title: '显示名称', dataIndex: 'displayName' },
    {
      title: '状态',
      dataIndex: 'isEnabled',
      width: 80,
      render: (v: boolean) => (v ? '启用' : '停用'),
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_, row) => (
        <Space>
          <Button type="link" size="small" onClick={() => openEdit(row)}>
            编辑
          </Button>
          {row.isEnabled ? (
            <Button type="link" size="small" danger onClick={() => handleToggleEnabled(row, false)}>
              停用
            </Button>
          ) : (
            <Button type="link" size="small" onClick={() => handleToggleEnabled(row, true)}>
              启用
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Alert
        type="info"
        showIcon
        message="公司为系统固定根层级；此处配置公司下的层级（例如 本部/部/课/系/班）。"
        style={{ marginBottom: 16 }}
      />
      <div style={{ marginBottom: 16, display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
        <Button type="primary" onClick={openCreate}>
          新建层级
        </Button>
        <Button
          onClick={handleResetToDefault}
          disabled={!canReset}
          loading={submitting}
        >
          重置为默认层级（仅当层级未被使用时）
        </Button>
        {!canReset && (
          <span style={{ color: '#666', fontSize: 12 }}>
            {resetMessage ?? '已有组织节点/人员归属使用层级，需先迁移/清理后才能重置'}
          </span>
        )}
        <Button
          onClick={() => setToolingResetModalOpen(true)}
          disabled={resetAvailability != null && !resetAvailability.allowed}
          style={{ marginLeft: 8 }}
        >
          清空组织树并恢复默认（测试用）
        </Button>
        {resetAvailability != null && !resetAvailability.allowed && resetAvailability.reason && (
          <span style={{ color: '#666', fontSize: 12 }}>{resetAvailability.reason}</span>
        )}
      </div>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={list}
        loading={loading}
        pagination={false}
      />
      <Modal
        title={editingId == null ? '新建层级' : '编辑层级'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        confirmLoading={submitting}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="orderIndex"
            label="层级顺序（第几层）"
            tooltip="公司为第1层（系统固定）；此处为公司下的顺序：第2层、第3层…"
            rules={[{ required: true }]}
          >
            <InputNumber min={2} max={8} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="displayName" label="显示名称" rules={[{ required: true, message: '请输入显示名称' }]}>
            <Input placeholder="如：本部、部、课、系" />
          </Form.Item>
          {editingId != null && (
            <Form.Item name="isEnabled" label="启用" valuePropName="checked">
              <Switch />
            </Form.Item>
          )}
        </Form>
      </Modal>

      <TypeToConfirmModal
        open={toolingResetModalOpen}
        onCancel={() => setToolingResetModalOpen(false)}
        title="清空组织树并恢复默认（测试用）"
        description={
          <>
            <p style={{ marginBottom: 8 }}><strong>将清空：</strong></p>
            <ul style={{ marginBottom: 8, paddingLeft: 20 }}>
              <li>组织树（org_node）</li>
              <li>变更记录（org_change_log）</li>
            </ul>
            <p style={{ marginBottom: 8 }}><strong>将恢复：</strong></p>
            <ul style={{ marginBottom: 8, paddingLeft: 20 }}>
              <li>默认层级（公司固定不显示在列表中 + 公司下默认层级 本部/部/课/系/班）</li>
            </ul>
            <p style={{ marginBottom: 0 }}><strong>不会触及：</strong>人员通讯录、归属、账号与权限、职级字典。若存在人员或归属数据，此操作将被禁止。</p>
          </>
        }
        confirmText="RESET"
        transformInput={(s) => s.toUpperCase()}
        onConfirm={async () => {
          setSubmitting(true);
          try {
            await resetOrgStructureTooling({ confirmText: 'RESET' });
            message.success('已清空组织树并恢复默认');
            setToolingResetModalOpen(false);
            fetchList();
            fetchCanReset();
            fetchResetAvailability();
          } catch (e) {
            showError(e);
            throw e;
          } finally {
            setSubmitting(false);
          }
        }}
        okText="确认清空"
        danger
        loading={submitting}
      />
    </div>
  );
}
