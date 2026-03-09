import { useRef, useState } from 'react';
import { ProTable, ModalForm, ProFormSelect, ProFormText } from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Drawer, Descriptions, Button, message, Modal, Input, Form, InputNumber, DatePicker, Select, Tooltip } from 'antd';
import { useNavigate, useLocation, Navigate } from 'react-router-dom';
import request from '@/shared/utils/request';
import dayjs, { type Dayjs } from 'dayjs';
import { AttachmentList } from '@/shared/components/AttachmentList';
import { toProTableResult, type SpringPage } from '@/shared/utils/adapters';
import { useEffectivePermissions } from '@/shared/hooks/useEffectivePermissions';
import { useIsAdminOrSuperAdmin } from '@/shared/components/AdminOrSuperAdminGuard';
import { useSimulatedRoleStore } from '@/shared/stores/useSimulatedRoleStore';
import { isShipmentWorkflowActionAllowedForSimulatedRole } from '@/shared/config/roleToUiGatingMatrix';

const OPERATOR_ID = 1;

const PARAMS = { operatorId: OPERATOR_ID };

/** Permission keys (must match backend ShipmentController constants). */
const PERM_VIEW = 'shipment.shipments:VIEW';
const PERM_ACCEPT = 'shipment.shipments:ACCEPT';
const PERM_PREPARE = 'shipment.shipments:PREPARE';
const PERM_SHIP = 'shipment.shipments:SHIP';
const PERM_TRACKING = 'shipment.shipments:TRACKING';
const PERM_COMPLETE = 'shipment.shipments:COMPLETE';
const PERM_EXCEPTION = 'shipment.shipments:EXCEPTION';
const PERM_CANCEL = 'shipment.shipments:CANCEL';
const PERM_CLOSE = 'shipment.shipments:CLOSE';

function permKeyForAction(actionCode: string): string | null {
  switch (actionCode?.trim()) {
    case 'ACCEPT': return PERM_ACCEPT;
    case 'PREPARE': return PERM_PREPARE;
    case 'SHIP': return PERM_SHIP;
    case 'TRACKING': return PERM_TRACKING;
    case 'COMPLETE': return PERM_COMPLETE;
    case 'EXCEPTION': return PERM_EXCEPTION;
    case 'CANCEL': return PERM_CANCEL;
    case 'CLOSE': return PERM_CLOSE;
    default: return null;
  }
}

function is403(e: unknown): boolean {
  return (e as { response?: { status?: number } })?.response?.status === 403;
}

function shipmentErrorMsg(e: unknown): string {
  const data = (e as { response?: { data?: { message?: string; machineCode?: string } } })?.response?.data;
  const code = data?.machineCode;
  const msg = data?.message;
  const map: Record<string, string> = {
    SHIPMENT_MISSING_RECEIVER: '请填写收货人/联系人',
    SHIPMENT_MISSING_DELIVERY_ADDRESS: '请填写收货地址',
    SHIPMENT_MISSING_CARRIER: '请填写承运商',
    SHIPMENT_MISSING_LOGISTICS_NO: '请填写物流单号',
    SHIPMENT_MACHINE_NOT_FOUND: '所选设备不属于该发运单',
  };
  return (code && map[code]) ? map[code] : (msg ?? '操作失败，请稍后重试');
}

/** CN labels aligned with backend workflow step (申请/备货/发运/签收完成/异常/已取消). */
const STATUS_LABEL_CN: Record<string, string> = {
  CREATED: '申请',
  PENDING_SHIP: '备货',
  SHIPPED: '发运',
  COMPLETED: '签收完成',
  EXCEPTION: '异常',
  CANCELLED: '已取消',
};

interface WorkflowActionDto {
  actionCode: string;
  labelCn: string;
  method: string;
  path: string;
  confirmTextCn?: string | null;
}

interface ShipmentWorkflowDto {
  shipmentId: number;
  status: string;
  stepCode: string;
  stepLabelCn: string;
  actions: WorkflowActionDto[];
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
  logisticsNo?: string;
  ticketUrl?: string;
  receiptUrl?: string;
  createdAt?: string;
}

interface ExceptionRecordDto {
  id: number;
  machineId?: number | null;
  exceptionType?: string | null;
  description: string;
  responsibility?: string | null;
  evidenceUrl?: string | null;
  operatorId?: number | null;
  createdAt?: string;
}

interface ShipmentMachineItem {
  id: number;
  shipmentId: number;
  model?: string;
  machineNo: string;
}

interface CustomerItem {
  id: number;
  customerCode: string;
  name: string;
  status: string;
}

const SIMULATOR_DISABLED_TOOLTIP = '该角色不可操作';

export default function Shipment() {
  const navigate = useNavigate();
  const location = useLocation();
  const { has, loading: permLoading } = useEffectivePermissions();
  const isAdminOrSuperAdmin = useIsAdminOrSuperAdmin();
  const simulatedRole = useSimulatedRoleStore((s) => s.simulatedRole);
  const actionRef = useRef<ActionType>(null);
  const [detailId, setDetailId] = useState<number | null>(null);
  const [detail, setDetail] = useState<ShipmentDetail | null>(null);
  const [workflow, setWorkflow] = useState<ShipmentWorkflowDto | null>(null);
  const [workflowError, setWorkflowError] = useState(false);
  const [applyAfterSalesOpen, setApplyAfterSalesOpen] = useState(false);
  type StepModalCode = 'ACCEPT' | 'PREPARE' | 'SHIP' | 'TRACKING' | 'EXCEPTION' | null;
  const [stepModal, setStepModal] = useState<StepModalCode>(null);
  const [acceptForm] = Form.useForm();
  const [prepareForm] = Form.useForm();
  const [shipForm] = Form.useForm();
  const [trackingForm] = Form.useForm();
  const [exceptionForm] = Form.useForm();
  const [exceptionRecords, setExceptionRecords] = useState<ExceptionRecordDto[]>([]);
  const [shipmentMachines, setShipmentMachines] = useState<ShipmentMachineItem[]>([]);
  const simulatorDisable = !isShipmentWorkflowActionAllowedForSimulatedRole(simulatedRole);

  const columns: ProColumns<ShipmentListRow>[] = [
    { title: '发货单号', dataIndex: 'shipmentNo', width: 160 },
    { title: '客户', dataIndex: 'customerName', ellipsis: true },
    {
      title: '状态',
      dataIndex: 'status',
      width: 120,
      valueEnum: {
        CREATED: { text: STATUS_LABEL_CN.CREATED ?? '申请', status: 'Default' },
        PENDING_SHIP: { text: STATUS_LABEL_CN.PENDING_SHIP ?? '备货', status: 'Warning' },
        SHIPPED: { text: STATUS_LABEL_CN.SHIPPED ?? '发运', status: 'Processing' },
        COMPLETED: { text: STATUS_LABEL_CN.COMPLETED ?? '签收完成', status: 'Success' },
        EXCEPTION: { text: STATUS_LABEL_CN.EXCEPTION ?? '异常', status: 'Error' },
        CANCELLED: { text: STATUS_LABEL_CN.CANCELLED ?? '已取消', status: 'Error' },
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
    setWorkflowError(false);
    setWorkflow(null);
    setExceptionRecords([]);
    setShipmentMachines([]);
    try {
      const { data } = await request.get<ShipmentDetail>(`/v1/shipments/${id}`);
      setDetail(data);
    } catch (e: unknown) {
      setDetail(null);
      if (is403(e)) {
        message.error('无权限访问发运单');
        setDetailId(null);
        navigate('/dashboard', { state: { from: location.pathname, reason: 'forbidden' } });
      }
    }
    try {
      const { data: wf } = await request.get<ShipmentWorkflowDto>(`/v1/shipments/${id}/workflow`);
      setWorkflow(wf);
    } catch (e: unknown) {
      setWorkflowError(true);
      if (is403(e)) {
        message.error('无权限访问发运单');
        setDetailId(null);
        setWorkflow(null);
        navigate('/dashboard', { state: { from: location.pathname, reason: 'forbidden' } });
      }
    }
    try {
      const [{ data: exList }, { data: machines }] = await Promise.all([
        request.get<ExceptionRecordDto[]>(`/v1/shipments/${id}/exceptions`),
        request.get<ShipmentMachineItem[]>(`/v1/shipments/${id}/machines`),
      ]);
      setExceptionRecords(Array.isArray(exList) ? exList : []);
      setShipmentMachines(Array.isArray(machines) ? machines : []);
    } catch (e: unknown) {
      if (!is403(e)) {
        setExceptionRecords([]);
        setShipmentMachines([]);
      }
    }
  }

  async function refreshDetailAndWorkflow() {
    if (detailId == null) return;
    try {
      const [{ data: d }, { data: wf }, { data: exList }] = await Promise.all([
        request.get<ShipmentDetail>(`/v1/shipments/${detailId}`),
        request.get<ShipmentWorkflowDto>(`/v1/shipments/${detailId}/workflow`),
        request.get<ExceptionRecordDto[]>(`/v1/shipments/${detailId}/exceptions`),
      ]);
      setDetail(d);
      setWorkflow(wf);
      setWorkflowError(false);
      setExceptionRecords(Array.isArray(exList) ? exList : []);
    } catch (e: unknown) {
      setWorkflowError(true);
      if (is403(e)) {
        message.error('无权限访问发运单');
        setDetailId(null);
        setDetail(null);
        setWorkflow(null);
      }
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

  async function runWorkflowAction(action: WorkflowActionDto) {
    if (detailId == null) return;
    const code = action.actionCode;
    const base = `/v1/shipments/${detailId}`;
    try {
      if (code === 'COMPLETE') {
        await request.post(`${base}/complete`, undefined, { params: PARAMS });
        message.success('操作成功');
        await refreshDetailAndWorkflow();
      } else if (code === 'CLOSE') {
        await request.post(`${base}/close`, undefined, { params: PARAMS });
        message.success('操作成功');
        await refreshDetailAndWorkflow();
      } else if (code === 'EXCEPTION') {
        return;
      } else if (code === 'CANCEL') {
        return new Promise<void>((resolve) => {
          let reason = '';
          Modal.confirm({
            title: action.confirmTextCn ?? '确认取消？',
            content: (
              <div style={{ marginTop: 8 }}>
                <Input placeholder="取消原因（可选）" onChange={(e) => { reason = e.target.value; }} />
              </div>
            ),
            okText: '确定',
            cancelText: '取消',
            onOk: async () => {
              await request.post(`${base}/cancel`, { reason: reason?.trim() || undefined }, { params: PARAMS });
              message.success('已取消');
              await refreshDetailAndWorkflow();
              resolve();
            },
          });
        });
      }
    } catch (e: unknown) {
      if (is403(e)) {
        message.error('无权限访问发运单');
        void refreshDetailAndWorkflow();
      } else {
        message.error(shipmentErrorMsg(e));
      }
    }
  }

  function handleActionClick(action: WorkflowActionDto) {
    const code = action.actionCode;
    if (code === 'ACCEPT') {
      acceptForm.setFieldsValue({
        receiverName: detail?.receiverName ?? '',
        receiverPhone: detail?.receiverPhone ?? '',
        deliveryAddress: detail?.deliveryAddress ?? '',
        contractNo: detail?.contractNo ?? '',
        shipDate: detail?.shipDate ? dayjs(detail.shipDate) : undefined,
        remark: undefined,
      });
      setStepModal('ACCEPT');
      return;
    }
    if (code === 'PREPARE') {
      prepareForm.resetFields();
      setStepModal('PREPARE');
      return;
    }
    if (code === 'SHIP') {
      shipForm.setFieldsValue({
        carrier: detail?.carrier ?? '',
        logisticsNo: detail?.logisticsNo ?? '',
        receiptUrl: detail?.receiptUrl ?? '',
        ticketUrl: detail?.ticketUrl ?? '',
        remark: undefined,
      });
      setStepModal('SHIP');
      return;
    }
    if (code === 'TRACKING') {
      trackingForm.setFieldsValue({
        logisticsNo: detail?.logisticsNo ?? '',
        ticketUrl: detail?.ticketUrl ?? '',
        receiptUrl: detail?.receiptUrl ?? '',
        remark: undefined,
      });
      setStepModal('TRACKING');
      return;
    }
    if (code === 'EXCEPTION') {
      exceptionForm.resetFields();
      setStepModal('EXCEPTION');
      return;
    }
    if ((code === 'COMPLETE' || code === 'CLOSE') && action.confirmTextCn) {
      Modal.confirm({
        title: action.confirmTextCn,
        okText: '确定',
        cancelText: '取消',
        onOk: () => runWorkflowAction(action),
      });
      return;
    }
    void runWorkflowAction(action);
  }

  if (permLoading) {
    return <div style={{ padding: 24 }}>加载中...</div>;
  }
  if (!has(PERM_VIEW) && !isAdminOrSuperAdmin) {
    return <Navigate to="/dashboard" state={{ from: location.pathname, reason: 'forbidden' }} replace />;
  }

  const visibleActions = (actions: WorkflowActionDto[]) =>
    actions.filter((a) => {
      const key = permKeyForAction(a.actionCode);
      return key != null && (has(key) || isAdminOrSuperAdmin);
    });

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
          } catch (e: unknown) {
            if (is403(e)) {
              message.error('无权限访问发运单');
              navigate('/dashboard', { state: { from: location.pathname, reason: 'forbidden' } });
            }
            return { data: [], total: 0, success: true };
          }
        }}
        rowKey="id"
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
        headerTitle="发货列表"
        toolBarRender={() => [
          simulatorDisable ? (
            <Tooltip key="create" title={SIMULATOR_DISABLED_TOOLTIP}>
              <span style={{ display: 'inline-block' }}>
                <Button type="primary" disabled>新建发货单</Button>
              </span>
            </Tooltip>
          ) : (
          <ModalForm<{ customerId: number; shipmentType: string }>
            key="create"
            title="新建发货单"
            trigger={<Button type="primary">新建发货单</Button>}
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
          </ModalForm>
          ),
        ]}
      />
      <Drawer
        title="发货详情"
        open={detailId !== null}
        onClose={() => { setDetailId(null); setDetail(null); setWorkflow(null); setWorkflowError(false); }}
        width={520}
      >
        {detail && (
          <>
            {workflowError && (
              <div style={{ marginBottom: 12, color: '#faad14' }}>
                流程信息加载失败，可刷新重试
              </div>
            )}
            {workflow && (
              <div style={{ marginBottom: 16 }}>
                <div style={{ marginBottom: 8 }}>
                  <strong>当前状态：</strong>
                  {STATUS_LABEL_CN[detail.status] ?? detail.status}
                </div>
                <div style={{ marginBottom: 8 }}>
                  <strong>当前步骤：</strong>
                  {workflow.stepLabelCn}
                </div>
                {workflow.actions.length > 0 && (
                  <div style={{ marginTop: 12 }}>
                    <strong>可执行操作：</strong>
                    <div style={{ marginTop: 8, display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                      {visibleActions(workflow.actions).map((a) => {
                        const simulatorDisable = !isShipmentWorkflowActionAllowedForSimulatedRole(simulatedRole);
                        if (simulatorDisable) {
                          return (
                            <Tooltip key={a.actionCode} title={SIMULATOR_DISABLED_TOOLTIP}>
                              <span style={{ display: 'inline-block' }}>
                                <Button size="small" disabled>
                                  {a.labelCn}
                                </Button>
                              </span>
                            </Tooltip>
                          );
                        }
                        return (
                          <Button
                            key={a.actionCode}
                            size="small"
                            onClick={() => handleActionClick(a)}
                          >
                            {a.labelCn}
                          </Button>
                        );
                      })}
                    </div>
                  </div>
                )}
              </div>
            )}
            <div style={{ marginBottom: 16 }}>
              {simulatorDisable ? (
                <Tooltip title={SIMULATOR_DISABLED_TOOLTIP}>
                  <span style={{ display: 'inline-block' }}>
                    <Button type="primary" size="small" disabled>发起售后</Button>
                  </span>
                </Tooltip>
              ) : (
                <Button type="primary" size="small" onClick={() => setApplyAfterSalesOpen(true)}>
                  发起售后
                </Button>
              )}
            </div>
            <Descriptions column={1} bordered size="small">
              <Descriptions.Item label="ID">{detail.id}</Descriptions.Item>
              <Descriptions.Item label="报价单ID">{detail.quoteId ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="类型">{detail.type ?? '-'}</Descriptions.Item>
              <Descriptions.Item label="状态">{STATUS_LABEL_CN[detail.status] ?? detail.status}</Descriptions.Item>
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
            <div style={{ marginTop: 16 }}>
              <strong>异常记录</strong>
              {exceptionRecords.length === 0 ? (
                <div style={{ marginTop: 8, color: '#999' }}>暂无异常记录</div>
              ) : (
                <table style={{ marginTop: 8, width: '100%', borderCollapse: 'collapse', fontSize: 12 }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid #eee' }}>
                      <th style={{ textAlign: 'left', padding: '4px 8px' }}>时间</th>
                      <th style={{ textAlign: 'left', padding: '4px 8px' }}>设备</th>
                      <th style={{ textAlign: 'left', padding: '4px 8px' }}>类型/责任</th>
                      <th style={{ textAlign: 'left', padding: '4px 8px' }}>说明</th>
                      <th style={{ textAlign: 'left', padding: '4px 8px' }}>凭证</th>
                    </tr>
                  </thead>
                  <tbody>
                    {exceptionRecords.map((r) => (
                      <tr key={r.id} style={{ borderBottom: '1px solid #f0f0f0' }}>
                        <td style={{ padding: '4px 8px' }}>
                          {r.createdAt ? dayjs(r.createdAt).format('YYYY-MM-DD HH:mm') : '-'}
                        </td>
                        <td style={{ padding: '4px 8px' }}>
                          {r.machineId != null
                            ? (shipmentMachines.find((m) => m.id === r.machineId)?.machineNo ?? `#${r.machineId}`)
                            : '-'}
                        </td>
                        <td style={{ padding: '4px 8px' }}>
                          {[r.exceptionType, r.responsibility].filter(Boolean).join(' / ') || '-'}
                        </td>
                        <td style={{ padding: '4px 8px' }}>{r.description ?? '-'}</td>
                        <td style={{ padding: '4px 8px' }}>
                          {r.evidenceUrl ? (
                            <a href={r.evidenceUrl} target="_blank" rel="noopener noreferrer">链接</a>
                          ) : (
                            '-'
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
            {/* Step-02: ACCEPT / PREPARE / SHIP / TRACKING modals */}
            <Modal
              title="审核并补充"
              open={stepModal === 'ACCEPT'}
              onCancel={() => setStepModal(null)}
              destroyOnClose
              footer={null}
            >
              <Form
                form={acceptForm}
                layout="vertical"
                onFinish={async (values: { receiverName?: string; receiverPhone?: string; deliveryAddress?: string; contractNo?: string; shipDate?: Dayjs; remark?: string }) => {
                  const rn = values.receiverName?.trim();
                  const addr = values.deliveryAddress?.trim();
                  if (!rn) { message.error('请填写收货人/联系人'); return; }
                  if (!addr) { message.error('请填写收货地址'); return; }
                  if (detailId == null) return;
                  try {
                    await request.post(`/v1/shipments/${detailId}/accept`, {
                      receiverName: rn,
                      receiverPhone: values.receiverPhone?.trim() || undefined,
                      deliveryAddress: addr,
                      contractNo: values.contractNo?.trim() || undefined,
                      shipDate: values.shipDate ? values.shipDate.format('YYYY-MM-DD') : undefined,
                      remark: values.remark?.trim() || undefined,
                    }, { params: PARAMS });
                    message.success('操作成功');
                    setStepModal(null);
                    acceptForm.resetFields();
                    await refreshDetailAndWorkflow();
                  } catch (e) {
                    message.error(shipmentErrorMsg(e));
                  }
                }}
              >
                <Form.Item name="receiverName" label="收货人/联系人" rules={[{ required: true, message: '请填写收货人/联系人' }]}>
                  <Input placeholder="收货人/联系人" />
                </Form.Item>
                <Form.Item name="receiverPhone" label="电话">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="deliveryAddress" label="收货地址" rules={[{ required: true, message: '请填写收货地址' }]}>
                  <Input.TextArea placeholder="收货地址" rows={2} />
                </Form.Item>
                <Form.Item name="contractNo" label="合同号">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="shipDate" label="计划发货日期">
                  <DatePicker style={{ width: '100%' }} />
                </Form.Item>
                <Form.Item name="remark" label="备注">
                  <Input.TextArea placeholder="可选" rows={2} />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" htmlType="submit" style={{ marginRight: 8 }}>提交</Button>
                  <Button onClick={() => setStepModal(null)}>取消</Button>
                </Form.Item>
              </Form>
            </Modal>

            <Modal
              title="备货确认"
              open={stepModal === 'PREPARE'}
              onCancel={() => setStepModal(null)}
              destroyOnClose
              footer={null}
            >
              <Form
                form={prepareForm}
                layout="vertical"
                onFinish={async (values: Record<string, unknown>) => {
                  if (detailId == null) return;
                  try {
                    await request.post(`/v1/shipments/${detailId}/prepare`, {
                      quantity: values.quantity,
                      model: values.model ?? undefined,
                      needPackaging: values.needPackaging,
                      entrustMatter: values.entrustMatter ?? undefined,
                      pickupContact: values.pickupContact ?? undefined,
                      pickupPhone: values.pickupPhone ?? undefined,
                      needLoading: values.needLoading,
                      pickupAddress: values.pickupAddress ?? undefined,
                      deliveryAddress: values.deliveryAddress ?? undefined,
                      remark: values.remark ?? undefined,
                    }, { params: PARAMS });
                    message.success('操作成功');
                    setStepModal(null);
                    prepareForm.resetFields();
                    await refreshDetailAndWorkflow();
                  } catch (e) {
                    message.error(shipmentErrorMsg(e));
                  }
                }}
              >
                <Form.Item name="quantity" label="数量">
                  <InputNumber min={1} style={{ width: '100%' }} />
                </Form.Item>
                <Form.Item name="model" label="型号">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="needPackaging" label="需要包装">
                  <Select allowClear options={[{ label: '是', value: true }, { label: '否', value: false }]} placeholder="可选" />
                </Form.Item>
                <Form.Item name="entrustMatter" label="委托事项">
                  <Input.TextArea placeholder="可选" rows={2} />
                </Form.Item>
                <Form.Item name="pickupContact" label="提货联系人">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="pickupPhone" label="提货电话">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="needLoading" label="需要装车">
                  <Select allowClear options={[{ label: '是', value: true }, { label: '否', value: false }]} placeholder="可选" />
                </Form.Item>
                <Form.Item name="pickupAddress" label="提货地址">
                  <Input.TextArea placeholder="可选" rows={2} />
                </Form.Item>
                <Form.Item name="deliveryAddress" label="收货地址">
                  <Input.TextArea placeholder="可选" rows={2} />
                </Form.Item>
                <Form.Item name="remark" label="备注">
                  <Input.TextArea placeholder="可选" rows={2} />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" htmlType="submit" style={{ marginRight: 8 }}>提交</Button>
                  <Button onClick={() => setStepModal(null)}>取消</Button>
                </Form.Item>
              </Form>
            </Modal>

            <Modal
              title="发运"
              open={stepModal === 'SHIP'}
              onCancel={() => setStepModal(null)}
              destroyOnClose
              footer={null}
            >
              <Form
                form={shipForm}
                layout="vertical"
                onFinish={async (values: { carrier?: string; logisticsNo?: string; receiptUrl?: string; ticketUrl?: string; remark?: string }) => {
                  const carrier = values.carrier?.trim();
                  const logisticsNo = values.logisticsNo?.trim();
                  if (!carrier) { message.error('请填写承运商'); return; }
                  if (!logisticsNo) { message.error('请填写物流单号'); return; }
                  if (detailId == null) return;
                  try {
                    await request.post(`/v1/shipments/${detailId}/ship`, {
                      carrier,
                      logisticsNo,
                      receiptUrl: values.receiptUrl?.trim() || undefined,
                      ticketUrl: values.ticketUrl?.trim() || undefined,
                    }, { params: PARAMS });
                    message.success('操作成功');
                    setStepModal(null);
                    shipForm.resetFields();
                    await refreshDetailAndWorkflow();
                  } catch (e) {
                    message.error(shipmentErrorMsg(e));
                  }
                }}
              >
                <Form.Item name="carrier" label="承运商" rules={[{ required: true, message: '请填写承运商' }]}>
                  <Input placeholder="承运商" />
                </Form.Item>
                <Form.Item name="logisticsNo" label="物流单号" rules={[{ required: true, message: '请填写物流单号' }]}>
                  <Input placeholder="物流单号" />
                </Form.Item>
                <Form.Item name="receiptUrl" label="回单URL">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="ticketUrl" label="运单URL">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="remark" label="备注">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" htmlType="submit" style={{ marginRight: 8 }}>提交</Button>
                  <Button onClick={() => setStepModal(null)}>取消</Button>
                </Form.Item>
              </Form>
            </Modal>

            <Modal
              title="更新物流信息"
              open={stepModal === 'TRACKING'}
              onCancel={() => setStepModal(null)}
              destroyOnClose
              footer={null}
            >
              <Form
                form={trackingForm}
                layout="vertical"
                onFinish={async (values: { logisticsNo?: string; ticketUrl?: string; receiptUrl?: string; remark?: string }) => {
                  if (detailId == null) return;
                  try {
                    await request.post(`/v1/shipments/${detailId}/tracking`, {
                      logisticsNo: values.logisticsNo?.trim() || undefined,
                      ticketUrl: values.ticketUrl?.trim() || undefined,
                      receiptUrl: values.receiptUrl?.trim() || undefined,
                      remark: values.remark?.trim() || undefined,
                    }, { params: PARAMS });
                    message.success('操作成功');
                    setStepModal(null);
                    trackingForm.resetFields();
                    await refreshDetailAndWorkflow();
                  } catch (e) {
                    message.error(shipmentErrorMsg(e));
                  }
                }}
              >
                <Form.Item name="logisticsNo" label="物流单号">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="ticketUrl" label="运单URL">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="receiptUrl" label="回单URL">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="remark" label="备注">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" htmlType="submit" style={{ marginRight: 8 }}>提交</Button>
                  <Button onClick={() => setStepModal(null)}>取消</Button>
                </Form.Item>
              </Form>
            </Modal>

            <Modal
              title="标记异常（人工填写）"
              open={stepModal === 'EXCEPTION'}
              onCancel={() => setStepModal(null)}
              destroyOnClose
              footer={null}
            >
              <Form
                form={exceptionForm}
                layout="vertical"
                onFinish={async (values: { reason?: string; exceptionType?: string; responsibility?: string; evidenceUrl?: string; machineId?: number }) => {
                  const reason = values.reason?.trim();
                  if (!reason) {
                    message.error('异常原因不能为空');
                    return;
                  }
                  if (detailId == null) return;
                  try {
                    await request.post(`/v1/shipments/${detailId}/exception`, {
                      reason,
                      exceptionType: values.exceptionType?.trim() || undefined,
                      responsibility: values.responsibility?.trim() || undefined,
                      evidenceUrl: values.evidenceUrl?.trim() || undefined,
                      machineId: values.machineId ?? undefined,
                    }, { params: PARAMS });
                    message.success('已标记异常');
                    setStepModal(null);
                    exceptionForm.resetFields();
                    await refreshDetailAndWorkflow();
                  } catch (e) {
                    message.error(shipmentErrorMsg(e));
                  }
                }}
              >
                <Form.Item name="reason" label="异常原因（必填，人工填写）" rules={[{ required: true, message: '请填写异常原因' }]}>
                  <Input.TextArea placeholder="请人工填写异常原因" rows={3} />
                </Form.Item>
                <Form.Item name="exceptionType" label="异常类型">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="responsibility" label="责任方">
                  <Input placeholder="可选" />
                </Form.Item>
                <Form.Item name="evidenceUrl" label="凭证链接">
                  <Input placeholder="可粘贴链接" />
                </Form.Item>
                {shipmentMachines.length > 0 && (
                  <Form.Item name="machineId" label="关联设备">
                    <Select
                      allowClear
                      placeholder="可选"
                      options={shipmentMachines.map((m) => ({ label: `${m.machineNo}${m.model ? ` (${m.model})` : ''}`, value: m.id }))}
                    />
                  </Form.Item>
                )}
                <Form.Item>
                  <Button type="primary" htmlType="submit" style={{ marginRight: 8 }}>提交</Button>
                  <Button onClick={() => setStepModal(null)}>取消</Button>
                </Form.Item>
              </Form>
            </Modal>

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
