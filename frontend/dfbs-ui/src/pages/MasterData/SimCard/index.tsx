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

interface SimCardRow {
  id: number;
  cardNo: string;
  operator?: string;
  planInfo?: string;
  machineId?: number;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

interface MachineOption {
  id: number;
  machineNo: string;
  serialNo: string;
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

export default function SimCard() {
  const navigate = useNavigate();
  const actionRef = useRef<ActionType>(null);

  const columns: ProColumns<SimCardRow>[] = [
    { title: 'ID', dataIndex: 'id', width: 80, search: false },
    { title: '卡号', dataIndex: 'cardNo', width: 160 },
    { title: '运营商', dataIndex: 'operator', width: 120, search: false },
    { title: '机器ID', dataIndex: 'machineId', width: 100, search: false },
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
        <a key="detail" onClick={() => navigate(`/master-data/sim-cards/${row.id}`)}>详情</a>,
        row.status === 'ENABLE' && (
          <a
            key="disable"
            onClick={async () => {
              try {
                await request.post(`/v1/masterdata/sim-cards/${row.id}/disable`, { updatedBy: 'user' });
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
      <ProTable<SimCardRow>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          const page = (params.current ?? 1) - 1;
          const size = params.pageSize ?? 10;
          const { data } = await request.get<SpringPage<SimCardRow>>('/v1/masterdata/sim-cards', {
            params: {
              keyword: params.cardNo ?? undefined,
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
        headerTitle="SIM卡"
        toolBarRender={() => [
          <ModalForm<{ cardNo: string; operator?: string; planInfo?: string; machineId?: number; createdBy?: string }>
            key="create"
            title="新建SIM卡"
            trigger={<Button type="primary">新建</Button>}
            onFinish={async (values) => {
              try {
                await request.post('/v1/masterdata/sim-cards', {
                  cardNo: values.cardNo,
                  operator: values.operator,
                  planInfo: values.planInfo,
                  machineId: values.machineId ?? null,
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
            <ProFormText name="cardNo" label="卡号" rules={[{ required: true }]} />
            <ProFormText name="operator" label="运营商" />
            <ProFormText name="planInfo" label="套餐信息" />
            <ProFormSelect
              name="machineId"
              label="绑定机器"
              allowClear
              request={async () => {
                const { data } = await request.get<SpringPage<MachineOption>>('/v1/masterdata/machines', {
                  params: { page: 0, size: 200, sort: 'id,asc' },
                });
                return (data?.content ?? []).map((m) => ({
                  label: `${m.machineNo} / ${m.serialNo}`,
                  value: m.id,
                }));
              }}
            />
            <ProFormText name="createdBy" label="创建人" initialValue="user" />
          </ModalForm>,
        ]}
      />
    </div>
  );
}
