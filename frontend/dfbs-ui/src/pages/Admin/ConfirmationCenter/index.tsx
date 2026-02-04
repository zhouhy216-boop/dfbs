import { useEffect, useState } from 'react';
import { Tabs, Table, Button, Modal, Form, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import request from '@/utils/request';
import SmartReferenceSelect from '@/components/SmartReferenceSelect';
import type { EntityType } from '@/components/SmartReferenceSelect';

interface TempPoolItem {
  id: number;
  entityType: string;
  uniqueKey: string;
  displayName: string;
}

type TempPoolData = Record<string, TempPoolItem[]>;

const ENTITY_LABELS: Record<string, string> = {
  CUSTOMER: '客户',
  PART: '零部件',
  MACHINE: '机器',
  CONTRACT: '合同',
  SIM: 'SIM卡',
  MODEL: '型号',
};

const ENTITY_TAB_ORDER = ['CUSTOMER', 'PART', 'MACHINE', 'CONTRACT', 'SIM', 'MODEL'];

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  message.error(err?.response?.data?.message ?? err?.message ?? '操作失败');
}

export default function ConfirmationCenter() {
  const [pool, setPool] = useState<TempPoolData>({});
  const [loading, setLoading] = useState(true);
  const [confirmModalOpen, setConfirmModalOpen] = useState(false);
  const [confirmingRow, setConfirmingRow] = useState<TempPoolItem | null>(null);
  const [confirmLoading, setConfirmLoading] = useState(false);
  const [targetId, setTargetId] = useState<number | null>(null);
  const [finalName, setFinalName] = useState<string>('');

  const fetchPool = async () => {
    setLoading(true);
    try {
      const { data } = await request.get<TempPoolData>('/v1/temp-pool');
      setPool(data ?? {});
    } catch {
      setPool({});
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPool();
  }, []);

  const openConfirmModal = (row: TempPoolItem) => {
    setConfirmingRow(row);
    setTargetId(null);
    setFinalName(row.displayName ?? row.uniqueKey ?? '');
    setConfirmModalOpen(true);
  };

  const handleConfirm = async () => {
    if (!confirmingRow) return;
    const nameToSend = (finalName ?? '').trim();
    if (!nameToSend && targetId == null) {
      message.error('请选择已有记录或输入新名称');
      return;
    }
    if (targetId != null && targetId === confirmingRow.id) {
      message.error('不能合并到自身');
      return;
    }
    setConfirmLoading(true);
    try {
      await request.post('/v1/temp-pool/confirm', {
        id: confirmingRow.id,
        entityType: confirmingRow.entityType,
        targetId: targetId ?? undefined,
        finalValues: { name: nameToSend || undefined },
      });
      message.success(targetId != null ? '已合并到目标记录' : '已转为正式数据');
      setConfirmModalOpen(false);
      setConfirmingRow(null);
      setTargetId(null);
      setFinalName('');
      await fetchPool();
    } catch (e) {
      showError(e);
    } finally {
      setConfirmLoading(false);
    }
  };

  const columns: ColumnsType<TempPoolItem> = [
    { title: 'ID', dataIndex: 'id', width: 72, ellipsis: true },
    { title: '关键标识', dataIndex: 'displayName', ellipsis: true, render: (_, r) => r.displayName || r.uniqueKey },
    {
      title: '状态',
      key: 'status',
      width: 100,
      render: () => <Tag color="orange">待确认</Tag>,
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, row) => (
        <Button type="link" size="small" onClick={() => openConfirmModal(row)}>
          确认/修正
        </Button>
      ),
    },
  ];

  const tabItems = ENTITY_TAB_ORDER.map((key) => {
    const list = pool[key] ?? [];
    return {
      key,
      label: `${ENTITY_LABELS[key] ?? key} (${list.length})`,
      children: (
        <Table
          rowKey="id"
          size="small"
          loading={loading}
          columns={columns}
          dataSource={list}
          pagination={{ pageSize: 10, showSizeChanger: false }}
        />
      ),
    };
  });

  return (
    <div style={{ padding: 24 }}>
      <h2 style={{ marginBottom: 16 }}>数据确认中心</h2>
      <p style={{ color: '#666', marginBottom: 16 }}>
        选择已有记录即合并；输入新名称即转为正式（系统自动生成编码）。
      </p>
      <Tabs items={tabItems} />

      <Modal
        title="确认 / 修正"
        open={confirmModalOpen}
        onOk={handleConfirm}
        onCancel={() => {
          setConfirmModalOpen(false);
          setConfirmingRow(null);
          setTargetId(null);
          setFinalName('');
        }}
        confirmLoading={confirmLoading}
        okText="确认"
        cancelText="取消"
        destroyOnClose
      >
        {confirmingRow && (
          <>
            <p style={{ marginBottom: 16 }}>
              类型：<Tag>{ENTITY_LABELS[confirmingRow.entityType] ?? confirmingRow.entityType}</Tag>
              &nbsp; ID：{confirmingRow.id}
            </p>
            <Form layout="vertical" style={{ marginTop: 8 }}>
              <Form.Item
                label={targetId != null ? '合并到（已选）' : '选择已有记录或输入新名称'}
                required
              >
                <SmartReferenceSelect
                  entityType={confirmingRow.entityType as EntityType}
                  value={finalName}
                  onChange={(ref) => {
                    setTargetId(ref.id ?? null);
                    setFinalName(ref.name);
                  }}
                  placeholder={
                    targetId != null
                      ? '已选择目标，可改选或清空后输入新名称'
                      : `选择要合并到的${ENTITY_LABELS[confirmingRow.entityType] ?? confirmingRow.entityType}，或直接输入新名称转为正式`
                  }
                />
              </Form.Item>
            </Form>
          </>
        )}
      </Modal>
    </div>
  );
}
