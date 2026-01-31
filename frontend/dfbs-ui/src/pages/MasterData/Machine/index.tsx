import { useRef } from 'react';
import {
  ProTable,
  ModalForm,
  ProFormText,
  ProFormSelect,
} from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Button, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import request from '@/utils/request';
import { toProTableResult, type SpringPage } from '@/utils/adapters';

interface MachineRow {
  id: number;
  machineNo: string;
  serialNo: string;
  customerId?: number;
  contractId?: number;
  modelId?: number;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

interface CustomerOption {
  id: number;
  customerCode: string;
  name: string;
}

interface ModelOption {
  id: number;
  modelNo: string;
  modelName?: string;
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

export default function Machine() {
  const navigate = useNavigate();
  const actionRef = useRef<ActionType>(null);

  const columns: ProColumns<MachineRow>[] = [
    { title: 'ID', dataIndex: 'id', width: 80, search: false },
    { title: '机器编号', dataIndex: 'machineNo', width: 140 },
    { title: '序列号', dataIndex: 'serialNo', width: 140 },
    { title: '客户ID', dataIndex: 'customerId', width: 100, search: false },
    { title: '型号ID', dataIndex: 'modelId', width: 100, search: false },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      valueType: 'select',
      fieldProps: { options: STATUS_OPTIONS },
    },
    { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime', width: 180, search: false },
    {
      title: '操作',
      valueType: 'option',
      width: 180,
      render: (_, row) => [
        <a key="detail" onClick={() => navigate(`/master-data/machines/${row.id}`)}>详情</a>,
        row.status === 'ENABLE' && (
          <a
            key="disable"
            onClick={async () => {
              try {
                await request.post(`/v1/masterdata/machines/${row.id}/disable`, { updatedBy: 'user' });
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

  return (
    <div style={{ padding: 24 }}>
      <ProTable<MachineRow>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          const page = (params.current ?? 1) - 1;
          const size = params.pageSize ?? 10;
          const { data } = await request.get<SpringPage<MachineRow>>('/v1/masterdata/machines', {
            params: {
              keyword: params.machineNo ?? params.serialNo ?? undefined,
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
        headerTitle="机器"
        toolBarRender={() => [
          <ModalForm<{
            machineNo: string;
            serialNo: string;
            customerId?: number;
            contractId?: number;
            modelId?: number;
            createdBy?: string;
          }>
            key="create"
            title="新建机器"
            trigger={<Button type="primary">新建</Button>}
            onFinish={async (values) => {
              try {
                await request.post('/v1/masterdata/machines', {
                  machineNo: values.machineNo,
                  serialNo: values.serialNo,
                  customerId: values.customerId ?? null,
                  contractId: values.contractId ?? null,
                  modelId: values.modelId ?? null,
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
            <ProFormText name="machineNo" label="机器编号" />
            <ProFormText name="serialNo" label="序列号" rules={[{ required: true }]} />
            <ProFormSelect
              name="customerId"
              label="客户"
              allowClear
              request={async () => {
                const { data } = await request.get<SpringPage<CustomerOption>>('/v1/customers', {
                  params: { page: 0, size: 200, sort: 'id,asc' },
                });
                return (data?.content ?? []).map((c) => ({ label: `${c.name} (${c.customerCode})`, value: c.id }));
              }}
            />
            <ProFormSelect
              name="modelId"
              label="型号"
              allowClear
              request={async () => {
                const { data } = await request.get<SpringPage<ModelOption>>('/v1/masterdata/machine-models', {
                  params: { page: 0, size: 200, sort: 'id,asc' },
                });
                return (data?.content ?? []).map((m) => ({ label: `${m.modelNo} ${m.modelName ?? ''}`.trim(), value: m.id }));
              }}
            />
            <ProFormText name="createdBy" label="创建人" initialValue="user" />
          </ModalForm>,
        ]}
      />
    </div>
  );
}
