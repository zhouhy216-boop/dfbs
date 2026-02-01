import { useRef, useState, useEffect } from 'react';
import {
  ProTable,
  ModalForm,
  ProFormSelect,
  ProFormText,
  ProFormDigit,
  ProFormTextArea,
} from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Tabs, Button, Modal, Form, Input, Radio, message } from 'antd';
import request from '@/utils/request';

/** 补货申请行（与 WhReplenishRequestEntity 对应） */
interface ReplenishRequestRow {
  id: number;
  requestNo: string;
  targetWarehouseId: number;
  applicantId: number;
  partNo: string;
  quantity: number;
  reason?: string;
  status: string;
  l1ApproverId?: number;
  l1Comment?: string;
  l1Time?: string;
  l2ApproverId?: number;
  l2Comment?: string;
  l2Time?: string;
  createdAt?: string;
}

interface WhWarehouseOption {
  id: number;
  name: string;
  type: string;
}

const STATUS_MAP: Record<string, string> = {
  DRAFT: '草稿',
  PENDING_L1: '待负责人审批',
  PENDING_L2: '待领导审批',
  APPROVED: '通过/待落账',
  REJECTED: '驳回',
  COMPLETED: '已落账',
};

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  const msg = err.response?.data?.message ?? err.message ?? '操作失败';
  message.error(msg);
}

export default function WarehouseReplenish() {
  const myRequestsRef = useRef<ActionType>(null);
  const pendingRef = useRef<ActionType>(null);
  const [warehouses, setWarehouses] = useState<WhWarehouseOption[]>([]);
  const [approveModalOpen, setApproveModalOpen] = useState(false);
  const [approveRow, setApproveRow] = useState<ReplenishRequestRow | null>(null);
  const [approveForm] = Form.useForm<{ comment: string; approved: boolean }>();

  useEffect(() => {
    request.get<WhWarehouseOption[]>('/v1/warehouse/warehouses').then(({ data }) => {
      setWarehouses(data ?? []);
    }).catch(() => setWarehouses([]));
  }, []);

  const satelliteWarehouses = warehouses.filter((w) => w.type === 'SATELLITE');

  const baseColumns: ProColumns<ReplenishRequestRow>[] = [
    { title: '申请单号', dataIndex: 'requestNo', width: 160, ellipsis: true },
    { title: '目标仓库ID', dataIndex: 'targetWarehouseId', width: 100, search: false },
    { title: '配件编号', dataIndex: 'partNo', width: 120 },
    { title: '数量', dataIndex: 'quantity', width: 80, search: false },
    { title: '原因', dataIndex: 'reason', ellipsis: true, search: false },
    {
      title: '状态',
      dataIndex: 'status',
      width: 120,
      search: false,
      render: (_, row) => STATUS_MAP[row.status] ?? row.status,
    },
    { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime', width: 180, search: false },
  ];

  const myRequestColumns: ProColumns<ReplenishRequestRow>[] = [
    ...baseColumns,
    {
      title: '操作',
      valueType: 'option',
      width: 80,
      search: false,
      render: () => [],
    },
  ];

  const pendingColumns: ProColumns<ReplenishRequestRow>[] = [
    ...baseColumns,
    {
      title: '操作',
      valueType: 'option',
      width: 100,
      search: false,
      render: (_, row) =>
        (row.status === 'PENDING_L1' || row.status === 'PENDING_L2')
          ? [
              <a
                key="approve"
                onClick={() => {
                  setApproveRow(row);
                  approveForm.setFieldsValue({ comment: '', approved: true });
                  setApproveModalOpen(true);
                }}
              >
                审批
              </a>,
            ]
          : [],
    },
  ];

  const handleApproveSubmit = async () => {
    if (!approveRow) return;
    const values = await approveForm.validateFields();
    const isL1 = approveRow.status === 'PENDING_L1';
    const url = isL1 ? '/v1/warehouse/replenish/approve-l1' : '/v1/warehouse/replenish/approve-l2';
    try {
      await request.post(url, {
        requestId: approveRow.id,
        approved: values.approved,
        comment: values.comment,
      });
      message.success(values.approved ? '已通过' : '已驳回');
      setApproveModalOpen(false);
      setApproveRow(null);
      approveForm.resetFields();
      pendingRef.current?.reload();
      myRequestsRef.current?.reload();
    } catch (err) {
      showError(err);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <Tabs
        items={[
          {
            key: 'my',
            label: '我的申请',
            children: (
              <ProTable<ReplenishRequestRow>
                actionRef={myRequestsRef}
                columns={myRequestColumns}
                request={async () => {
                  try {
                    const { data } = await request.get<ReplenishRequestRow[]>('/v1/warehouse/replenish/my-requests');
                    const list = Array.isArray(data) ? data : [];
                    return { data: list, total: list.length, success: true };
                  } catch {
                    return { data: [], total: 0, success: true };
                  }
                }}
                rowKey="id"
                search={false}
                pagination={{ pageSize: 10 }}
                headerTitle="我的补货申请"
                toolBarRender={() => [
                  <ModalForm<{ targetWarehouseId: number; partNo: string; quantity: number; reason?: string }>
                    key="create"
                    title="发起补货"
                    trigger={<Button type="primary">发起补货</Button>}
                    onFinish={async (values) => {
                      try {
                        await request.post('/v1/warehouse/replenish/create', {
                          targetWarehouseId: values.targetWarehouseId,
                          partNo: values.partNo,
                          quantity: values.quantity,
                          reason: values.reason,
                        });
                        message.success('申请已提交');
                        myRequestsRef.current?.reload();
                        return true;
                      } catch (err) {
                        showError(err);
                        return false;
                      }
                    }}
                  >
                    <ProFormSelect
                      name="targetWarehouseId"
                      label="目标仓库"
                      rules={[{ required: true, message: '请选择目标仓库（小库）' }]}
                      options={satelliteWarehouses.map((w) => ({ label: w.name, value: w.id }))}
                      placeholder="请选择服务站小库"
                    />
                    <ProFormText name="partNo" label="配件编号" rules={[{ required: true }]} placeholder="配件编号" />
                    <ProFormDigit name="quantity" label="数量" min={1} rules={[{ required: true }]} />
                    <ProFormTextArea name="reason" label="补货原因" placeholder="选填" />
                  </ModalForm>,
                ]}
              />
            ),
          },
          {
            key: 'pending',
            label: '待我审批',
            children: (
              <ProTable<ReplenishRequestRow>
                actionRef={pendingRef}
                columns={pendingColumns}
                request={async () => {
                  try {
                    const { data } = await request.get<ReplenishRequestRow[]>('/v1/warehouse/replenish/my-pending');
                    const list = Array.isArray(data) ? data : [];
                    return { data: list, total: list.length, success: true };
                  } catch {
                    return { data: [], total: 0, success: true };
                  }
                }}
                rowKey="id"
                search={false}
                pagination={{ pageSize: 10 }}
                headerTitle="待审批补货申请"
              />
            ),
          },
        ]}
      />

      <Modal
        title="审批"
        open={approveModalOpen}
        onOk={handleApproveSubmit}
        onCancel={() => {
          setApproveModalOpen(false);
          setApproveRow(null);
          approveForm.resetFields();
        }}
        okText="提交"
        cancelText="取消"
      >
        <Form form={approveForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="approved" label="审批结果" rules={[{ required: true }]} initialValue={true}>
            <Radio.Group>
              <Radio value={true}>通过</Radio>
              <Radio value={false}>驳回</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item name="comment" label="审批意见">
            <Input.TextArea rows={3} placeholder="选填" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
