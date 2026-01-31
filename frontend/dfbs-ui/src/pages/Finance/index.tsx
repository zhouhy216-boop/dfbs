import { useRef, useState } from 'react';
import { ProTable } from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Drawer, Descriptions, InputNumber, Button, Form, message } from 'antd';
import request from '@/utils/request';
import dayjs from 'dayjs';
import { toProTableResult, type SpringPage } from '@/utils/adapters';

interface PaymentItem {
  id: number;
  quoteId?: number;
  amount: number;
  currency: string;
  status?: string;
  paidAt?: string;
  createdAt?: string;
  statementId?: number;
  isFinanceConfirmed?: boolean;
}

export default function Finance() {
  const [quoteIdFilter, setQuoteIdFilter] = useState<number | undefined>(undefined);
  const [detailId, setDetailId] = useState<number | null>(null);
  const [detail, setDetail] = useState<PaymentItem | null>(null);
  const tableRef = useRef<ActionType>(null);

  const columns: ProColumns<PaymentItem>[] = [
    { title: 'ID', dataIndex: 'id', width: 80, search: false },
    { title: '报价单ID', dataIndex: 'quoteId', width: 100, search: false },
    { title: '金额', dataIndex: 'amount', valueType: 'money', width: 120, search: false },
    { title: '币种', dataIndex: 'currency', width: 80, search: false },
    { title: '状态', dataIndex: 'status', width: 100, search: false },
    { title: '回款时间', dataIndex: 'paidAt', valueType: 'dateTime', width: 180, search: false },
    { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime', width: 180, search: false },
    {
      title: '操作',
      valueType: 'option',
      width: 80,
      render: (_, row) => [
        <a key="detail" onClick={() => loadDetail(row.id)}>详情</a>,
      ],
    },
  ];

  async function loadDetail(id: number) {
    setDetailId(id);
    try {
      const { data } = await request.get<PaymentItem>(`/v1/payments/${id}`);
      setDetail(data);
    } catch {
      setDetail(null);
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <Form layout="inline" style={{ marginBottom: 16 }}>
        <Form.Item label="报价单ID（可选）">
          <InputNumber
            min={1}
            placeholder="按报价单筛选"
            value={quoteIdFilter}
            onChange={(v) => setQuoteIdFilter(v ?? undefined)}
            style={{ width: 160 }}
          />
        </Form.Item>
        <Form.Item>
          <Button type="primary" onClick={() => tableRef.current?.reload()}>查询</Button>
        </Form.Item>
      </Form>
      <ProTable<PaymentItem>
        actionRef={tableRef}
        columns={columns}
        request={async (params) => {
          try {
            const page = (params.current ?? 1) - 1;
            const size = params.pageSize ?? 10;
            const { data } = await request.get<SpringPage<PaymentItem>>('/v1/payments', {
              params: { quoteId: quoteIdFilter ?? undefined, page, size, sort: 'createdAt,desc' },
            });
            return toProTableResult(data);
          } catch {
            return { data: [], total: 0, success: true };
          }
        }}
        rowKey="id"
        search={false}
        pagination={{ pageSize: 10 }}
        params={{ quoteId: quoteIdFilter }}
        headerTitle="回款列表"
        toolBarRender={() => [
          <Button key="tip" onClick={() => message.info('可输入报价单ID筛选该单的回款')}>说明</Button>,
        ]}
      />
      <Drawer
        title="回款详情"
        open={detailId !== null}
        onClose={() => { setDetailId(null); setDetail(null); }}
        width={480}
      >
        {detail && (
          <Descriptions column={1} bordered>
            <Descriptions.Item label="ID">{detail.id}</Descriptions.Item>
            <Descriptions.Item label="报价单ID">{detail.quoteId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="对账单ID">{detail.statementId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="金额">{detail.amount}</Descriptions.Item>
            <Descriptions.Item label="币种">{detail.currency}</Descriptions.Item>
            <Descriptions.Item label="状态">{detail.status ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="回款时间">{detail.paidAt ? dayjs(detail.paidAt).format('YYYY-MM-DD HH:mm') : '-'}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{detail.createdAt ? dayjs(detail.createdAt).format('YYYY-MM-DD HH:mm') : '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
}
