import { useRef, useState, useEffect } from 'react';
import { ProTable, ModalForm } from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Tabs, Button, Modal, Form, Input, InputNumber, DatePicker, Select, message } from 'antd';
import type { Dayjs } from 'dayjs';
import { useNavigate } from 'react-router-dom';
import request from '@/utils/request';

interface WorkOrderRow {
  id: number;
  orderNo: string;
  type: string;
  status: string;
  customerName: string;
  contactPerson: string;
  contactPhone: string;
  serviceAddress?: string;
  serviceManagerId?: number;
  createdAt?: string;
}

interface MachineModelOption {
  id: number;
  modelNo: string;
  modelName?: string;
}

interface SpringPage<T> {
  content?: T[];
  totalElements?: number;
}

const STATUS_MAP: Record<string, string> = {
  PENDING: '待受理',
  DISPATCHED: '已派单',
  ACCEPTED: '已接单',
  PROCESSING: '处理中',
  PENDING_SIGN: '待签字',
  COMPLETED: '已完修',
  CANCELLED: '已取消',
};

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  message.error(err.response?.data?.message ?? err.message ?? '操作失败');
}

export default function WorkOrderInternal() {
  const navigate = useNavigate();
  const poolRef = useRef<ActionType>(null);
  const myOrdersRef = useRef<ActionType>(null);
  const allRef = useRef<ActionType>(null);
  const [dispatchModalOpen, setDispatchModalOpen] = useState(false);
  const [dispatchRow, setDispatchRow] = useState<WorkOrderRow | null>(null);
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [rejectRow, setRejectRow] = useState<WorkOrderRow | null>(null);
  const [modelOptions, setModelOptions] = useState<{ label: string; value: number }[]>([]);
  const [dispatchForm] = Form.useForm<{ serviceManagerId: number }>();
  const [rejectForm] = Form.useForm<{ reason: string }>();

  useEffect(() => {
    request
      .get<SpringPage<MachineModelOption>>('/v1/masterdata/machine-models', {
        params: { page: 0, size: 200, sort: 'id,asc' },
      })
      .then(({ data }) => {
        const list = data?.content ?? [];
        setModelOptions(
          list.map((m) => ({
            label: `${m.modelNo}${m.modelName ? ' - ' + m.modelName : ''}`,
            value: m.id,
          }))
        );
      })
      .catch(() => setModelOptions([]));
  }, []);

  const baseColumns: ProColumns<WorkOrderRow>[] = [
    { title: '工单号', dataIndex: 'orderNo', width: 180, ellipsis: true },
    { title: '客户', dataIndex: 'customerName', width: 140, ellipsis: true },
    { title: '联系人', dataIndex: 'contactPerson', width: 100 },
    { title: '电话', dataIndex: 'contactPhone', width: 120 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (_, row) => STATUS_MAP[row.status] ?? row.status,
    },
    { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime', width: 180, search: false },
    {
      title: '操作',
      valueType: 'option',
      width: 120,
      render: (_, row) => [
        <a key="detail" onClick={() => navigate(`/work-orders/${row.id}`)}>详情</a>,
      ],
    },
  ];

  const poolColumns: ProColumns<WorkOrderRow>[] = [
    ...baseColumns,
    {
      title: '操作',
      valueType: 'option',
      width: 180,
      render: (_, row) => [
        <a key="detail" onClick={() => navigate(`/work-orders/${row.id}`)}>详情</a>,
        row.status === 'PENDING' && (
          <a
            key="dispatch"
            onClick={() => {
              setDispatchRow(row);
              dispatchForm.setFieldsValue({ serviceManagerId: undefined });
              setDispatchModalOpen(true);
            }}
          >
            派单
          </a>
        ),
        row.status === 'PENDING' && (
          <a
            key="reject"
            style={{ color: 'var(--ant-color-error)' }}
            onClick={() => {
              setRejectRow(row);
              rejectForm.setFieldsValue({ reason: '' });
              setRejectModalOpen(true);
            }}
          >
            驳回
          </a>
        ),
      ].filter(Boolean),
    },
  ];

  const handleDispatch = async () => {
    const values = await dispatchForm.validateFields();
    if (!dispatchRow) return;
    try {
      await request.post('/v1/work-orders/dispatch', {
        workOrderId: dispatchRow.id,
        serviceManagerId: values.serviceManagerId,
      });
      message.success('派单成功');
      setDispatchModalOpen(false);
      setDispatchRow(null);
      dispatchForm.resetFields();
      poolRef.current?.reload();
      myOrdersRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  };

  const handleReject = async () => {
    const values = await rejectForm.validateFields();
    if (!rejectRow) return;
    try {
      await request.post('/v1/work-orders/reject', {
        id: rejectRow.id,
        reason: values.reason ?? '',
      });
      message.success('已驳回');
      setRejectModalOpen(false);
      setRejectRow(null);
      rejectForm.resetFields();
      poolRef.current?.reload();
      myOrdersRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  };

  type CreateFormValues = {
    customerName: string;
    contactPerson: string;
    contactPhone: string;
    serviceAddress: string;
    deviceModelId?: number;
    issueDescription?: string;
    appointmentTime?: Dayjs;
  };

  return (
    <div style={{ padding: 24 }}>
      <Tabs
        items={[
          {
            key: 'pool',
            label: '待派单池',
            children: (
              <ProTable<WorkOrderRow>
                actionRef={poolRef}
                columns={poolColumns}
                request={async () => {
                  try {
                    const { data } = await request.get<WorkOrderRow[]>('/v1/work-orders/pool');
                    const list = Array.isArray(data) ? data : [];
                    return { data: list, total: list.length, success: true };
                  } catch {
                    return { data: [], total: 0, success: true };
                  }
                }}
                rowKey="id"
                search={false}
                pagination={{ pageSize: 10 }}
                headerTitle="待派单池"
                toolBarRender={() => [
                  <ModalForm<CreateFormValues>
                    key="create"
                    title="新建工单"
                    trigger={<Button type="primary">新建工单</Button>}
                    onFinish={async (values) => {
                      try {
                        await request.post('/v1/work-orders/create', {
                          customerName: values.customerName,
                          contactPerson: values.contactPerson,
                          contactPhone: values.contactPhone,
                          serviceAddress: values.serviceAddress,
                          deviceModelId: values.deviceModelId ?? undefined,
                          issueDescription: values.issueDescription ?? undefined,
                          appointmentTime: values.appointmentTime ? values.appointmentTime.toISOString() : undefined,
                        });
                        message.success('工单已创建');
                        poolRef.current?.reload();
                        myOrdersRef.current?.reload();
                        return true;
                      } catch (e) {
                        showError(e);
                        return false;
                      }
                    }}
                    layout="vertical"
                  >
                    <Form.Item name="customerName" label="客户名称" rules={[{ required: true, message: '请输入客户名称' }]}>
                      <Input placeholder="客户名称" />
                    </Form.Item>
                    <Form.Item name="contactPerson" label="联系人" rules={[{ required: true, message: '请输入联系人' }]}>
                      <Input placeholder="联系人" />
                    </Form.Item>
                    <Form.Item name="contactPhone" label="联系电话" rules={[{ required: true, message: '请输入联系电话' }]}>
                      <Input placeholder="联系电话" />
                    </Form.Item>
                    <Form.Item name="serviceAddress" label="服务地址" rules={[{ required: true, message: '请输入服务地址' }]}>
                      <Input.TextArea rows={2} placeholder="服务地址" />
                    </Form.Item>
                    <Form.Item name="deviceModelId" label="设备型号">
                      <Select placeholder="请选择设备型号（选填）" allowClear options={modelOptions} showSearch optionFilterProp="label" />
                    </Form.Item>
                    <Form.Item name="issueDescription" label="故障描述">
                      <Input.TextArea rows={4} placeholder="请描述故障现象（选填）" />
                    </Form.Item>
                    <Form.Item name="appointmentTime" label="期望上门时间">
                      <DatePicker showTime style={{ width: '100%' }} placeholder="选填" />
                    </Form.Item>
                  </ModalForm>,
                ]}
              />
            ),
          },
          {
            key: 'my',
            label: '我的工单',
            children: (
              <ProTable<WorkOrderRow>
                actionRef={myOrdersRef}
                columns={baseColumns}
                request={async () => {
                  try {
                    const { data } = await request.get<WorkOrderRow[]>('/v1/work-orders/my-orders');
                    const list = Array.isArray(data) ? data : [];
                    return { data: list, total: list.length, success: true };
                  } catch {
                    return { data: [], total: 0, success: true };
                  }
                }}
                rowKey="id"
                search={false}
                pagination={{ pageSize: 10 }}
                headerTitle="我的工单"
              />
            ),
          },
          {
            key: 'all',
            label: '全部工单',
            children: (
              <ProTable<WorkOrderRow>
                actionRef={allRef}
                columns={baseColumns}
                request={async () => {
                  try {
                    const { data } = await request.get<WorkOrderRow[]>('/v1/work-orders/my-orders');
                    const list = Array.isArray(data) ? data : [];
                    return { data: list, total: list.length, success: true };
                  } catch {
                    return { data: [], total: 0, success: true };
                  }
                }}
                rowKey="id"
                search={false}
                pagination={{ pageSize: 10 }}
                headerTitle="全部工单（当前同我的工单）"
              />
            ),
          },
        ]}
      />

      <Modal
        title="派单"
        open={dispatchModalOpen}
        onOk={handleDispatch}
        onCancel={() => { setDispatchModalOpen(false); setDispatchRow(null); dispatchForm.resetFields(); }}
        okText="确定"
        cancelText="取消"
      >
        <Form form={dispatchForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="serviceManagerId" label="服务负责人（用户ID）" rules={[{ required: true, message: '请输入用户ID' }]}>
            <InputNumber min={1} placeholder="输入负责人用户ID" style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="驳回"
        open={rejectModalOpen}
        onOk={handleReject}
        onCancel={() => { setRejectModalOpen(false); setRejectRow(null); rejectForm.resetFields(); }}
        okText="确定驳回"
        cancelText="取消"
        okButtonProps={{ danger: true }}
      >
        <Form form={rejectForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="reason"
            label="驳回原因"
            rules={[{ required: true, message: '请输入驳回原因' }]}
          >
            <Input.TextArea rows={3} placeholder="请输入驳回原因" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
