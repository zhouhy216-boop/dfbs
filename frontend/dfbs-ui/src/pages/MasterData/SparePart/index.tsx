import { useRef, useState } from 'react';
import {
  ProTable,
  ModalForm,
  ProFormText,
  ProFormDigit,
} from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Drawer, Descriptions, Button, message } from 'antd';
import request from '@/shared/utils/request';
import { toProTableResult, type SpringPage } from '@/shared/utils/adapters';

interface SparePartRow {
  id: number;
  partNo: string;
  name: string;
  spec?: string;
  unit?: string;
  referencePrice?: number;
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

export default function SparePart() {
  const actionRef = useRef<ActionType>(null);
  const [detailId, setDetailId] = useState<number | null>(null);
  const [detail, setDetail] = useState<SparePartRow | null>(null);

  const columns: ProColumns<SparePartRow>[] = [
    { title: 'ID', dataIndex: 'id', width: 80, search: false },
    { title: '配件编号', dataIndex: 'partNo', width: 140 },
    { title: '名称', dataIndex: 'name', ellipsis: true },
    { title: '规格', dataIndex: 'spec', ellipsis: true, search: false },
    { title: '单位', dataIndex: 'unit', width: 80, search: false },
    { title: '参考价', dataIndex: 'referencePrice', valueType: 'money', width: 120, search: false },
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
        <a key="detail" onClick={() => loadDetail(row.id)}>详情</a>,
        row.status === 'ENABLE' && (
          <a
            key="disable"
            onClick={async () => {
              try {
                await request.post(`/v1/masterdata/spare-parts/${row.id}/disable`, { updatedBy: 'user' });
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
      const { data } = await request.get<SparePartRow>(`/v1/masterdata/spare-parts/${id}`);
      setDetail(data);
    } catch {
      setDetail(null);
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <ProTable<SparePartRow>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          const page = (params.current ?? 1) - 1;
          const size = params.pageSize ?? 10;
          const { data } = await request.get<SpringPage<SparePartRow>>('/v1/masterdata/spare-parts', {
            params: {
              keyword: params.partNo ?? params.name ?? undefined,
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
        headerTitle="备件"
        toolBarRender={() => [
          <ModalForm<{ partNo: string; name: string; spec?: string; unit?: string; referencePrice?: number; createdBy?: string }>
            key="create"
            title="新建备件"
            trigger={<Button type="primary">新建</Button>}
            onFinish={async (values) => {
              try {
                await request.post('/v1/masterdata/spare-parts', {
                  partNo: values.partNo,
                  name: values.name,
                  spec: values.spec,
                  unit: values.unit ?? '个',
                  referencePrice: values.referencePrice,
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
            <ProFormText name="partNo" label="配件编号" rules={[{ required: true }]} />
            <ProFormText name="name" label="名称" rules={[{ required: true }]} />
            <ProFormText name="spec" label="规格" />
            <ProFormText name="unit" label="单位" initialValue="个" />
            <ProFormDigit name="referencePrice" label="参考价" min={0} fieldProps={{ precision: 2 }} />
            <ProFormText name="createdBy" label="创建人" initialValue="user" />
          </ModalForm>,
        ]}
      />
      <Drawer
        title="备件详情"
        open={detailId != null}
        onClose={() => setDetailId(null)}
        width={480}
      >
        {detail && (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="ID">{detail.id}</Descriptions.Item>
            <Descriptions.Item label="配件编号">{detail.partNo}</Descriptions.Item>
            <Descriptions.Item label="名称">{detail.name}</Descriptions.Item>
            <Descriptions.Item label="规格">{detail.spec ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="单位">{detail.unit ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="参考价">{detail.referencePrice ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">{detail.status}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{detail.createdAt ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="更新时间">{detail.updatedAt ?? '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
}
