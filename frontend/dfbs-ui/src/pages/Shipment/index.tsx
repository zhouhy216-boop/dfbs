import { useRef, useState } from 'react';
import { ProTable, ModalForm, ProFormSelect, ProFormText } from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Drawer, Descriptions, Button, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import request from '@/shared/utils/request';
import dayjs from 'dayjs';
import { AttachmentList } from '@/shared/components/AttachmentList';
import { toProTableResult, type SpringPage } from '@/shared/utils/adapters';

interface ShipmentMachineItem {
  id: number;
  shipmentId: number;
  machineNo: string;
  model?: string;
}

interface ShipmentListRow {
  id: number;
  shipmentNo: string;
  customerName: string;
  status: string;
  createdAt?: string;
}

interface ShipmentDetail {
  id: number;
  quoteId?: number;
  type?: string;
  status: string;
  contractNo?: string;
  shipDate?: string;
  deliveryAddress?: string;
  receiverName?: string;
  receiverPhone?: string;
  carrierId?: number;
  carrier?: string;
  createdAt?: string;
}

interface CustomerItem {
  id: number;
  customerCode: string;
  name: string;
  status: string;
}

export default function Shipment() {
  const navigate = useNavigate();
  const actionRef = useRef<ActionType>(null);
  const [detailId, setDetailId] = useState<number | null>(null);
  const [detail, setDetail] = useState<ShipmentDetail | null>(null);
  const [applyAfterSalesOpen, setApplyAfterSalesOpen] = useState(false);

  const columns: ProColumns<ShipmentListRow>[] = [
    { title: '发货单号', dataIndex: 'shipmentNo', width: 160 },
    { title: '客户', dataIndex: 'customerName', ellipsis: true },
    {
      title: '状态',
      dataIndex: 'status',
      width: 120,
      valueEnum: {
        PENDING: { text: '待发货', status: 'Warning' },
        CREATED: { text: '新建', status: 'Default' },
        PENDING_SHIP: { text: '待发货', status: 'Warning' },
        PARTIAL_SHIPPED: { text: '部分发货', status: 'Processing' },
        SHIPPED: { text: '已发货', status: 'Success' },
        DELIVERED: { text: '已送达', status: 'Success' },
        COMPLETED: { text: '已完成', status: 'Success' },
        EXCEPTION: { text: '异常', status: 'Error' },
        CANCELLED: { text: '已取消', status: 'Error' },
      },
    },
    { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime', width: 180, search: false },
    {
      title: '操作',
      valueType: 'option',
      width: 200,
      render: (_, row) => [
        <a key="detail" onClick={() => loadDetail(row.id)}>详情</a>,
        <a key="ticket" onClick={() => exportTicket(row.id)}>导出运单</a>,
        <a key="receipt" onClick={() => exportReceipt(row.id)}>导出回单</a>,
      ],
    },
  ];

  async function loadDetail(id: number) {
    setDetailId(id);
    try {
      const { data } = await request.get<ShipmentDetail>(`/v1/shipments/${id}`);
      setDetail(data);
    } catch {
      setDetail(null);
    }
  }

  async function exportTicket(id: number) {
    try {
      const res = await request.get(`/v1/shipments/${id}/export-ticket`, { responseType: 'blob' });
      downloadBlob(res.data, `shipment-${id}-ticket.html`);
      message.success('导出成功');
    } catch {
      message.error('导出失败');
    }
  }

  async function exportReceipt(id: number) {
    try {
      const res = await request.get(`/v1/shipments/${id}/export-receipt`, { responseType: 'blob' });
      downloadBlob(res.data, `shipment-${id}-receipt.html`);
      message.success('导出成功');
    } catch {
      message.error('导出失败');
    }
  }

  function downloadBlob(blob: Blob, filename: string) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }

  return (
    <div style={{ padding: 24 }}>
      <ProTable<ShipmentListRow>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          try {
            const page = (params.current ?? 1) - 1;
            const size = params.pageSize ?? 10;
            const { data } = await request.get<SpringPage<ShipmentListRow>>('/v1/shipments', {
              params: { status: params.status ?? undefined, page, size },
            });
            return toProTableResult(data);
          } catch {
            return { data: [], total: 0, success: true };
          }
        }}
        rowKey="id"
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
        headerTitle="发货列表"
        toolBarRender={() => [
          <ModalForm<{ customerId: number; shipmentType: string }>
            key="create"
            title="新建发货单"
            trigger={<Button type="primary">New Shipment</Button>}
            onFinish={async (values) => {
              await request.post('/v1/shipments', {
                customerId: values.customerId,
                shipmentType: values.shipmentType ?? 'STANDARD',
              }, { params: { operatorId: 1 } });
              message.success('创建成功');
              actionRef.current?.reload();
              return true;
            }}
          >
            <ProFormSelect
              name="customerId"
              label="客户"
              rules={[{ required: true, message: '请选择客户' }]}
              request={async () => {
                const { data } = await request.get<SpringPage<CustomerItem>>('/v1/customers', {
                  params: { page: 0, size: 100, sort: 'createdAt,desc' },
                });
                const list = data?.content ?? [];
                return list.map((c) => ({ label: `${c.name} (${c.customerCode})`, value: c.id }));
              }}
              placeholder="请选择客户"
            />
            <ProFormSelect
              name="shipmentType"
              label="类型"
              options={[
                { label: '标准', value: 'STANDARD' },
                { label: '加急', value: 'EXPRESS' },
              ]}
              initialValue="STANDARD"
            />
          </ModalForm>,
        ]}
      />
      <Drawer
        title="发货详情"
        open={detailId !== null}
        onClose={() => { setDetailId(null); setDetail(null); }}
        width={520}
      >
        {detail && (
          <>
            <div style={{ marginBottom: 16 }}>
              <Button type="primary" size="small" onClick={() => setApplyAfterSalesOpen(true)}>
                发起售后 (Apply After-Sales)
              </Button>
            </div>
            <Descriptions column={1} bordered size="small">
              <Descriptions.Item label="ID">{detail.id}</Descriptions.Item>
              <Descriptions.Item label="报价单ID">{detail.quoteId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="类型">{detail.type ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="状态">{detail.status}</Descriptions.Item>
              <Descriptions.Item label="承运商">{detail.carrier ?? detail.carrierId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="发货日期">{detail.shipDate ? dayjs(detail.shipDate).format('YYYY-MM-DD') : '-'}</Descriptions.Item>
              <Descriptions.Item label="收货人">{detail.receiverName ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="电话">{detail.receiverPhone ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="地址">{detail.deliveryAddress ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{detail.createdAt ? dayjs(detail.createdAt).format('YYYY-MM-DD HH:mm') : '-'}</Descriptions.Item>
            </Descriptions>
            <div style={{ marginTop: 16 }}>
              <strong>附件</strong>
              <AttachmentList fileUrls={undefined} />
            </div>
            {detailId != null && (
              <ModalForm<{ type: string; machineNo: string; reason?: string }>
                title="发起售后"
                open={applyAfterSalesOpen}
                onOpenChange={setApplyAfterSalesOpen}
                onFinish={async (values) => {
                  await request.post('/v1/after-sales', {
                    sourceShipmentId: detailId,
                    type: values.type ?? 'REPAIR',
                    machineNo: values.machineNo,
                    reason: values.reason ?? null,
                  });
                  message.success('售后单已创建');
                  setApplyAfterSalesOpen(false);
                  navigate('/after-sales');
                  return true;
                }}
              >
                <ProFormSelect
                  name="type"
                  label="类型"
                  options={[
                    { label: '换货', value: 'EXCHANGE' },
                    { label: '维修', value: 'REPAIR' },
                  ]}
                  initialValue="REPAIR"
                  rules={[{ required: true }]}
                />
                <ProFormText
                  name="machineNo"
                  label="机器/序列号 (Machine / Serial No)"
                  placeholder="Please manually enter the SN being returned"
                  rules={[{ required: true, message: '请输入机器/序列号' }]}
                />
                <ProFormText name="reason" label="原因" placeholder="可选" />
              </ModalForm>
            )}
          </>
        )}
      </Drawer>
    </div>
  );
}
