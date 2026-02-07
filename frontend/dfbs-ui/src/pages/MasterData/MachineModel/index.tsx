import { useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ProTable,
  ModalForm,
  ProFormText,
} from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Button, message } from 'antd';
import request from '@/shared/utils/request';
import { toProTableResult, type SpringPage } from '@/shared/utils/adapters';

interface MachineModelRow {
  id: number;
  modelName?: string;
  modelNo: string;
  freightInfo?: string;
  warrantyInfo?: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
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

export default function MachineModel() {
  const actionRef = useRef<ActionType>(null);
  const navigate = useNavigate();

  const columns: ProColumns<MachineModelRow>[] = [
    { title: 'ID', dataIndex: 'id', width: 80, search: false },
    { title: '型号名称', dataIndex: 'modelName', width: 160 },
    { title: '型号编号', dataIndex: 'modelNo', width: 140 },
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
        <a key="detail" onClick={() => navigate(`/master-data/machine-models/${row.id}`)}>详情</a>,
        row.status === 'ENABLE' && (
          <a
            key="disable"
            onClick={async () => {
              try {
                await request.post(`/v1/masterdata/machine-models/${row.id}/disable`, { updatedBy: 'user' });
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
      <ProTable<MachineModelRow>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          const page = (params.current ?? 1) - 1;
          const size = params.pageSize ?? 10;
          const { data } = await request.get<SpringPage<MachineModelRow>>('/v1/masterdata/machine-models', {
            params: {
              keyword: params.modelName ?? undefined,
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
        headerTitle="机器型号"
        toolBarRender={() => [
          <ModalForm<{ modelName?: string; modelNo: string; freightInfo?: string; warrantyInfo?: string; createdBy?: string }>
            key="create"
            title="新建机器型号"
            trigger={<Button type="primary">新建</Button>}
            onFinish={async (values) => {
              try {
                await request.post('/v1/masterdata/machine-models', {
                  modelName: values.modelName,
                  modelNo: values.modelNo,
                  freightInfo: values.freightInfo,
                  warrantyInfo: values.warrantyInfo,
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
            <ProFormText name="modelName" label="型号名称" />
            <ProFormText name="modelNo" label="型号编号" rules={[{ required: true }]} />
            <ProFormText name="freightInfo" label="运费信息" />
            <ProFormText name="warrantyInfo" label="保修信息" />
            <ProFormText name="createdBy" label="创建人" initialValue="user" />
          </ModalForm>,
        ]}
      />
    </div>
  );
}
