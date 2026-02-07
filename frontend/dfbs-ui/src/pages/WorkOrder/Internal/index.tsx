import { useRef, useState, useEffect } from 'react';
import { ProTable, ModalForm, PageContainer } from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Tabs, Button, Modal, Form, Input, InputNumber, DatePicker, Select, message } from 'antd';
import type { Dayjs } from 'dayjs';
import { useNavigate } from 'react-router-dom';
import request from '@/shared/utils/request';
import SmartReferenceSelect from '@/shared/components/SmartReferenceSelect';

function CustomerSmartSelectInternal() {
  const form = Form.useFormInstance();
  const customerName = Form.useWatch('customerName', form);
  return (
    <SmartReferenceSelect
      entityType="CUSTOMER"
      value={customerName ?? ''}
      placeholder="输入客户名称或从下拉选择"
      onChange={(ref) => {
        form.setFieldValue('customerId', ref.id ?? null);
        form.setFieldValue('customerName', ref.name);
      }}
    />
  );
}

interface WorkOrderRow {
  id: number;
  orderNo: string;
  type: string;
  status: string;
  customerId?: number | null;
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
  ACCEPTED_BY_DISPATCHER: '已受理/待派单',
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

/** Build a row-like object from API entity for dispatch modal */
function toWorkOrderRow(d: { id: number; orderNo?: string; status?: string; customerName?: string; contactPerson?: string; contactPhone?: string; serviceAddress?: string }): WorkOrderRow {
  return {
    id: d.id,
    orderNo: d.orderNo ?? '',
    type: '',
    status: d.status ?? 'ACCEPTED_BY_DISPATCHER',
    customerName: d.customerName ?? '',
    contactPerson: d.contactPerson ?? '',
    contactPhone: d.contactPhone ?? '',
    serviceAddress: d.serviceAddress,
  };
}

export default function WorkOrderInternal() {
  const navigate = useNavigate();
  const pendingPoolRef = useRef<ActionType>(null);
  const readyPoolRef = useRef<ActionType>(null);
  const myOrdersRef = useRef<ActionType>(null);
  const allRef = useRef<ActionType>(null);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [dispatchModalOpen, setDispatchModalOpen] = useState(false);
  const [dispatchRow, setDispatchRow] = useState<WorkOrderRow | null>(null);
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [rejectRow, setRejectRow] = useState<WorkOrderRow | null>(null);
  const [acceptModalOpen, setAcceptModalOpen] = useState(false);
  const [acceptRow, setAcceptRow] = useState<WorkOrderRow | null>(null);
  const [modelOptions, setModelOptions] = useState<{ label: string; value: number }[]>([]);
  const [dispatchForm] = Form.useForm<{ serviceManagerId: number }>();
  const [rejectForm] = Form.useForm<{ reason: string }>();
  const [acceptForm] = Form.useForm<{
    customerId?: number | null;
    customerName?: string;
    contactPerson: string;
    contactPhone: string;
    serviceAddress: string;
    issueDescription?: string;
    appointmentTime?: Dayjs;
  }>();

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
      width: 120,
      render: (_, row) => {
        const color = row.status === 'ACCEPTED_BY_DISPATCHER' ? 'blue' : row.status === 'COMPLETED' ? 'green' : row.status === 'CANCELLED' ? 'default' : undefined;
        return color ? <span style={{ color: color === 'blue' ? '#1890ff' : color === 'green' ? '#52c41a' : undefined }}>{STATUS_MAP[row.status] ?? row.status}</span> : (STATUS_MAP[row.status] ?? row.status);
      },
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

  const pendingPoolColumns: ProColumns<WorkOrderRow>[] = [
    ...baseColumns,
    {
      title: '操作',
      valueType: 'option',
      width: 200,
      render: (_, row) => [
        <a key="detail" onClick={() => navigate(`/work-orders/${row.id}`)}>详情</a>,
        <a
          key="accept"
          onClick={() => {
            setAcceptRow(row);
            acceptForm.setFieldsValue({
              customerId: row.customerId ?? undefined,
              customerName: row.customerName ?? '',
              contactPerson: row.contactPerson ?? '',
              contactPhone: row.contactPhone ?? '',
              serviceAddress: row.serviceAddress ?? '',
              issueDescription: undefined,
              appointmentTime: undefined,
            });
            setAcceptModalOpen(true);
          }}
        >
          受理
        </a>,
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
        </a>,
      ],
    },
  ];

  const readyPoolColumns: ProColumns<WorkOrderRow>[] = [
    ...baseColumns,
    {
      title: '操作',
      valueType: 'option',
      width: 140,
      render: (_, row) => [
        <a key="detail" onClick={() => navigate(`/work-orders/${row.id}`)}>详情</a>,
        <a
          key="dispatch"
          onClick={() => {
            setDispatchRow(row);
            dispatchForm.setFieldsValue({ serviceManagerId: undefined });
            setDispatchModalOpen(true);
          }}
        >
          派单
        </a>,
      ],
    },
  ];

  const allPoolColumns: ProColumns<WorkOrderRow>[] = [
    ...baseColumns,
    {
      title: '操作',
      valueType: 'option',
      width: 220,
      render: (_, row) => [
        <a key="detail" onClick={() => navigate(`/work-orders/${row.id}`)}>详情</a>,
        row.status === 'PENDING' && (
          <a
            key="accept"
            onClick={() => {
              setAcceptRow(row);
              acceptForm.setFieldsValue({
                customerId: row.customerId ?? undefined,
                customerName: row.customerName ?? '',
                contactPerson: row.contactPerson ?? '',
                contactPhone: row.contactPhone ?? '',
                serviceAddress: row.serviceAddress ?? '',
                issueDescription: undefined,
                appointmentTime: undefined,
              });
              setAcceptModalOpen(true);
            }}
          >
            受理
          </a>
        ),
        row.status === 'ACCEPTED_BY_DISPATCHER' && (
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
      pendingPoolRef.current?.reload();
      readyPoolRef.current?.reload();
      myOrdersRef.current?.reload();
      allRef.current?.reload();
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
      pendingPoolRef.current?.reload();
      readyPoolRef.current?.reload();
      myOrdersRef.current?.reload();
      allRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  };

  const handleAcceptByDispatcher = async () => {
    const values = await acceptForm.validateFields();
    if (!acceptRow) return;
    try {
      const { data } = await request.post<WorkOrderRow>('/v1/work-orders/accept-by-dispatcher', {
        id: acceptRow.id,
        customerId: values.customerId ?? undefined,
        customerName: values.customerName ?? undefined,
        contactPerson: values.contactPerson ?? undefined,
        contactPhone: values.contactPhone ?? undefined,
        serviceAddress: values.serviceAddress ?? undefined,
        issueDescription: values.issueDescription || undefined,
        appointmentTime: values.appointmentTime ? values.appointmentTime.toISOString() : undefined,
      });
      message.success('受理成功');
      setAcceptModalOpen(false);
      setAcceptRow(null);
      acceptForm.resetFields();
      pendingPoolRef.current?.reload();
      readyPoolRef.current?.reload();
      myOrdersRef.current?.reload();
      setDispatchRow(toWorkOrderRow(data));
      dispatchForm.setFieldsValue({ serviceManagerId: undefined });
      setDispatchModalOpen(true);
      allRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  };


  type CreateFormValues = {
    customerId?: number | null;
    customerName?: string;
    contactPerson: string;
    contactPhone: string;
    serviceAddress: string;
    deviceModelId?: number;
    issueDescription?: string;
    appointmentTime?: Dayjs;
  };

  const createFormContent = (
    <>
      <Form.Item name="customerId" hidden>
        <Input type="hidden" />
      </Form.Item>
      <Form.Item name="customerName" hidden rules={[{ required: true, message: '请输入或选择客户' }]}>
        <Input type="hidden" />
      </Form.Item>
      <Form.Item label="客户" required>
        <CustomerSmartSelectInternal />
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
    </>
  );

  return (
    <PageContainer
      title="工单管理"
      extra={[
        <Button key="create" type="primary" onClick={() => setCreateModalOpen(true)}>
          新建工单
        </Button>,
      ]}
    >
      <div style={{ padding: 24 }}>
        <Tabs
        defaultActiveKey="pending"
        items={[
          {
            key: 'pending',
            label: '待受理',
            children: (
              <ProTable<WorkOrderRow>
                actionRef={pendingPoolRef}
                columns={pendingPoolColumns}
                request={async () => {
                  try {
                    const { data } = await request.get<WorkOrderRow[]>('/v1/work-orders/pool');
                    const list = Array.isArray(data) ? data : [];
                    const filtered = list.filter((r) => r.status === 'PENDING');
                    return { data: filtered, total: filtered.length, success: true };
                  } catch {
                    return { data: [], total: 0, success: true };
                  }
                }}
                rowKey="id"
                search={false}
                pagination={{ pageSize: 10 }}
                headerTitle="待受理"
              />
            ),
          },
          {
            key: 'ready',
            label: '待派单',
            children: (
              <ProTable<WorkOrderRow>
                actionRef={readyPoolRef}
                columns={readyPoolColumns}
                request={async () => {
                  try {
                    const { data } = await request.get<WorkOrderRow[]>('/v1/work-orders/pool');
                    const list = Array.isArray(data) ? data : [];
                    const filtered = list.filter((r) => r.status === 'ACCEPTED_BY_DISPATCHER');
                    return { data: filtered, total: filtered.length, success: true };
                  } catch {
                    return { data: [], total: 0, success: true };
                  }
                }}
                rowKey="id"
                search={false}
                pagination={{ pageSize: 10 }}
                headerTitle="待派单"
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
                columns={allPoolColumns}
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
                headerTitle="全部工单"
              />
            ),
          },
        ]}
        />
      </div>

      <ModalForm<CreateFormValues>
        title="新建工单"
        open={createModalOpen}
        onOpenChange={setCreateModalOpen}
        onFinish={async (values) => {
          try {
            const { data } = await request.post<WorkOrderRow>('/v1/work-orders/create', {
              customerId: values.customerId ?? undefined,
              customerName: values.customerName ?? undefined,
              contactPerson: values.contactPerson,
              contactPhone: values.contactPhone,
              serviceAddress: values.serviceAddress,
              deviceModelId: values.deviceModelId ?? undefined,
              issueDescription: values.issueDescription ?? undefined,
              appointmentTime: values.appointmentTime ? values.appointmentTime.toISOString() : undefined,
            });
            message.success('工单已创建');
            setCreateModalOpen(false);
            pendingPoolRef.current?.reload();
            readyPoolRef.current?.reload();
            myOrdersRef.current?.reload();
            allRef.current?.reload();
            setDispatchRow(toWorkOrderRow(data));
            dispatchForm.setFieldsValue({ serviceManagerId: undefined });
            setDispatchModalOpen(true);
            return true;
          } catch (e) {
            showError(e);
            return false;
          }
        }}
        layout="vertical"
      >
        {createFormContent}
      </ModalForm>

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

      <Modal
        title="受理"
        open={acceptModalOpen}
        onOk={handleAcceptByDispatcher}
        onCancel={() => { setAcceptModalOpen(false); setAcceptRow(null); acceptForm.resetFields(); }}
        okText="确定受理"
        cancelText="取消"
        width={520}
      >
        <Form form={acceptForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="customerId" hidden>
            <Input type="hidden" />
          </Form.Item>
          <Form.Item name="customerName" hidden rules={[{ required: true, message: '请输入或选择客户' }]}>
            <Input type="hidden" />
          </Form.Item>
          <Form.Item label="客户" required>
            <CustomerSmartSelectInternal />
          </Form.Item>
          <Form.Item name="contactPerson" label="联系人">
            <Input placeholder="联系人" />
          </Form.Item>
          <Form.Item name="contactPhone" label="联系电话">
            <Input placeholder="联系电话" />
          </Form.Item>
          <Form.Item name="serviceAddress" label="服务地址">
            <Input.TextArea rows={2} placeholder="服务地址" />
          </Form.Item>
          <Form.Item name="issueDescription" label="问题描述">
            <Input.TextArea rows={3} placeholder="问题描述（选填）" />
          </Form.Item>
          <Form.Item name="appointmentTime" label="预约时间">
            <DatePicker showTime style={{ width: '100%' }} placeholder="选填" />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
}
