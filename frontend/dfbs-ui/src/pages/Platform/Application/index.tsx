import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ProTable, ModalForm, ProFormMoney, ProFormDigit } from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Alert, Button, Card, Col, Modal, Form, Input, InputNumber, Row, Select, Spin, Tag, message } from 'antd';
import request from '@/shared/utils/request';
import SmartReferenceSelect from '@/shared/components/SmartReferenceSelect';
import SmartInput from '@/shared/components/SmartInput';
import DuplicateCheckModal from '@/features/platform/components/DuplicateCheckModal';
import HitAnalysisPanel from '@/features/platform/components/HitAnalysisPanel';
import type { DuplicateMatchItem } from '@/features/platform/components/HitAnalysisPanel';
import { PhoneRule, EmailRule } from '@/shared/utils/validators/common';
import { ContractRule, OrgCodeRule, OrgCodeUppercaseRule } from '@/features/platform/utils/validators';
import { getPlatformConfigs, type PlatformConfigItem } from '@/features/platform/services/platformConfig';

const SOURCE_TYPE_OPTIONS = [
  { label: '销售渠道', value: 'FACTORY' },
  { label: '服务渠道', value: 'SERVICE' },
];

const SALES_PERSON_OPTIONS = [
  { label: '张三', value: '张三' },
  { label: '李四', value: '李四' },
  { label: '王五', value: '王五' },
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
  PENDING: { label: '待申请人', color: 'default' },
  PENDING_PLANNER: { label: '待确认', color: 'processing' },
  PENDING_CONFIRM: { label: '待营企确认', color: 'processing' },
  PENDING_ADMIN: { label: '待审核', color: 'warning' },
  APPROVED: { label: '已通过', color: 'success' },
  REJECTED: { label: '已驳回', color: 'error' },
  CLOSED: { label: '已关闭', color: 'default' },
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
  applicantId?: number | null;
  applicantName?: string | null;
}

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  message.error(err?.response?.data?.message ?? err?.message ?? '操作失败');
}

/** Robust match for backend reason strings (e.g. "客户名称已存在" or "Contact Email already used"). */
function isHitReason(reason: string | undefined, kind: 'customer' | 'phone' | 'email' | 'orgCode'): boolean {
  if (!reason) return false;
  const r = reason;
  switch (kind) {
    case 'customer': return r.includes('客户') || r.includes('Customer');
    case 'phone': return r.includes('电话') || r.includes('Phone');
    case 'email': return r.includes('邮箱') || r.includes('Email');
    case 'orgCode': return r.includes('代码') || r.includes('编码') || r.includes('Code');
    default: return false;
  }
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

/** Planner modal: customer select with orgFullName auto-fill (must be inside Form with form instance). */
function PlannerCustomerSelect({ setPlannerCustomerExists }: { setPlannerCustomerExists: (v: boolean | null) => void }) {
  const form = Form.useFormInstance();
  const customerName = Form.useWatch('customerName', form);
  return (
    <SmartReferenceSelect
      entityType="CUSTOMER"
      value={customerName ?? ''}
      placeholder="输入客户名称或从主数据选择"
      onChange={(ref) => {
        form.setFieldValue('customerId', ref.id ?? null);
        form.setFieldValue('customerName', ref.name);
        form.setFieldValue('orgFullName', ref.name ?? '');
        setPlannerCustomerExists(ref.id != null ? true : null);
      }}
    />
  );
}

/** Planner modal: contract select (must be inside Form). */
function PlannerContractSelect() {
  const form = Form.useFormInstance();
  const contractNo = Form.useWatch('contractNo', form);
  return (
    <SmartReferenceSelect
      entityType="CONTRACT"
      value={contractNo ?? ''}
      placeholder="输入合同号或从主数据选择"
      onChange={(ref) => form.setFieldValue('contractNo', ref.name ?? '')}
    />
  );
}

export default function PlatformApplication() {
  const navigate = useNavigate();
  const actionRef = useRef<ActionType>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [plannerModalOpen, setPlannerModalOpen] = useState(false);
  const [adminModalOpen, setAdminModalOpen] = useState(false);
  const [adminHitMatches, setAdminHitMatches] = useState<DuplicateMatchItem[]>([]);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [rejectModalOpen, setRejectModalOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [currentRow, setCurrentRow] = useState<ApplicationRow | null>(null);
  const [createSourceType, setCreateSourceType] = useState<'FACTORY' | 'SERVICE'>('FACTORY');
  const [plannerCustomerExists, setPlannerCustomerExists] = useState<boolean | null>(null);
  const [duplicateMatches, setDuplicateMatches] = useState<DuplicateMatchItem[]>([]);
  const [duplicateCurrentInput, setDuplicateCurrentInput] = useState<{ customerName: string; email: string; phone: string } | null>(null);
  const [showDuplicateModal, setShowDuplicateModal] = useState(false);
  /** When duplicate modal is open, platform/label for HitAnalysisPanel summary (set in both Create and Planner flows). */
  const [duplicateModalPlatform, setDuplicateModalPlatform] = useState('');
  const [duplicateModalPlatformLabel, setDuplicateModalPlatformLabel] = useState('');
  /** When non-null, duplicate modal was opened from Create flow; "确认新增" will call create with this. */
  const [pendingCreateValues, setPendingCreateValues] = useState<Record<string, unknown> | null>(null);
  const [plannerForm] = Form.useForm<ApplicationRow & { customerName?: string }>();
  const [adminForm] = Form.useForm<ApplicationRow>();

  const [platformConfigs, setPlatformConfigs] = useState<PlatformConfigItem[]>([]);
  useEffect(() => {
    getPlatformConfigs().then(setPlatformConfigs).catch(() => setPlatformConfigs([]));
  }, []);
  const platformOptions = useMemo(
    () => (platformConfigs?.filter((p) => p.isActive) ?? []).map((p) => ({ label: p.platformName, value: p.platformCode })),
    [platformConfigs]
  );
  const getConfig = (code: string) => platformConfigs?.find((p) => p.platformCode === code);
  const getPlatformLabel = (code: string) => getConfig(code)?.platformName ?? code;

  const columns: ProColumns<ApplicationRow>[] = [
    { title: '申请单号', dataIndex: 'applicationNo', width: 160, ellipsis: true },
    { title: '平台', dataIndex: 'platform', width: 100, render: (_, row) => getPlatformLabel(row.platform ?? '') },
    { title: '渠道', dataIndex: 'sourceType', width: 80, search: false, valueEnum: { FACTORY: '销售', SERVICE: '服务' } },
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
        (row.status === 'PENDING_PLANNER' || row.status === 'PENDING_CONFIRM' || row.status === 'CLOSED') && (
          <a
            key="planner"
            onClick={() => {
              setCurrentRow(row);
              const applicantName = (row as ApplicationRow).applicantName;
              plannerForm.setFieldsValue({
                platform: row.platform,
                customerId: row.customerId ?? undefined,
                customerName: row.customerName ?? '',
                orgFullName: row.orgFullName,
                contactPerson: row.contactPerson,
                phone: row.phone,
                email: row.email,
                salesPerson: applicantName ?? row.salesPerson ?? undefined,
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
            营企处理
          </a>
        ),
        row.status === 'PENDING_ADMIN' && (
          <a key="admin" onClick={() => { setCurrentRow(row); setAdminHitMatches([]); adminForm.setFieldsValue({ orgCodeShort: row.orgCodeShort ?? '', region: row.region ?? undefined }); setAdminModalOpen(true); }}>
            管理员审核
          </a>
        ),
      ].filter(Boolean),
    },
  ];

  /** Perform the create API call (used when no duplicates, or when user confirms add from duplicate modal). */
  const doCreate = async (values: Record<string, unknown>) => {
    const sourceType = (values.sourceType ?? createSourceType) as 'FACTORY' | 'SERVICE';
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
  };

  const handleCreate = async (values: Record<string, unknown>) => {
    try {
      const { data: matches } = await request.post<DuplicateMatchItem[]>(
        '/v1/platform-account-applications/check-duplicates',
        {
          platform: values.platform,
          customerName: values.customerName ? String(values.customerName).trim() : '',
          email: values.email ?? '',
          contactPhone: values.phone ?? '',
          orgFullName: values.orgFullName ? String(values.orgFullName).trim() : '',
        }
      );
      if (matches?.length) {
        setPendingCreateValues(values);
        setDuplicateMatches(matches);
        setDuplicateCurrentInput({
          customerName: values.customerName ? String(values.customerName).trim() : '',
          email: (values.email ?? '') as string,
          phone: (values.phone ?? '') as string,
        });
        setDuplicateModalPlatform(String(values.platform ?? ''));
        setDuplicateModalPlatformLabel(getPlatformLabel(String(values.platform ?? '')));
        setShowDuplicateModal(true);
        return false;
      }
      await doCreate(values);
      message.success('申请已提交');
      setCreateOpen(false);
      actionRef.current?.reload();
      return true;
    } catch (e) {
      showError(e);
      return false;
    }
  };

  const doPlannerSubmit = async () => {
    if (!currentRow) return;
    const values = await plannerForm.validateFields();
    try {
      await request.put(`/v1/platform-account-applications/${currentRow.id}/planner-submit`, {
        platform: values.platform ?? undefined,
        customerId: values.customerId ?? undefined,
        customerName: values.customerName ? String(values.customerName).trim() : undefined,
        orgCodeShort: values.orgCodeShort ?? undefined,
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
      setShowDuplicateModal(false);
      setDuplicateMatches([]);
      setDuplicateCurrentInput(null);
      plannerForm.resetFields();
      actionRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  };

  const handlePlannerConfirm = async () => {
    if (!currentRow) return;
    try {
      const values = await plannerForm.validateFields();
      const { data: matches } = await request.post<DuplicateMatchItem[]>(
        '/v1/platform-account-applications/check-duplicates',
        {
          platform: values.platform,
          customerName: values.customerName ? String(values.customerName).trim() : '',
          email: values.email ?? '',
          contactPhone: values.phone ?? '',
          orgFullName: values.orgFullName ? String(values.orgFullName).trim() : '',
        }
      );
      if (!matches?.length) {
        await doPlannerSubmit();
        return;
      }
      setPendingCreateValues(null);
      setDuplicateMatches(matches);
      setDuplicateCurrentInput({
        customerName: values.customerName ? String(values.customerName).trim() : '',
        email: values.email ? String(values.email).trim() : '',
        phone: values.phone ? String(values.phone).trim() : '',
      });
      setDuplicateModalPlatform(String(values.platform ?? ''));
      setDuplicateModalPlatformLabel(getPlatformLabel(String(values.platform ?? '')));
      setShowDuplicateModal(true);
    } catch (e) {
      if ((e as { errorFields?: unknown[] })?.errorFields) return;
      showError(e);
    }
  };

  const handleDuplicateReturn = () => {
    setShowDuplicateModal(false);
    setDuplicateMatches([]);
    setDuplicateCurrentInput(null);
  };

  const handleDuplicateConfirmNew = async () => {
    await doPlannerSubmit();
  };

  /** "确认新增": from Create flow call doCreate and close both modals; from Planner flow call doPlannerSubmit. */
  const handleDuplicateConfirmAdd = async () => {
    if (pendingCreateValues) {
      try {
        await doCreate(pendingCreateValues);
        message.success('申请已提交');
        setPendingCreateValues(null);
        setShowDuplicateModal(false);
        setDuplicateMatches([]);
        setDuplicateCurrentInput(null);
        setCreateOpen(false);
        actionRef.current?.reload();
      } catch (e) {
        showError(e);
      }
    } else {
      await handleDuplicateConfirmNew();
    }
  };

  const handleDuplicateGoSim = () => {
    setShowDuplicateModal(false);
    setDuplicateMatches([]);
    setDuplicateCurrentInput(null);
    setPlannerModalOpen(false);
    setCurrentRow(null);
    plannerForm.resetFields();
    navigate('/platform/sim-applications');
    actionRef.current?.reload();
  };

  /** Close duplicate modal and parent modal(s) — full exit. */
  const handleDuplicateCancel = () => {
    setShowDuplicateModal(false);
    setDuplicateMatches([]);
    setDuplicateCurrentInput(null);
    setPendingCreateValues(null);
    setPlannerModalOpen(false);
    setCurrentRow(null);
    setCreateOpen(false);
    plannerForm.resetFields();
  };

  const handlePlannerCloseApp = () => {
    if (!currentRow) return;
    Modal.confirm({
      title: '确认关闭申请？',
      content: '关闭后流程结束，营企可重新打开并提交。确认关闭吗？',
      okText: '确认关闭',
      cancelText: '取消',
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          await request.post(`/v1/platform-account-applications/${currentRow.id}/close`);
          message.success('申请已关闭');
          setPlannerModalOpen(false);
          setCurrentRow(null);
          setPlannerCustomerExists(null);
          plannerForm.resetFields();
          actionRef.current?.reload();
        } catch (e) {
          showError(e);
        }
      },
    });
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
            销售申请
          </Button>,
          <Button key="service" onClick={() => { setCreateSourceType('SERVICE'); setCreateOpen(true); }}>
            服务申请
          </Button>,
        ]}
      />

      <ModalForm
        key={`create-${createSourceType}`}
        title={createSourceType === 'FACTORY' ? '销售申请' : '服务申请'}
        open={createOpen}
        onOpenChange={(open) => { setCreateOpen(open); if (!open) setCreateSourceType('FACTORY'); }}
        onFinish={handleCreate}
        layout="vertical"
        modalProps={{ destroyOnClose: true }}
      >
        <Form.Item name="platform" label="平台" rules={[{ required: true }]}>
          <Select options={platformOptions} placeholder="选择平台" />
        </Form.Item>
        <Form.Item name="sourceType" hidden initialValue={createSourceType}><Input type="hidden" /></Form.Item>
        <CustomerFieldCreate />
        <Form.Item name="orgFullName" label="机构全称" rules={[{ required: true, message: '请输入机构全称' }]}>
          <Input placeholder="机构全称（默认同客户名称，可修改）" />
        </Form.Item>
        <Form.Item name="contactPerson" label="联系人" rules={[{ required: true, message: '此项必填' }]}>
          <SmartInput name="contactPerson" trim placeholder="联系人" />
        </Form.Item>
        <Form.Item name="phone" label="联系电话" rules={[{ required: true, message: '此项必填' }, PhoneRule]}>
          <SmartInput name="phone" noSpaces placeholder="11位手机号或区号-号码" />
        </Form.Item>
        <Form.Item name="email" label="邮箱" rules={[{ required: true, message: '此项必填' }, EmailRule]}>
          <SmartInput name="email" type="email" noSpaces placeholder="邮箱" />
        </Form.Item>
        {createSourceType === 'FACTORY' && (
          <Form.Item name="contractNo" label="合同号" rules={[{ required: true, message: '请输入合同号' }, ContractRule]}>
            <SmartInput name="contractNo" uppercase noSpaces placeholder="合同号（英文/数字/符号）" />
          </Form.Item>
        )}
        {createSourceType === 'SERVICE' && (
          <>
            <Alert
              type="warning"
              message="首次开通必须交满1年（12个月），特殊情况请单独联系管理员"
              showIcon
              style={{ marginBottom: 16 }}
            />
            <ProFormMoney
              name="price"
              label="单价（元/点）"
              initialValue={25}
              rules={[{ required: true, message: '请填写单价' }]}
              fieldProps={{
                min: 0,
                precision: 2,
                step: 0.01,
                moneySymbol: false,
                addonAfter: '元/点',
                formatter: (value) => (value != null && value !== '' ? Number(value).toFixed(2) : ''),
                parser: (v) => (v ? parseFloat(String(v).replace(/,/g, '')) : undefined),
              }}
            />
            <ProFormDigit
              name="quantity"
              label="台数"
              initialValue={1}
              rules={[{ required: true, message: '请填写台数' }]}
              fieldProps={{ min: 1, precision: 0, addonAfter: '台' }}
            />
            <Form.Item name="reason" label="原因" rules={[{ required: true, message: '请填写原因' }]}>
              <Input.TextArea rows={2} placeholder="原因" />
            </Form.Item>
          </>
        )}
      </ModalForm>

      <Modal
        title="营企确认"
        open={plannerModalOpen}
        onOk={handlePlannerConfirm}
        onCancel={() => { setPlannerModalOpen(false); setCurrentRow(null); setPlannerCustomerExists(null); plannerForm.resetFields(); }}
        destroyOnClose
        width={560}
        footer={[
          <Button key="closeApp" danger onClick={handlePlannerCloseApp}>
            关闭申请
          </Button>,
          <Button key="cancel" onClick={() => { setPlannerModalOpen(false); setCurrentRow(null); setPlannerCustomerExists(null); plannerForm.resetFields(); }}>
            取消
          </Button>,
          <Button key="submit" type="primary" onClick={() => handlePlannerConfirm()}>
            提交至管理员
          </Button>,
        ]}
      >
        {currentRow && (
          <div style={{ marginBottom: 16, padding: 12, background: '#fafafa', borderRadius: 8 }}>
            <p><strong>申请单号</strong>: {currentRow.applicationNo}</p>
          </div>
        )}
        {currentRow?.rejectReason && (
          <Alert type="error" showIcon message={`被驳回，理由：${currentRow.rejectReason}`} style={{ marginBottom: 16 }} />
        )}
        {plannerCustomerExists === true && (
          <Alert type="warning" message="该客户后台已存在" style={{ marginBottom: 16 }} showIcon />
        )}
        {currentRow?.sourceType === 'SERVICE' && (
          <Alert
            type="warning"
            message="首次开通必须交满1年（12个月），特殊情况请单独联系管理员"
            showIcon
            style={{ marginBottom: 16 }}
          />
        )}
        <Form form={plannerForm} layout="vertical">
          <Form.Item name="platform" label="平台" rules={[{ required: true, message: '此项必填' }]}>
            <Select options={platformOptions} placeholder="请选择平台" />
          </Form.Item>
          <Form.Item name="customerId" hidden><Input type="hidden" /></Form.Item>
          <Form.Item name="customerName" label="客户名称" rules={[{ required: true, message: '此项必填' }]}>
            <PlannerCustomerSelect setPlannerCustomerExists={setPlannerCustomerExists} />
          </Form.Item>
          <Form.Item name="orgFullName" label="机构全称" rules={[{ required: true, message: '此项必填' }]}>
            <Input placeholder="机构全称（选客户后自动带出，可修改）" />
          </Form.Item>
          <Form.Item name="contactPerson" label="联系人" rules={[{ required: true, message: '此项必填' }]}>
            <Input placeholder="联系人" />
          </Form.Item>
          <Form.Item name="phone" label="联系电话" rules={[{ required: true, message: '此项必填' }, PhoneRule]}>
            <SmartInput name="phone" noSpaces placeholder="11位手机号或区号-号码" />
          </Form.Item>
          <Form.Item name="email" label="邮箱" rules={[{ required: true, message: '此项必填' }, EmailRule]}>
            <SmartInput name="email" type="email" noSpaces placeholder="邮箱" />
          </Form.Item>
          <Form.Item name="salesPerson" label="销售负责人" rules={[{ required: true, message: '此项必填' }]}>
            <Select
              options={[
                ...SALES_PERSON_OPTIONS,
                ...(currentRow?.applicantName && !SALES_PERSON_OPTIONS.some((o) => o.value === currentRow.applicantName)
                  ? [{ label: currentRow.applicantName, value: currentRow.applicantName }]
                  : []),
              ]}
              placeholder="请选择销售负责人"
              allowClear={false}
            />
          </Form.Item>
          {currentRow?.sourceType === 'SERVICE' ? (
            <>
              <Form.Item name="price" label="单价（元/点）" initialValue={25} rules={[{ required: true, message: '此项必填' }]}>
                <InputNumber
                  min={0}
                  precision={2}
                  step={0.01}
                  style={{ width: '100%' }}
                  addonAfter="元/点"
                  placeholder="25.00"
                  formatter={(value) => (value != null && value !== '' ? Number(value).toFixed(2) : '')}
                  parser={(v) => (v ? parseFloat(String(v).replace(/,/g, '')) : undefined)}
                />
              </Form.Item>
              <Form.Item name="quantity" label="台数" initialValue={1} rules={[{ required: true, message: '此项必填' }]}>
                <InputNumber min={1} precision={0} style={{ width: '100%' }} addonAfter="台" placeholder="1" />
              </Form.Item>
              <Form.Item name="reason" label="原因" rules={[{ required: true, message: '此项必填' }]}>
                <Input.TextArea rows={2} placeholder="原因" />
              </Form.Item>
            </>
          ) : (
            <>
              <Form.Item name="contractNo" label="合同号" rules={[{ required: true, message: '此项必填' }, ContractRule]}>
                <PlannerContractSelect />
              </Form.Item>
              {currentRow?.sourceType === 'FACTORY' && (
                <Form.Item name="ccPlanner" label="抄送规划" rules={[{ required: true, message: '此项必填' }]}>
                  <Select
                    placeholder="请选择"
                    allowClear={false}
                    options={SALES_PERSON_OPTIONS}
                  />
                </Form.Item>
              )}
            </>
          )}
        </Form>
      </Modal>

      <DuplicateCheckModal
        visible={showDuplicateModal}
        matches={duplicateMatches}
        platform={duplicateModalPlatform}
        platformLabel={duplicateModalPlatformLabel}
        currentInput={duplicateCurrentInput ?? undefined}
        title="重复提醒"
        onCancel={handleDuplicateReturn}
        renderFooter={(close) => {
          const isServiceFlow = pendingCreateValues?.sourceType === 'SERVICE';
          const config = getConfig((pendingCreateValues?.platform as string) ?? '');
          const reason = (h: DuplicateMatchItem) => h.matchReason ?? '';
          const violationEmail = !!config?.ruleUniqueEmail && duplicateMatches.some((h) => reason(h).includes('邮箱'));
          const violationPhone = !!config?.ruleUniquePhone && duplicateMatches.some((h) => reason(h).includes('电话'));
          const violationName = !!config?.ruleUniqueOrgName && duplicateMatches.some((h) => reason(h).includes('全称'));
          const isBlocked = violationEmail || violationPhone || violationName;
          if (isServiceFlow) {
            return [
              <Button key="return" onClick={handleDuplicateReturn}>
                返回编辑
              </Button>,
              <Button
                key="requestCheck"
                type="primary"
                onClick={() => message.info('功能暂留：后续合并开发')}
              >
                申请核查
              </Button>,
              <Button
                key="payReuse"
                type="primary"
                onClick={() => message.info('功能暂留：后续合并开发')}
              >
                缴费复用
              </Button>,
              <Button key="cancel" onClick={handleDuplicateCancel}>
                取消
              </Button>,
            ];
          }
          return (
            <div>
              {isBlocked && (
                <p style={{ color: '#ff4d4f', marginBottom: 12 }}>
                  根据平台规则，上述字段严禁重复，无法强制提交。
                </p>
              )}
              <span>
                <Button key="return" onClick={handleDuplicateReturn}>
                  返回编辑
                </Button>
                {!isBlocked && (
                  <Button key="confirm" type="primary" onClick={handleDuplicateConfirmAdd} style={{ marginLeft: 8 }}>
                    确认新增
                  </Button>
                )}
                <Button key="cancel" onClick={handleDuplicateCancel} style={{ marginLeft: 8 }}>
                  取消
                </Button>
              </span>
            </div>
          );
        }}
      />

      <Modal
        title="管理员审核"
        open={adminModalOpen}
        onCancel={() => { setAdminModalOpen(false); setCurrentRow(null); setAdminHitMatches([]); adminForm.resetFields(); }}
        footer={null}
        width={900}
        destroyOnClose
      >
        {currentRow && (
          <Row gutter={16} wrap={false}>
            <Col span={14} style={{ borderRight: '1px solid #f0f0f0', paddingRight: 16 }}>
              {(() => {
                if (adminHitMatches.length > 0) {
                  console.log('Admin Hits:', adminHitMatches);
                }
                const reason = (h: DuplicateMatchItem) => h.matchReason || '';
                const anyCustomer = adminHitMatches.some((h) => reason(h).includes('客户') || reason(h).includes('Customer'));
                const anyPhone = adminHitMatches.some((h) => reason(h).includes('电话') || reason(h).includes('Phone'));
                const anyEmail = adminHitMatches.some((h) => reason(h).includes('邮箱') || reason(h).includes('Email'));
                const anyOrgCode = adminHitMatches.some((h) => reason(h).includes('代码') || reason(h).includes('Code') || reason(h).includes('编码'));
                const redStyle = { color: '#ff4d4f', fontWeight: 'bold' as const };
                const customerVal = currentRow.customerName ?? (currentRow.customerId != null ? `#${currentRow.customerId}` : '—');
                const phoneVal = currentRow.phone ?? '—';
                const emailVal = currentRow.email ?? '—';
                const orgCodeVal = currentRow.orgCodeShort ?? '—';
                const wrapIfHit = (hit: boolean, text: string) =>
                  hit ? <span style={redStyle}>{text}</span> : text;
                return (
                  <div style={{ marginBottom: 16, padding: 12, background: '#fafafa', borderRadius: 8, lineHeight: 1.8 }}>
                    <p><strong>申请单号</strong>: {currentRow.applicationNo}</p>
                    <p><strong>平台</strong>: {getPlatformLabel(currentRow.platform ?? '')}</p>
                    <p><strong>渠道</strong>: {currentRow.sourceType === 'SERVICE' ? '服务渠道' : '销售渠道'}</p>
                    <p><strong>客户</strong>: {wrapIfHit(anyCustomer, customerVal)}</p>
                    <p><strong>机构代码/简称</strong>: {wrapIfHit(anyOrgCode, orgCodeVal)}</p>
                    <p><strong>机构全称</strong>: {currentRow.orgFullName ?? '—'}</p>
                    <p><strong>联系人</strong>: {currentRow.contactPerson ?? '—'}</p>
                    <p><strong>联系电话</strong>: {wrapIfHit(anyPhone, phoneVal)}</p>
                    <p><strong>邮箱</strong>: {wrapIfHit(anyEmail, emailVal)}</p>
                    {currentRow.contractNo != null && <p><strong>合同号</strong>: {currentRow.contractNo}</p>}
                    {currentRow.price != null && <p><strong>价格</strong>: {currentRow.price}</p>}
                    {currentRow.quantity != null && <p><strong>数量</strong>: {currentRow.quantity}</p>}
                    {currentRow.reason != null && <p><strong>原因</strong>: {currentRow.reason}</p>}
                  </div>
                );
              })()}
              <Form form={adminForm} layout="vertical" initialValues={{ orgCodeShort: currentRow.orgCodeShort, region: currentRow.region }}>
                {(() => {
                  const config = getConfig(currentRow.platform ?? '');
                  const validatorType = config?.codeValidatorType ?? 'NONE';
                  const isUppercase = validatorType === 'UPPERCASE';
                  const isMixedOrChinese = validatorType === 'MIXED' || validatorType === 'CHINESE';
                  const label = isUppercase ? '机构代码 (仅限大写)' : isMixedOrChinese ? '机构代码/简称 (建议汉字)' : '机构代码/简称';
                  const placeholder = isUppercase ? '机构代码 (仅限大写字母)' : isMixedOrChinese ? '机构简称 (建议汉字)' : '机构代码/简称（大写字母+汉字）';
                  const rules = isUppercase
                    ? [{ required: true, message: '请填写机构代码/简称' }, OrgCodeUppercaseRule]
                    : isMixedOrChinese
                      ? [{ required: true, message: '请填写机构简称' }]
                      : [{ required: true, message: '请填写机构代码/简称' }, OrgCodeRule];
                  return (
                    <Form.Item name="orgCodeShort" label={label} rules={rules}>
                      <SmartInput
                        name="orgCodeShort"
                        uppercase={isUppercase}
                        onlyLetters={isUppercase}
                        trim={!isUppercase && !isMixedOrChinese}
                        noSpaces
                        placeholder={placeholder}
                      />
                    </Form.Item>
                  );
                })()}
                <Form.Item name="region" label="区域" rules={[{ required: true, message: '请选择区域' }]}>
                  <Select options={REGION_OPTIONS} placeholder="华东/华北/华南/西部/海外" allowClear />
                </Form.Item>
                <Form.Item>
                  <Button type="primary" onClick={handleApprove} style={{ marginRight: 8 }}>通过</Button>
                  <Button danger onClick={() => setRejectModalOpen(true)}>驳回</Button>
                </Form.Item>
              </Form>
            </Col>
            <Col span={10}>
              <HitAnalysisPanel
                platform={currentRow.platform ?? ''}
                platformLabel={getPlatformLabel(currentRow.platform ?? '')}
                customerName={currentRow.customerName ?? ''}
                email={currentRow.email ?? ''}
                contactPhone={currentRow.phone ?? ''}
                orgFullName={currentRow.orgFullName ?? ''}
                onHitsLoaded={setAdminHitMatches}
              />
            </Col>
          </Row>
        )}
      </Modal>

      <Modal
        title="确认驳回"
        open={rejectModalOpen}
        onOk={handleRejectConfirm}
        onCancel={() => { setRejectModalOpen(false); setRejectReason(''); }}
        okText="确认驳回"
        destroyOnClose
      >
        <Form layout="vertical">
          <Form.Item label="驳回理由" required>
            <Input.TextArea
              rows={4}
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="请输入驳回理由（必填）"
            />
          </Form.Item>
        </Form>
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
            <p><strong>平台</strong>: {getPlatformLabel(currentRow.platform ?? '')}</p>
            <p><strong>渠道</strong>: {currentRow.sourceType === 'SERVICE' ? '服务渠道' : '销售渠道'}</p>
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
