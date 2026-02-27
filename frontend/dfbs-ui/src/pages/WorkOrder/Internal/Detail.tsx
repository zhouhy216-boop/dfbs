import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Descriptions,
  Button,
  Tag,
  Timeline,
  Table,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  message,
  Space,
} from 'antd';
import { ArrowLeftOutlined, EnvironmentOutlined } from '@ant-design/icons';
import request from '@/shared/utils/request';
import { useAuthStore } from '@/shared/stores/useAuthStore';
import { useEffectivePermissions } from '@/shared/hooks/useEffectivePermissions';
import dayjs from 'dayjs';

interface WorkOrder {
  id: number;
  orderNo: string;
  type: string;
  status: string;
  customerName: string;
  contactPerson: string;
  contactPhone: string;
  serviceAddress: string;
  deviceModelId?: number;
  machineNo?: string;
  issueDescription?: string;
  appointmentTime?: string;
  dispatcherId?: number;
  serviceManagerId?: number;
  customerSignatureUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

interface WorkOrderRecord {
  id: number;
  workOrderId: number;
  description?: string;
  attachmentUrl?: string;
  createdAt?: string;
  createdBy?: string;
}

interface WorkOrderPart {
  id: number;
  workOrderId: number;
  partNo: string;
  partName?: string;
  quantity: number;
  usageStatus: string;
  warehouseId?: number;
  stockRecordId?: number;
  createdAt?: string;
}

interface WorkOrderDetailDto {
  workOrder: WorkOrder;
  records: WorkOrderRecord[];
  parts: WorkOrderPart[];
  /** Resolved from master when work order has customerId; use this for display. */
  customerNameDisplay?: string;
}

interface SparePartOption {
  id: number;
  partNo: string;
  name: string;
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

const USAGE_STATUS_MAP: Record<string, string> = {
  PENDING: '待出库',
  OUTBOUNDED: '已出库',
};

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  message.error(err?.response?.data?.message ?? err?.message ?? '操作失败');
}

export default function WorkOrderInternalDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const userInfo = useAuthStore((s) => s.userInfo);
  const { has } = useEffectivePermissions();
  const [detail, setDetail] = useState<WorkOrderDetailDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // Modals
  const [dispatchOpen, setDispatchOpen] = useState(false);
  const [recordOpen, setRecordOpen] = useState(false);
  const [partOpen, setPartOpen] = useState(false);
  const [completeOpen, setCompleteOpen] = useState(false);
  const [spareParts, setSpareParts] = useState<SparePartOption[]>([]);

  const [dispatchForm] = Form.useForm<{ serviceManagerId: number }>();
  const [recordForm] = Form.useForm<{ description: string; attachmentUrl?: string }>();
  const [partForm] = Form.useForm<{ partNo: string; quantity: number }>();
  const [completeForm] = Form.useForm<{ signatureUrl: string }>();

  useEffect(() => {
    if (id) fetchDetail(Number(id));
  }, [id]);

  async function fetchDetail(idNum: number) {
    setLoading(true);
    try {
      const { data } = await request.get<WorkOrderDetailDto>(`/v1/work-orders/${idNum}`);
      setDetail(data);
    } catch {
      setDetail(null);
    } finally {
      setLoading(false);
    }
  }

  async function loadSpareParts() {
    try {
      const { data } = await request.get<{ content?: SparePartOption[] }>('/v1/masterdata/spare-parts', {
        params: { page: 0, size: 500, status: 'ENABLE' },
      });
      const list = data?.content ?? [];
      setSpareParts(Array.isArray(list) ? list : []);
    } catch {
      setSpareParts([]);
    }
  }

  const handleDispatch = async () => {
    const values = await dispatchForm.validateFields();
    if (!id) return;
    setSubmitting(true);
    try {
      await request.post('/v1/work-orders/dispatch', {
        workOrderId: Number(id),
        serviceManagerId: values.serviceManagerId,
      });
      message.success('派单成功');
      setDispatchOpen(false);
      dispatchForm.resetFields();
      await fetchDetail(Number(id));
    } catch (e) {
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleAccept = async () => {
    if (!id) return;
    setSubmitting(true);
    try {
      await request.post('/v1/work-orders/accept', { id: Number(id) });
      message.success('接单成功');
      await fetchDetail(Number(id));
    } catch (e) {
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleAddRecord = async () => {
    const values = await recordForm.validateFields();
    if (!id) return;
    setSubmitting(true);
    try {
      await request.post('/v1/work-orders/record', {
        id: Number(id),
        description: values.description ?? '',
        attachmentUrl: values.attachmentUrl || undefined,
      });
      message.success('记录已添加');
      setRecordOpen(false);
      recordForm.resetFields();
      await fetchDetail(Number(id));
    } catch (e) {
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleAddPart = async () => {
    const values = await partForm.validateFields();
    if (!id) return;
    setSubmitting(true);
    try {
      await request.post('/v1/work-orders/parts/add', {
        id: Number(id),
        partNo: values.partNo,
        quantity: values.quantity,
      });
      message.success('配件已添加');
      setPartOpen(false);
      partForm.resetFields();
      await fetchDetail(Number(id));
    } catch (e) {
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleConsumePart = async (woPartId: number) => {
    setSubmitting(true);
    try {
      await request.post('/v1/work-orders/parts/consume', { woPartId });
      message.success('已触发出库');
      if (id) await fetchDetail(Number(id));
    } catch (e) {
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleSubmitSign = async () => {
    if (!id) return;
    setSubmitting(true);
    try {
      await request.post('/v1/work-orders/sign', { id: Number(id) });
      message.success('已提交签字');
      await fetchDetail(Number(id));
    } catch (e) {
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleComplete = async () => {
    const values = await completeForm.validateFields();
    if (!id) return;
    setSubmitting(true);
    try {
      await request.post('/v1/work-orders/complete', {
        id: Number(id),
        signatureUrl: values.signatureUrl ?? '',
      });
      message.success('签字完成，工单已完修');
      setCompleteOpen(false);
      completeForm.resetFields();
      await fetchDetail(Number(id));
    } catch (e) {
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading || !detail || !detail.workOrder) {
    return (
      <div style={{ padding: 24 }}>
        {loading ? '加载中...' : '未找到工单'}
      </div>
    );
  }

  const wo = detail.workOrder;
  const records = detail.records ?? [];
  const parts = detail.parts ?? [];
  const isAssignee = userInfo?.id != null && String(userInfo.id) === String(wo.serviceManagerId);
  const status = wo.status;
  const canDispatch = status === 'ACCEPTED_BY_DISPATCHER';
  const canAccept = status === 'DISPATCHED' && isAssignee;
  const canStart = status === 'ACCEPTED' && isAssignee;
  const canSubmitSign = status === 'PROCESSING' && isAssignee;
  const canComplete = status === 'PENDING_SIGN' && isAssignee;
  const canAddRecordOrParts = (status === 'ACCEPTED' || status === 'PROCESSING') && isAssignee;

  return (
    <div style={{ padding: 24 }}>
      <Button
        type="text"
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/work-orders')}
        style={{ marginBottom: 16 }}
      >
        返回工单列表
      </Button>

      <Card
        title={
          <Space>
            <span>工单 {wo.orderNo}</span>
            <Tag color={status === 'COMPLETED' ? 'green' : status === 'CANCELLED' ? 'default' : 'blue'}>
              {STATUS_MAP[status] ?? status}
            </Tag>
          </Space>
        }
        extra={
          <Space wrap>
            {canDispatch && has('work_order:ASSIGN') && (
              <Button type="primary" onClick={() => setDispatchOpen(true)}>
                派单
              </Button>
            )}
            {canAccept && has('work_order:SUBMIT') && (
              <Button type="primary" onClick={handleAccept} loading={submitting}>
                接单
              </Button>
            )}
            {canStart && has('work_order:EDIT') && (
              <Button type="primary" onClick={() => setRecordOpen(true)}>
                开始处理
              </Button>
            )}
            {canAddRecordOrParts && has('work_order:EDIT') && (
              <>
                <Button onClick={() => { setRecordOpen(true); }}>添加记录</Button>
                <Button
                  onClick={() => {
                    loadSpareParts();
                    setPartOpen(true);
                  }}
                >
                  添加配件
                </Button>
              </>
            )}
            {canSubmitSign && has('work_order:APPROVE') && (
              <Button type="primary" onClick={handleSubmitSign} loading={submitting}>
                提交签字
              </Button>
            )}
            {canComplete && has('work_order:CLOSE') && (
              <Button type="primary" onClick={() => setCompleteOpen(true)}>
                完修
              </Button>
            )}
          </Space>
        }
      >
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="客户名称">{detail?.customerNameDisplay ?? wo?.customerName}</Descriptions.Item>
          <Descriptions.Item label="联系人">{wo.contactPerson}</Descriptions.Item>
          <Descriptions.Item label="电话">{wo.contactPhone}</Descriptions.Item>
          <Descriptions.Item label="服务地址" span={2}>
            {wo.serviceAddress}
            {wo.serviceAddress && (
              <a
                href={`https://www.amap.com/search?query=${encodeURIComponent(wo.serviceAddress)}`}
                target="_blank"
                rel="noopener noreferrer"
                title="打开高德地图"
                style={{ marginLeft: 8 }}
              >
                <EnvironmentOutlined /> 打开高德地图
              </a>
            )}
          </Descriptions.Item>
          {wo.machineNo != null && (
            <Descriptions.Item label="机器编号">{wo.machineNo}</Descriptions.Item>
          )}
          {wo.issueDescription != null && (
            <Descriptions.Item label="问题描述" span={2}>{wo.issueDescription}</Descriptions.Item>
          )}
          {wo.appointmentTime != null && (
            <Descriptions.Item label="预约时间">
              {wo.appointmentTime ? dayjs(wo.appointmentTime).format('YYYY-MM-DD HH:mm') : '-'}
            </Descriptions.Item>
          )}
        </Descriptions>
      </Card>

      <Card title="过程记录" style={{ marginTop: 16 }}>
        <Timeline
          items={records
            .slice()
            .sort((a, b) => (new Date(b.createdAt ?? 0).getTime() - new Date(a.createdAt ?? 0).getTime()))
            .map((r) => ({
              children: (
                <div>
                  <div>{r.description ?? '(无描述)'}</div>
                  {r.attachmentUrl && (
                    <a href={r.attachmentUrl} target="_blank" rel="noopener noreferrer">
                      附件
                    </a>
                  )}
                  <div style={{ color: '#999', fontSize: 12 }}>
                    {r.createdAt ? dayjs(r.createdAt).format('YYYY-MM-DD HH:mm') : ''}
                    {r.createdBy ? ` · ${r.createdBy}` : ''}
                  </div>
                </div>
              ),
            }))}
        />
        {records.length === 0 && <div style={{ color: '#999' }}>暂无记录</div>}
      </Card>

      <Card title="配件清单" style={{ marginTop: 16 }}>
        <Table
          dataSource={parts}
          rowKey="id"
          size="small"
          columns={[
            { title: '配件编号', dataIndex: 'partNo', width: 140 },
            { title: '配件名称', dataIndex: 'partName', ellipsis: true },
            { title: '数量', dataIndex: 'quantity', width: 80 },
            {
              title: '状态',
              dataIndex: 'usageStatus',
              width: 100,
              render: (s: string) => USAGE_STATUS_MAP[s] ?? s,
            },
            {
              title: '操作',
              width: 100,
              render: (_, row: WorkOrderPart) =>
                row.usageStatus === 'PENDING' && has('work_order:EDIT') ? (
                  <Button
                    type="link"
                    size="small"
                    onClick={() => handleConsumePart(row.id)}
                    loading={submitting}
                  >
                    消耗配件
                  </Button>
                ) : null,
            },
          ]}
          pagination={false}
        />
        {parts.length === 0 && <div style={{ color: '#999' }}>暂无配件</div>}
      </Card>

      <Modal
        title="派单"
        open={dispatchOpen}
        onOk={handleDispatch}
        onCancel={() => { setDispatchOpen(false); dispatchForm.resetFields(); }}
        confirmLoading={submitting}
        okText="确定"
        cancelText="取消"
      >
        <Form form={dispatchForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="serviceManagerId"
            label="服务负责人（用户ID）"
            rules={[{ required: true, message: '请输入用户ID' }]}
          >
            <InputNumber min={1} placeholder="输入负责人用户ID" style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="添加记录"
        open={recordOpen}
        onOk={handleAddRecord}
        onCancel={() => { setRecordOpen(false); recordForm.resetFields(); }}
        confirmLoading={submitting}
        okText="确定"
        cancelText="取消"
      >
        <Form form={recordForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="description" label="记录内容">
            <Input.TextArea rows={4} placeholder="过程描述" />
          </Form.Item>
          <Form.Item name="attachmentUrl" label="附件链接">
            <Input placeholder="可选，填写附件URL" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="添加配件"
        open={partOpen}
        onOk={handleAddPart}
        onCancel={() => { setPartOpen(false); partForm.resetFields(); }}
        confirmLoading={submitting}
        okText="确定"
        cancelText="取消"
      >
        <Form form={partForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="partNo"
            label="配件"
            rules={[{ required: true, message: '请选择配件' }]}
          >
            <Select
              placeholder="选择配件编号"
              showSearch
              optionFilterProp="label"
              options={spareParts.map((p) => ({ label: `${p.partNo} - ${p.name}`, value: p.partNo }))}
            />
          </Form.Item>
          <Form.Item
            name="quantity"
            label="数量"
            initialValue={1}
            rules={[{ required: true, message: '请输入数量' }]}
          >
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="完成签字"
        open={completeOpen}
        onOk={handleComplete}
        onCancel={() => { setCompleteOpen(false); completeForm.resetFields(); }}
        confirmLoading={submitting}
        okText="确定"
        cancelText="取消"
      >
        <Form form={completeForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="signatureUrl"
            label="签字图片链接"
            rules={[{ required: true, message: '请填写签字图片URL' }]}
          >
            <Input placeholder="上传签字图片后填写URL，或粘贴图片地址" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
