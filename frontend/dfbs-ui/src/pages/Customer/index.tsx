import { useRef, useState } from 'react';
import { ProTable, ModalForm, ProFormText } from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Drawer, Descriptions, Button, message } from 'antd';
import request from '@/utils/request';
import dayjs from 'dayjs';
import { toProTableResult, type SpringPage } from '@/utils/adapters';

interface CustomerItem {
  id: number;
  customerCode: string;
  name: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export default function Customer() {
  const actionRef = useRef<ActionType>(null);
  const [detailId, setDetailId] = useState<number | null>(null);
  const [detail, setDetail] = useState<CustomerItem | null>(null);

  const columns: ProColumns<CustomerItem>[] = [
    { title: 'ID', dataIndex: 'id', width: 80, search: false },
    { title: '客户编码', dataIndex: 'customerCode', width: 140, search: false },
    { title: '名称', dataIndex: 'name', ellipsis: true },
    { title: '状态', dataIndex: 'status', width: 100, search: false },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      valueType: 'dateTime',
      width: 180,
      sorter: true,
      defaultSortOrder: 'descend',
      search: false,
    },
    {
      title: '操作',
      valueType: 'option',
      width: 100,
      render: (_, row) => [
        <a key="detail" onClick={() => { setDetailId(row.id); fetchDetail(row.id); }}>详情</a>,
      ],
    },
  ];

  async function fetchDetail(id: number) {
    try {
      const { data } = await request.get<CustomerItem>(`/masterdata/customers/${id}`);
      setDetail(data);
    } catch {
      setDetail(null);
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <ProTable<CustomerItem>
        actionRef={actionRef}
        columns={columns}
        request={async (params, sort) => {
          const page = (params.current ?? 1) - 1;
          const size = params.pageSize ?? 10;
          const createdSort = sort?.createdAt === 'ascend' ? 'asc' : 'desc';
          const { data } = await request.get<SpringPage<CustomerItem>>('/v1/customers', {
            params: {
              keyword: params.name ?? undefined,
              page,
              size,
              sort: `createdAt,${createdSort}`,
            },
          });
          return toProTableResult(data);
        }}
        rowKey="id"
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
        options={{ reload: true }}
        headerTitle="客户列表"
        toolBarRender={() => [
          <ModalForm<{ name: string; customerNo?: string }>
            key="create"
            title="新建客户"
            trigger={<Button type="primary">New Customer</Button>}
            onFinish={async (values) => {
              try {
                const customerNo = values.customerNo?.trim() || `CUST-${Date.now()}`;
                await request.post('/masterdata/customers', {
                  customerNo,
                  name: values.name?.trim(),
                });
                message.success('创建成功');
                actionRef.current?.reload();
                return true;
              } catch (e: unknown) {
                const err = e as { response?: { status: number } };
                if (err.response?.status === 409) {
                  message.error('Client name exists');
                  return false;
                }
                if (err.response?.status === 400) {
                  message.error('Client name exists or invalid input');
                  return false;
                }
                throw e;
              }
            }}
          >
            <ProFormText
              name="name"
              label="Company Name"
              rules={[{ required: true, message: '请输入公司名称' }]}
              placeholder="公司名称"
            />
            <ProFormText name="customerNo" label="客户编码（可选）" placeholder="留空则自动生成" />
          </ModalForm>,
        ]}
      />
      <Drawer
        title="客户详情"
        open={detailId !== null}
        onClose={() => { setDetailId(null); setDetail(null); }}
        width={480}
      >
        {detail && (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="ID">{detail.id}</Descriptions.Item>
            <Descriptions.Item label="客户编码">{detail.customerCode}</Descriptions.Item>
            <Descriptions.Item label="名称">{detail.name}</Descriptions.Item>
            <Descriptions.Item label="状态">{detail.status}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{detail.createdAt ? dayjs(detail.createdAt).format('YYYY-MM-DD HH:mm') : '-'}</Descriptions.Item>
            <Descriptions.Item label="更新时间">{detail.updatedAt ? dayjs(detail.updatedAt).format('YYYY-MM-DD HH:mm') : '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
}
