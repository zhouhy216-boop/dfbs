import { useRef } from 'react';
import { ProTable } from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { message } from 'antd';
import request from '@/shared/utils/request';
import { toProTableResult, type SpringPage } from '@/shared/utils/adapters';

function showError(e: unknown) {
  const status = (e as { response?: { status?: number } })?.response?.status;
  if (status === 409) {
    message.error('Data already exists');
    return;
  }
  message.error((e as { message?: string })?.message ?? '操作失败');
}

interface ModelPartListRow {
  id: number;
  modelId: number;
  version: string;
  effectiveDate?: string;
  items: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

const STATUS_OPTIONS = [
  { label: 'DRAFT', value: 'DRAFT' },
  { label: 'PUBLISHED', value: 'PUBLISHED' },
  { label: 'DEPRECATED', value: 'DEPRECATED' },
];

export default function ModelPartList() {
  const actionRef = useRef<ActionType>(null);

  const columns: ProColumns<ModelPartListRow>[] = [
    { title: 'ID', dataIndex: 'id', width: 80, search: false },
    { title: '型号ID', dataIndex: 'modelId', width: 100 },
    { title: '版本', dataIndex: 'version', width: 120 },
    { title: '生效日期', dataIndex: 'effectiveDate', width: 120, search: false },
    { title: '明细(JSON)', dataIndex: 'items', ellipsis: true, search: false },
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
      width: 100,
      render: (_, row) =>
        row.status !== 'DEPRECATED'
          ? [
              <a
                key="disable"
                onClick={async () => {
                  try {
                    await request.post(`/v1/masterdata/model-part-lists/${row.id}/disable`, { updatedBy: 'user' });
                    message.success('已禁用');
                    actionRef.current?.reload();
                  } catch (err) {
                    showError(err);
                  }
                }}
              >
                禁用
              </a>,
            ]
          : [],
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <ProTable<ModelPartListRow>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          const page = (params.current ?? 1) - 1;
          const size = params.pageSize ?? 10;
          const { data } = await request.get<SpringPage<ModelPartListRow>>('/v1/masterdata/model-part-lists', {
            params: {
              keyword: params.version ?? undefined,
              status: params.status ?? undefined,
              modelId: params.modelId ?? undefined,
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
        headerTitle="型号BOM (Model Part List)"
      />
    </div>
  );
}
