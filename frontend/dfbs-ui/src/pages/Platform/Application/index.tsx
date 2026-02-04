import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ProTable, ModalForm } from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Alert, Button, Modal, Form, Input, InputNumber, Select, Tag, message } from 'antd';
import request from '@/utils/request';
import SmartReferenceSelect from '@/components/SmartReferenceSelect';

const PLATFORM_OPTIONS = [
  { label: '映翰通', value: 'INHAND' },
  { label: '恒动', value: 'HENDONG' },
  { label: '京品', value: 'JINGPIN' },
  { label: '其他', value: 'OTHER' },
];

const SOURCE_TYPE_OPTIONS = [
  { label: '工厂渠道', value: 'FACTORY' },
  { label: '服务渠道', value: 'SERVICE' },
];

const REGION_OPTIONS = [
  { label: '华东', value: '华东' },
  { label: '华北', value: '华北' },
  { label: '华南', value: '华南' },
  { label: '西部', value: '西部' },
  { label: '海外', value: '海外' },
];

const STATUS_MAP: Record<string, { label: string; color?: string }> = {
  DRAFT: { label: '草稿', color: 'default' },
  PENDING_PLANNER: { label: '待规划', color: 'processing' },
  PENDING_ADMIN: { label: '待审核', color: 'warning' },
  APPROVED: { label: '已通过', color: 'success' },
  REJECTED: { label: '已驳回', color: 'error' },
};

interface ApplicationRow {
  id: number;
  applicationNo: string;
  status: string;
  platform: string;
  sourceType?: string;
  customerId?: number | null;
  customerName?: string | null;
  orgCodeShort: string;
  orgFullName: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  region?: string;
  salesPerson?: string;
  contractNo?: string;
  price?: number;
  quantity?: number;
  reason?: string;
  isCcPlanner?: boolean;
  rejectReason?: string;
  createdAt?: string;
  updatedAt?: string;
}

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  message.error(err?.response?.data?.message ?? err?.message ?? '操作失败');
}

/** Create modal: allows free text (new customer) or select existing. Auto-fills orgFullName from customer. */
function CustomerFieldCreate() {
  const form = Form.useFormInstance();
  const customerName = Form.useWatch('customerName', form);
  return (
    <>
      <Form.Item name="customerId" hidden><Input type="hidden" /></Form.Item>
      <Form.Item name="customerName" label="客户" rules={[{ required: true, message: '请选择或输入客户名称' }]}>
        <SmartReferenceSelect
          entityType="CUSTOMER"
          value={customerName ?? ''}
          placeholder="输入客户名称或从下拉选择已有客户"
          onChange={(ref) => {
            form.setFieldValue('customerId', ref.id ?? null);
            form.setFieldValue('customerName', ref.name);
            form.setFieldValue('orgFullName', ref.name ?? '');
          }}
        />
      </Form.Item>
    </>
  );
}

export default function PlatformApplication() {
  const navigate = useNavigate();
  const actionRef = useRef<ActionType>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [plannerModalOpen, setPlannerModalOpen] = useState(false);
  const [adminModalOpen, setAdminModalOpen] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [currentRow, setCurrentRow] = useState<ApplicationRow | null>(null);
  const [createSourceType, setCreateSourceType] = useState<'FACTORY' | 'SERVICE'>('FACTORY');
  const [plannerCustomerExists, setPlannerCustomerExists] = useState<boolean | null>(null);
  const [plannerForm] = Form.useForm<ApplicationRow & { customerName?: string }>();
  const [adminForm] = Form.useForm<ApplicationRow>();

  const columns: ProColumns<ApplicationRow>[] = [
    { title: '申请单号', dataIndex: 'applicationNo', width: 160, ellipsis: true },
    { title: '平台', dataIndex: 'platform', width: 100, valueEnum: { INHAND: '映翰通', HENDONG: '恒动', JINGPIN: '京品', OTHER: '其他' } },
    { title: '渠道', dataIndex: 'sourceType', width: 80, search: false, valueEnum: { FACTORY: '工厂', SERVICE: '服务' } },
    { title: '客户', dataIndex: 'customerName', width: 140, ellipsis: true, render: (_, row) => row.customerName ?? (row.customerId != null ? `#${row.customerId}` : '—') },
    { title: '机构代码/简称', dataIndex: 'orgCodeShort', width: 120, ellipsis: true },
    { title: '机构全称', dataIndex: 'orgFullName', width: 160, ellipsis: true },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (_, row) => {
        const cfg = STATUS_MAP[row.status] ?? { label: row.status };
        return <Tag color={cfg.color}>{cfg.label}</Tag>;
      },
    },
    { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime', width: 180, search: false },
    {
      title: '操作',
      valueType: 'option',
      width: 220,
      fixed: 'right',
      render: (_, row) => [
        <a key="view" onClick={() => { setCurrentRow(row); setDetailModalOpen(true); }}>详情</a>,
        row.status === 'PENDING_PLANNER' && (
          <a
            key="planner"
            onClick={() => {
              setCurrentRow(row);
              plannerForm.setFieldsValue({
                platform: row.platform,
                customerId: row.customerId ?? undefined,
                customerName: row.customerName ?? '',
                orgFullName: row.orgFullName,
                contactPerson: row.contactPerson,
                phone: row.phone,
                email: row.email,
                salesPerson: row.salesPerson,
                contractNo: row.contractNo,
                price: row.price,
                quantity: row.quantity,
                reason: row.reason,
                ccPlanner: undefined,
              });
              setPlannerCustomerExists(null);
              setPlannerModalOpen(true);
            }}
          >
            规划处理
          </a>
        ),
        row.status === 'PENDING_ADMIN' && (
          <a key="admin" onClick={() => { setCurrentRow(row); adminForm.setFieldsValue({ orgCodeShort: row.orgCodeShort ?? '', region: row.region ?? undefined }); setAdminModalOpen(true); }}>
            管理员审核
          </a>
        ),
      ].filter(Boolean),
    },
  ];

  const handleCreate = async (values: Record<string, unknown>) => {
    const sourceType = (values.sourceType ?? createSourceType) as 'FACTORY' | 'SERVICE';
    try {
      await request.post('/v1/platform-account-applications/create', {
        platform: values.platform,
        sourceType,
        customerId: values.customerId ?? undefined,
        customerName: values.customerName ? String(values.customerName).trim() : undefined,
        orgFullName: values.orgFullName,
        contactPerson: values.contactPerson ?? undefined,
        phone: values.phone ?? undefined,
        email: values.email ?? undefined,
        contractNo: sourceType === 'FACTORY' ? (values.contractNo ?? undefined) : undefined,
        price: sourceType === 'SERVICE' ? values.price ?? undefined : undefined,
        quantity: sourceType === 'SERVICE' ? values.quantity ?? undefined : undefined,
        reason: sourceType === 'SERVICE' ? values.reason ?? undefined : undefined,
        isCcPlanner: false,
        skipPlanner: false,
      });
      message.success('申请已提交');
      setCreateOpen(false);
      actionRef.current?.reload();
      return true;
    } catch (e) {
      showError(e);
      return false;
    }
  };

  const handlePlannerSubmit = async () => {
    if (!currentRow) return;
    const values = await plannerForm.validateFields();
    try {
      await request.put(`/v1/platform-account-applications/${currentRow.id}/planner-submit`, {
        platform: values.platform ?? undefined,
        customerId: values.customerId ?? undefined,
        customerName: values.customerName ? String(values.customerName).trim() : undefined,
        orgFullName: values.orgFullName ?? undefined,
        contactPerson: values.contactPerson ?? undefined,
        phone: values.phone ?? undefined,
        email: values.email ?? undefined,
        salesPerson: values.salesPerson ?? undefined,
        contractNo: values.contractNo ?? undefined,
        price: values.price ?? undefined,
        quantity: values.quantity ?? undefined,
        reason: values.reason ?? undefined,
        isCcPlanner: !!values.ccPlanner,
      });
      message.success('已提交至管理员审核');
      setPlannerModalOpen(false);
      setCurrentRow(null);
      setPlannerCustomerExists(null);
      plannerForm.resetFields();
      actionRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  };

  const handleApprove = async () => {
    if (!currentRow) return;
    const values = await adminForm.validateFields();
    if (!values.orgCodeShort?.trim() || !values.region) {
      message.error('请填写机构代码/简称并选择区域');
      return;
    }
    try {
      await request.post(`/v1/platform-account-applications/${currentRow.id}/approve`, {
        platform: currentRow.platform,
        customerId: currentRow.customerId ?? undefined,
        orgCodeShort: values.orgCodeShort?.trim(),
        orgFullName: currentRow.orgFullName,
        region: values.region,
        contactPerson: currentRow.contactPerson ?? undefined,
        phone: currentRow.phone ?? undefined,
        email: currentRow.email ?? undefined,
        salesPerson: currentRow.salesPerson ?? undefined,
        contractNo: currentRow.contractNo ?? undefined,
        price: currentRow.price ?? undefined,
        quantity: currentRow.quantity ?? undefined,
        reason: currentRow.reason ?? undefined,
        isCcPlanner: currentRow.isCcPlanner ?? false,
        action: 'BIND_ONLY',
      });
      message.success('已通过');
      setAdminModalOpen(false);
      setCurrentRow(null);
      adminForm.resetFields();
      Modal.confirm({
        title: '平台开户已完成',
        content: '是否需要办理物联网卡（SIM）开卡业务？',
        okText: '去办理',
        cancelText: '暂不需要',
        onOk: () => navigate('/platform/sim-applications'),
        onCancel: () => actionRef.current?.reload(),
      });
    } catch (e) {
      showError(e);
    }
  };

  const handleRejectConfirm = async () => {
    if (!currentRow || !rejectReason.trim()) {
      message.error('请输入驳回原因');
      return;
    }
    try {
      await request.post(`/v1/platform-account-applications/${currentRow.id}/reject`, { reason: rejectReason.trim() });
      message.success('已驳回');
      setRejectModalOpen(false);
      setAdminModalOpen(false);
      setCurrentRow(null);
      setRejectReason('');
      actionRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <ProTable<ApplicationRow>
        actionRef={actionRef}
        columns={columns}
        request={async (params) => {
          try {
            const page = (params.current ?? 1) - 1;
            const size = params.pageSize ?? 20;
            const { data } = await request.get<{ content: ApplicationRow[]; totalElements: number }>(
              '/v1/platform-account-applications/page',
              { params: { page, size, status: params.status, platform: params.platform, customerId: params.customerId } }
            );
            const content = Array.isArray(data?.content) ? data.content : [];
            const total = data?.totalElements ?? 0;
            return { data: content, total, success: true };
          } catch {
            return { data: [], total: 0, success: true };
          }
        }}
        rowKey="id"
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 20 }}
        headerTitle="平台开户申请"
        toolBarRender={() => [
          <Button key="factory" type="primary" onClick={() => { setCreateSourceType('FACTORY'); setCreateOpen(true); }}>
            工厂申请
          </Button>,
          <Button key="service" onClick={() => { setCreateSourceType('SERVICE'); setCreateOpen(true); }}>
            服务申请
          </Button>,
        ]}
      />

      <ModalForm
        key={`create-${createSourceType}`}
        title={createSourceType === 'FACTORY' ? '工厂申请' : '服务申请'}
        open={createOpen}
        onOpenChange={(open) => { setCreateOpen(open); if (!open) setCreateSourceType('FACTORY'); }}
        onFinish={handleCreate}
        layout="vertical"
        modalProps={{ destroyOnClose: true }}
      >
        <Form.Item name="platform" label="平台" rules={[{ required: true }]}>
          <Select options={PLATFORM_OPTIONS} placeholder="选择平台" />
        </Form.Item>
        <Form.Item name="sourceType" hidden initialValue={createSourceType}><Input type="hidden" /></Form.Item>
        <CustomerFieldCreate />
        <Form.Item name="orgFullName" label="机构全称" rules={[{ required: true, message: '请输入机构全称' }]}>
          <Input placeholder="机构全称（默认同客户名称，可修改）" />
        </Form.Item>
        <Form.Item name="contactPerson" label="联系人" rules={[{ required: true, message: '此项必填' }]}>
          <Input placeholder="联系人" />
        </Form.Item>
        <Form.Item name="phone" label="联系电话" rules={[{ required: true, message: '此项必填' }]}>
          <Input placeholder="联系电话" />
        </Form.Item>
        <Form.Item name="email" label="邮箱" rules={[{ required: true, message: '此项必填' }]}>
          <Input placeholder="邮箱" />
        </Form.Item>
        {createSourceType === 'FACTORY' && (
          <Form.Item name="contractNo" label="合同号" rules={[{ required: true, message: '请输入合同号' }]}>
            <Input placeholder="合同号" />
          </Form.Item>
        )}
        {createSourceType === 'SERVICE' && (
          <>
            <Form.Item name="price" label="价格" rules={[{ required: true, message: '请填写价格' }]}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="quantity" label="数量" rules={[{ required: true, message: '请填写数量' }]}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="reason" label="原因" rules={[{ required: true, message: '请填写原因' }]}>
              <Input.TextArea rows={2} placeholder="原因" />
            </Form.Item>
          </>
        )}
      </ModalForm>

      <Modal
        title="规划处理"
        open={plannerModalOpen}
        onOk={handlePlannerSubmit}
        onCancel={() => { setPlannerModalOpen(false); setCurrentRow(null); setPlannerCustomerExists(null); plannerForm.resetFields(); }}
        okText="提交至管理员"
        destroyOnClose
        width={560}
      >
        {currentRow && (
          <div style={{ marginBottom: 16, padding: 12, background: '#fafafa', borderRadius: 8 }}>
            <p><strong>申请单号</strong>: {currentRow.applicationNo}</p>
          </div>
        )}
        {plannerCustomerExists === true && (
          <Alert type="warning" message="该客户后台已存在" style={{ marginBottom: 16 }} showIcon />
        )}
        <Form form={plannerForm} layout="vertical">
          <Form.Item name="platform" label="平台">
            <Select options={PLATFORM_OPTIONS} placeholder="平台" />
          </Form.Item>
          <Form.Item name="customerId" hidden><Input type="hidden" /></Form.Item>
          <Form.Item name="customerName" label="客户名称">
            <Input
              placeholder="客户名称（可修改，失焦时检查是否已存在）"
              onChange={(e) => {
                plannerForm.setFieldValue('customerId', null);
                setPlannerCustomerExists(null);
              }}
              onBlur={async () => {
                const name = plannerForm.getFieldValue('customerName')?.trim();
                if (!name) { setPlannerCustomerExists(null); return; }
                try {
                  const { data } = await request.get<{ exists?: boolean }>('/v1/platform-account-applications/check-customer-name', { params: { name } });
                  setPlannerCustomerExists(data?.exists ?? false);
                } catch {
                  setPlannerCustomerExists(null);
                }
              }}
            />
          </Form.Item>
          <Form.Item name="orgFullName" label="机构全称" rules={[{ required: true, message: '必填' }]}>
            <Input placeholder="机构全称" />
          </Form.Item>
          <Form.Item name="contactPerson" label="联系人">
            <Input placeholder="联系人" />
          </Form.Item>
          <Form.Item name="phone" label="联系电话">
            <Input placeholder="联系电话" />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input placeholder="邮箱" />
          </Form.Item>
          <Form.Item name="salesPerson" label="销售负责人">
            <Input placeholder="销售负责人" />
          </Form.Item>
          {currentRow?.sourceType === 'SERVICE' ? (
            <>
              <Form.Item name="price" label="价格">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item name="quantity" label="数量">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item name="reason" label="原因">
                <Input.TextArea rows={2} placeholder="原因" />
              </Form.Item>
            </>
          ) : (
            <>
              <Form.Item name="contractNo" label="合同号">
                <Input placeholder="合同号" />
              </Form.Item>
              {currentRow?.sourceType === 'FACTORY' && (
                <Form.Item name="ccPlanner" label="抄送规划">
                  <Select
                    placeholder="选填"
                    allowClear
                    options={[
                      { label: '张三', value: '张三' },
                      { label: '李四', value: '李四' },
                      { label: '王五', value: '王五' },
                    ]}
                  />
                </Form.Item>
              )}
            </>
          )}
        </Form>
      </Modal>

      <Modal
        title="管理员审核"
        open={adminModalOpen}
        onCancel={() => { setAdminModalOpen(false); setCurrentRow(null); adminForm.resetFields(); }}
        footer={null}
        width={640}
        destroyOnClose
      >
        {currentRow && (
          <>
            <div style={{ marginBottom: 16, padding: 12, background: '#fafafa', borderRadius: 8, lineHeight: 1.8 }}>
              <p><strong>申请单号</strong>: {currentRow.applicationNo}</p>
              <p><strong>平台</strong>: {PLATFORM_OPTIONS.find((o) => o.value === currentRow.platform)?.label ?? currentRow.platform}</p>
              <p><strong>渠道</strong>: {currentRow.sourceType === 'SERVICE' ? '服务渠道' : '工厂渠道'}</p>
              <p><strong>客户</strong>: {currentRow.customerName ?? (currentRow.customerId != null ? `#${currentRow.customerId}` : '—')}</p>
              <p><strong>机构全称</strong>: {currentRow.orgFullName ?? '—'}</p>
              <p><strong>联系人</strong>: {currentRow.contactPerson ?? '—'}</p>
              <p><strong>联系电话</strong>: {currentRow.phone ?? '—'}</p>
              <p><strong>邮箱</strong>: {currentRow.email ?? '—'}</p>
              {currentRow.contractNo != null && <p><strong>合同号</strong>: {currentRow.contractNo}</p>}
              {currentRow.price != null && <p><strong>价格</strong>: {currentRow.price}</p>}
              {currentRow.quantity != null && <p><strong>数量</strong>: {currentRow.quantity}</p>}
              {currentRow.reason != null && <p><strong>原因</strong>: {currentRow.reason}</p>}
            </div>
            <Form form={adminForm} layout="vertical" initialValues={{ orgCodeShort: currentRow.orgCodeShort, region: currentRow.region }}>
              <Form.Item name="orgCodeShort" label="机构代码/简称" rules={[{ required: true, message: '请填写机构代码/简称' }]}>
                <Input placeholder="机构代码/简称" />
              </Form.Item>
              <Form.Item name="region" label="区域" rules={[{ required: true, message: '请选择区域' }]}>
                <Select options={REGION_OPTIONS} placeholder="华东/华北/华南/西部/海外" allowClear />
              </Form.Item>
              <Form.Item>
                <Button type="primary" onClick={handleApprove} style={{ marginRight: 8 }}>通过</Button>
                <Button danger onClick={() => setRejectModalOpen(true)}>驳回</Button>
              </Form.Item>
            </Form>
          </>
        )}
      </Modal>

      <Modal
        title="驳回原因"
        open={rejectModalOpen}
        onOk={handleRejectConfirm}
        onCancel={() => { setRejectModalOpen(false); setRejectReason(''); }}
        okText="确认驳回"
      >
        <Input.TextArea rows={4} value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} placeholder="请输入驳回原因" style={{ marginTop: 16 }} />
      </Modal>

      <Modal
        title="详情"
        open={detailModalOpen}
        onCancel={() => { setDetailModalOpen(false); setCurrentRow(null); }}
        footer={null}
        width={560}
      >
        {currentRow && (
          <div style={{ lineHeight: 2 }}>
            <p><strong>申请单号</strong>: {currentRow.applicationNo}</p>
            <p><strong>状态</strong>: {STATUS_MAP[currentRow.status]?.label ?? currentRow.status}</p>
            <p><strong>平台</strong>: {PLATFORM_OPTIONS.find((o) => o.value === currentRow.platform)?.label ?? currentRow.platform}</p>
            <p><strong>渠道</strong>: {currentRow.sourceType === 'SERVICE' ? '服务渠道' : '工厂渠道'}</p>
            <p><strong>客户</strong>: {currentRow.customerName ?? (currentRow.customerId != null ? `#${currentRow.customerId}` : '—')}</p>
            <p><strong>机构代码/简称</strong>: {currentRow.orgCodeShort}</p>
            <p><strong>机构全称</strong>: {currentRow.orgFullName}</p>
            <p><strong>联系人</strong>: {currentRow.contactPerson ?? '-'}</p>
            <p><strong>电话</strong>: {currentRow.phone ?? '-'}</p>
            <p><strong>邮箱</strong>: {currentRow.email ?? '-'}</p>
            <p><strong>合同号</strong>: {currentRow.contractNo ?? '-'}</p>
            <p><strong>价格</strong>: {currentRow.price ?? '-'}</p>
            <p><strong>数量</strong>: {currentRow.quantity ?? '-'}</p>
            <p><strong>原因</strong>: {currentRow.reason ?? '-'}</p>
            <p><strong>抄送规划</strong>: {currentRow.isCcPlanner ? '是' : '否'}</p>
            {currentRow.rejectReason && <p><strong>驳回原因</strong>: {currentRow.rejectReason}</p>}
            <p><strong>创建时间</strong>: {currentRow.createdAt}</p>
          </div>
        )}
      </Modal>
    </div>
  );
}
