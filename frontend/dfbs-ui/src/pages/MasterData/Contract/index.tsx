import { useRef, useState } from 'react';
import {
  ProTable,
  ModalForm,
  ProFormText,
  ProFormSelect,
} from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Drawer, Descriptions, Button, message } from 'antd';
import request from '@/utils/request';
import { toProTableResult, type SpringPage } from '@/utils/adapters';

interface ContractRow {
  id: number;
  contractNo: string;
  customerId: number;
  startDate?: string;
  endDate?: string;
  attachment: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

interface CustomerOption {
  id: number;
  customerCode: string;
  name: string;
}

const STATUS_OPTIONS = [
  { label: 'ENABLE', value: 'ENABLE' },
  { label: 'DISABLE', value: 'DISABLE' },
];

function showError(e: unknown) {
  const status = (e as { response?: { status?: number } })?.response?.status;
  if (status === 409) {
    message.error('Data already exists');
    return;
  }
  message.error((e as { message?: string })?.message ?? '操作失败');
}

export default function Contract() {
  const actionRef = useRef<ActionType>(null);
  const [detailId, setDetailId] = useState<number | null>(null);
  const [detail, setDetail] = useState<ContractRow | null>(null);

  const columns: ProColumns<ContractRow>[] = [
    { title: 'ID', dataIndex: 'id', width: 80, search: false },
    { title: '合同号', dataIndex: 'contractNo', width: 160 },
    { title: '客户ID', dataIndex: 'customerId', width: 100, search: false },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      valueType: 'select',
      fieldProps: { options: STATUS_OPTIONS },
    },
    { title: '开始日期', dataIndex: 'startDate', width: 120, search: false },
    { title: '结束日期', dataIndex: 'endDate', width: 120, search: false },
    { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime', width: 180, search: false },
    {
      title: '操作',
      valueType: 'option',
      width: 180,
      render: (_, row) => [
        <a key="detail" onClick={() => loadDetail(row.id)}>详情</a>,
        row.status === 'ENABLE' && (
          <a
            key="disable"
            onClick={async () => {
              try {
                await request.post(`/v1/masterdata/contracts/${row.id}/disable`, { updatedBy: 'user' });
                message.success('已禁用');
                actionRef.current?.reload();
              } catch (err) {
                showError(err);
              }
            }}
          >
            禁用
          </a>
        ),
      ].filter(Boolean),
    },
  ];

  async function loadDetail(id: number) {
    setDetailId(id);
    try {
      const { data } = await request.get<ContractRow>(`/v1/masterdata/contracts/${id}`);
      setDetail(data);
    } catch {
      setDetail(null);
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <ProTable<ContractRow>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          const page = (params.current ?? 1) - 1;
          const size = params.pageSize ?? 10;
          const { data } = await request.get<SpringPage<ContractRow>>('/v1/masterdata/contracts', {
            params: {
              keyword: params.contractNo ?? undefined,
              status: params.status ?? undefined,
              page,
              size,
              sort: 'id,desc',
            },
          });
          return toProTableResult(data);
        }}
        rowKey="id"
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
        headerTitle="合同"
        toolBarRender={() => [
          <ModalForm<{ contractNo: string; customerId: number; attachment: string; createdBy?: string }>
            key="create"
            title="新建合同"
            trigger={<Button type="primary">新建</Button>}
            onFinish={async (values) => {
              try {
                await request.post('/v1/masterdata/contracts', {
                  contractNo: values.contractNo,
                  customerId: values.customerId,
                  attachment: values.attachment ?? 'Mock URL',
                  createdBy: values.createdBy ?? 'user',
                });
                message.success('创建成功');
                actionRef.current?.reload();
                return true;
              } catch (err) {
                showError(err);
                return false;
              }
            }}
          >
            <ProFormText name="contractNo" label="合同号" rules={[{ required: true }]} />
            <ProFormSelect
              name="customerId"
              label="客户"
              rules={[{ required: true }]}
              request={async () => {
                const { data } = await request.get<SpringPage<CustomerOption>>('/v1/customers', {
                  params: { page: 0, size: 200, sort: 'id,asc' },
                });
                return (data?.content ?? []).map((c) => ({ label: `${c.name} (${c.customerCode})`, value: c.id }));
              }}
            />
            <ProFormText name="attachment" label="附件" placeholder="Mock URL" rules={[{ required: true }]} />
            <ProFormText name="createdBy" label="创建人" initialValue="user" />
          </ModalForm>,
        ]}
      />
      <Drawer
        title="合同详情"
        open={detailId != null}
        onClose={() => setDetailId(null)}
        width={480}
      >
        {detail && (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="ID">{detail.id}</Descriptions.Item>
            <Descriptions.Item label="合同号">{detail.contractNo}</Descriptions.Item>
            <Descriptions.Item label="客户ID">{detail.customerId}</Descriptions.Item>
            <Descriptions.Item label="状态">{detail.status}</Descriptions.Item>
            <Descriptions.Item label="开始日期">{detail.startDate ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="结束日期">{detail.endDate ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="附件">{detail.attachment}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{detail.createdAt ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="更新时间">{detail.updatedAt ?? '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
}
