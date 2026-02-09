import { useEffect, useState } from 'react';
import { Button, DatePicker, message, Select, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { listChangeLogs, resetOrgStructureAll, type ChangeLogItem } from '@/features/orgstructure/services/orgStructure';
import { OrgTreeSelect } from '@/features/orgstructure/components/OrgTreeSelect';
import { TypeToConfirmModal } from '@/shared/components/TypeToConfirmModal';
import dayjs from 'dayjs';

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  message.error(err?.response?.data?.message ?? err?.message ?? '操作失败');
}

const OBJECT_TYPE_OPTIONS = [
  { label: '全部', value: '' },
  { label: '层级', value: 'LEVEL' },
  { label: '组织', value: 'ORG_NODE' },
  { label: '人员', value: 'PERSON' },
];

const OBJECT_TYPE_LABELS: Record<string, string> = {
  LEVEL: '层级',
  ORG_LEVEL: '层级',
  ORG_NODE: '组织',
  PERSON: '人员',
  ORG_PERSON: '人员',
  ORG_POSITION: '职位',
};

const ACTION_LABELS: Record<string, string> = {
  CREATE: '新增',
  UPDATE: '编辑',
  MOVE: '移动',
  ENABLE: '启用',
  DISABLE: '停用',
  ORG_POSITION_ENABLE: '启用职位',
  ORG_POSITION_DISABLE: '停用职位',
  ORG_POSITION_BINDINGS_UPDATE: '更新职位绑定',
  TEMPLATE_APPLIED_ON_ORG_CREATE: '新建组织套用职位模板',
};

const COMBINED_ACTION_LABELS: Record<string, string> = {
  'LEVEL:CREATE': '新增层级',
  'LEVEL:UPDATE': '编辑层级',
  'LEVEL:ENABLE': '启用层级',
  'LEVEL:DISABLE': '停用层级',
  'ORG_NODE:CREATE': '新增组织',
  'ORG_NODE:UPDATE': '编辑组织',
  'ORG_NODE:MOVE': '移动组织',
  'ORG_NODE:ENABLE': '启用组织',
  'ORG_NODE:DISABLE': '停用组织',
  'ORG_NODE:ORG_POSITION_ENABLE': '启用职位',
  'ORG_NODE:ORG_POSITION_DISABLE': '停用职位',
  'ORG_NODE:ORG_POSITION_BINDINGS_UPDATE': '更新职位绑定',
  'ORG_NODE:TEMPLATE_APPLIED_ON_ORG_CREATE': '新建组织套用职位模板',
  'PERSON:CREATE': '新增人员',
  'PERSON:UPDATE': '编辑人员',
  'PERSON:ENABLE': '启用人员',
  'PERSON:DISABLE': '停用人员',
};

function getObjectTypeLabel(code: string | undefined): string {
  if (code == null || code === '') return '—';
  return OBJECT_TYPE_LABELS[code] ?? '其他';
}

function getActionLabel(objectType: string | undefined, action: string | undefined): string {
  if (action == null || action === '') return '—';
  const key = objectType && action ? `${objectType}:${action}` : '';
  const combined = key ? COMBINED_ACTION_LABELS[key] : null;
  if (combined) return combined;
  return ACTION_LABELS[action] ?? '其他操作';
}

export default function OrgChangeLogPage() {
  const [data, setData] = useState<ChangeLogItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [objectType, setObjectType] = useState<string>('');
  const [orgNodeId, setOrgNodeId] = useState<number | null>(null);
  const [from, setFrom] = useState<string | undefined>(undefined);
  const [to, setTo] = useState<string | undefined>(undefined);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [resetAllModalOpen, setResetAllModalOpen] = useState(false);
  const [resetAllSubmitting, setResetAllSubmitting] = useState(false);

  const fetchList = async () => {
    setLoading(true);
    try {
      const res = await listChangeLogs({
        objectType: orgNodeId != null ? 'ORG_NODE' : (objectType || undefined),
        objectId: orgNodeId ?? undefined,
        from,
        to,
        page,
        size: pageSize,
      });
      setData(res.content ?? []);
      setTotal(res.totalElements ?? 0);
    } catch {
      setData([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();
  }, [objectType, orgNodeId, from, to, page, pageSize]);

  const columns: ColumnsType<ChangeLogItem> = [
    { title: '时间', dataIndex: 'timestamp', width: 180, render: (v: string) => v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '-' },
    { title: '对象类型', dataIndex: 'objectType', width: 90, render: (v: string) => getObjectTypeLabel(v) },
    { title: '对象ID', dataIndex: 'objectId', width: 80 },
    { title: '操作', dataIndex: 'action', width: 120, render: (_: string, row: ChangeLogItem) => getActionLabel(row.objectType, row.action) },
    { title: '操作人', dataIndex: 'operatorName', width: 100 },
    { title: '摘要', dataIndex: 'summaryText', ellipsis: true },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Space style={{ marginBottom: 16 }} wrap>
        <Button size="small" danger onClick={() => setResetAllModalOpen(true)}>
          清空测试数据
        </Button>
        <Select
          placeholder="对象类型"
          style={{ width: 120 }}
          options={OBJECT_TYPE_OPTIONS}
          value={objectType}
          onChange={setObjectType}
          disabled={orgNodeId != null}
        />
        <OrgTreeSelect
          placeholder="组织（筛选该节点的变更）"
          value={orgNodeId}
          onChange={setOrgNodeId}
          style={{ minWidth: 220 }}
        />
        <DatePicker.RangePicker
          onChange={(dates) => {
            if (dates && dates[0]) setFrom(dates[0].format('YYYY-MM-DD'));
            else setFrom(undefined);
            if (dates && dates[1]) setTo(dates[1].format('YYYY-MM-DD'));
            else setTo(undefined);
          }}
        />
      </Space>
      <TypeToConfirmModal
        open={resetAllModalOpen}
        onCancel={() => setResetAllModalOpen(false)}
        title="清空组织架构测试数据"
        description={
          <>
            <p style={{ marginBottom: 8 }}><strong>将清空（仅组织架构域）：</strong></p>
            <ul style={{ marginBottom: 8, paddingLeft: 20 }}>
              <li>职位绑定、职位启用配置、人员归属、组织人员、组织节点、变更记录、层级</li>
            </ul>
            <p style={{ marginBottom: 0 }}><strong>将恢复：</strong>默认层级 + 根节点「公司」。不会触及账号、权限等。</p>
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
            fetchList();
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
      <Table
        rowKey="id"
        columns={columns}
        dataSource={data}
        loading={loading}
        pagination={{
          current: page,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (t) => `共 ${t} 条`,
          onChange: (p, ps) => {
            setPage(p);
            if (ps) setPageSize(ps);
          },
        }}
      />
    </div>
  );
}
