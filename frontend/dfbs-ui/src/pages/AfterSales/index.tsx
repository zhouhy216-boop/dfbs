import { useRef, useState } from 'react';
import { ProTable } from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Button, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import request from '@/utils/request';
import { toProTableResult, type SpringPage } from '@/utils/adapters';

interface AfterSalesRow {
  id: number;
  type: string;
  status: string;
  sourceShipmentId: number;
  machineNo: string;
  reason?: string;
  attachments?: string;
  relatedNewShipmentId?: number;
  createdAt?: string;
}

const STATUS_VALUE_ENUM = {
  PENDING: { text: '待处理', status: 'Warning' as const },
  APPROVED: { text: '已批准', status: 'Processing' as const },
  REJECTED: { text: '已驳回', status: 'Error' as const },
  PROCESSING: { text: '处理中', status: 'Processing' as const },
  COMPLETED: { text: '已完成', status: 'Success' as const },
  DRAFT: { text: '草稿', status: 'Default' as const },
  SUBMITTED: { text: '已提交', status: 'Warning' as const },
  RECEIVED: { text: '已收货', status: 'Processing' as const },
  SENT_BACK: { text: '已寄回', status: 'Processing' as const },
};

export default function AfterSales() {
  const navigate = useNavigate();
  const actionRef = useRef<ActionType>(null);

  const columns: ProColumns<AfterSalesRow>[] = [
    { title: 'ID', dataIndex: 'id', width: 80, search: false },
    { title: '类型', dataIndex: 'type', width: 100, valueEnum: { EXCHANGE: { text: '换货' }, REPAIR: { text: '维修' } } },
    { title: '状态', dataIndex: 'status', width: 120, valueEnum: STATUS_VALUE_ENUM },
    { title: '发货单ID', dataIndex: 'sourceShipmentId', width: 100, search: false },
    { title: '机器编号', dataIndex: 'machineNo', width: 140 },
    { title: '原因', dataIndex: 'reason', ellipsis: true, search: false },
    { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime', width: 180, search: false },
    {
      title: '操作',
      valueType: 'option',
      width: 100,
      render: (_, row) => [
        <a key="detail" onClick={() => navigate(`/after-sales/${row.id}`)}>详情</a>,
      ],
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <ProTable<AfterSalesRow>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          try {
            const page = (params.current ?? 1) - 1;
            const size = params.pageSize ?? 10;
            const { data } = await request.get<SpringPage<AfterSalesRow>>('/v1/after-sales', {
              params: {
                machineNo: params.machineNo ?? undefined,
                status: params.status ?? undefined,
                page,
                size,
                sort: 'id,desc',
              },
            });
            return toProTableResult(data);
          } catch {
            return { data: [], total: 0, success: true };
          }
        }}
        rowKey="id"
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
        headerTitle="运输异常管理"
      />
    </div>
  );
}
