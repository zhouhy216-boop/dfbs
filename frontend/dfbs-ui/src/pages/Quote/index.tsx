import { useRef, useState, useCallback, useEffect } from 'react';
import {
  ProTable,
  ModalForm,
  ProFormSelect,
  ProFormDigit,
  ProFormText,
  ProFormDatePicker,
} from '@ant-design/pro-components';
import type { ProColumns, ActionType } from '@ant-design/pro-components';
import { Drawer, Table, Button, message } from 'antd';
import request from '@/shared/utils/request';
import { getDictionaryItems } from '@/features/dicttype/services/dictRead';
import dayjs from 'dayjs';
import { AttachmentList } from '@/shared/components/AttachmentList';
import { toProTableResult, type SpringPage } from '@/shared/utils/adapters';

interface CustomerItem {
  id: number;
  customerCode: string;
  name: string;
  status: string;
}

interface QuoteDetail {
  id: number;
  quoteNo: string;
  status: string;
  sourceType: string;
  customerId: number;
  customerName: string;
  recipient: string;
  phone: string;
  address: string;
  currency: string;
  createdAt: string;
  createdBy: string;
}

/** List row from GET /api/v1/quotes (general search) */
interface QuoteListRow {
  id: number;
  quoteNo: string;
  status: string;
  customerName: string;
  totalAmount?: number;
  paidAmount?: number;
  createdAt?: string;
}

/** Payment row from GET /api/v1/payments/quote/{quoteId} */
interface QuotePaymentRow {
  id: number;
  quoteId: number;
  amount: number;
  currency: string;
  status?: string;
  paidAt?: string;
  isFinanceConfirmed?: boolean;
  createdAt?: string;
}

/** Quote line item from GET/POST/PUT /api/v1/quotes/{id}/items */
interface QuoteLineItem {
  id?: number;
  quoteId?: number;
  lineOrder?: number;
  expenseType?: string;
  expenseTypeLabelSnapshot?: string | null;
  description?: string;
  spec?: string;
  unit?: string;
  quantity?: number;
  unitPrice?: number;
  amount?: number;
  warehouse?: string;
  remark?: string;
}

/** Allowlist for backend enum; dict options are filtered to these values. */
const QUOTE_EXPENSE_TYPE_VALUES = [
  'REPAIR', 'ON_SITE', 'PARTS', 'PLATFORM', 'DATA_PLAN',
  'STORAGE', 'SHIPPING', 'PACKING', 'CONSTRUCTION', 'OTHER',
] as const;

const EXPENSE_TYPE_OPTIONS_FALLBACK = [
  { label: '维修费', value: 'REPAIR' },
  { label: '上门费', value: 'ON_SITE' },
  { label: '配件费', value: 'PARTS' },
  { label: '平台费', value: 'PLATFORM' },
  { label: '流量费', value: 'DATA_PLAN' },
  { label: '仓储费', value: 'STORAGE' },
  { label: '运输费', value: 'SHIPPING' },
  { label: '包装费', value: 'PACKING' },
  { label: '施工费', value: 'CONSTRUCTION' },
  { label: '其他', value: 'OTHER' },
];

export default function Quote() {
  const actionRef = useRef<ActionType>(null);
  const [detailId, setDetailId] = useState<number | null>(null);
  const [quoteDetail, setQuoteDetail] = useState<QuoteDetail | null>(null);
  const [items, setItems] = useState<QuoteLineItem[]>([]);
  const [payments, setPayments] = useState<QuotePaymentRow[]>([]);
  const [exporting, setExporting] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [confirmingQuote, setConfirmingQuote] = useState(false);

  const columns: ProColumns<QuoteListRow>[] = [
    { title: '报价单号', dataIndex: 'quoteNo', width: 140 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      valueEnum: {
        DRAFT: { text: '草稿', status: 'Default' },
        PENDING_APPROVAL: { text: '审批中', status: 'Processing' },
        APPROVAL_PENDING: { text: '审批中', status: 'Processing' },
        APPROVED: { text: '已审批', status: 'Success' },
        REJECTED: { text: '已驳回', status: 'Error' },
        CONVERTED_TO_ORDER: { text: '已转订单', status: 'Success' },
        EXPIRED: { text: '已过期', status: 'Error' },
        RETURNED: { text: '已退回', status: 'Error' },
        CONFIRMED: { text: '已确认', status: 'Success' },
        PARTIAL_PAID: { text: '部分回款', status: 'Processing' },
        PAID: { text: '已回款', status: 'Success' },
        VOID_AUDIT_PENDING: { text: '作废审批中', status: 'Warning' },
        VOID_AUDIT_INITIATOR: { text: '待发起人', status: 'Warning' },
        VOID_AUDIT_FINANCE: { text: '待财务', status: 'Warning' },
        VOID_AUDIT_LEADER: { text: '待领导', status: 'Warning' },
        CANCELLED: { text: '已取消', status: 'Error' },
      },
    },
    { title: '客户', dataIndex: 'customerName', ellipsis: true },
    { title: '总金额', dataIndex: 'totalAmount', valueType: 'money', width: 120, search: false },
    { title: '已回款', dataIndex: 'paidAmount', valueType: 'money', width: 120, search: false },
    { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime', width: 180, search: false },
    {
      title: '操作',
      valueType: 'option',
      width: 160,
      render: (_, row) => [
        <a key="detail" onClick={() => loadDetail(row.id)}>详情</a>,
        <a key="export" onClick={() => exportQuote(row.id)}>导出</a>,
      ],
    },
  ];

  const fetchItems = useCallback(async (quoteId: number) => {
    const { data } = await request.get<QuoteLineItem[]>(`/v1/quotes/${quoteId}/items`);
    setItems(Array.isArray(data) ? data : []);
  }, []);

  const fetchPayments = useCallback(async (quoteId: number) => {
    try {
      const { data } = await request.get<QuotePaymentRow[]>(`/v1/payments/quote/${quoteId}`);
      setPayments(Array.isArray(data) ? data : []);
    } catch {
      setPayments([]);
    }
  }, []);

  async function loadDetail(id: number) {
    setDetailId(id);
    try {
      const { data: quote } = await request.get<QuoteDetail>(`/v1/quotes/${id}`);
      setQuoteDetail(quote);
      await fetchItems(id);
      await fetchPayments(id);
    } catch {
      setQuoteDetail(null);
      setItems([]);
      setPayments([]);
    }
  }

  async function exportQuote(id: number) {
    setExporting(true);
    try {
      const res = await request.get(`/v1/quotes/${id}/export`, { params: { format: 'xlsx' }, responseType: 'blob' });
      const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `quote-${id}.xlsx`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('导出成功');
    } catch {
      message.error('导出失败');
    } finally {
      setExporting(false);
    }
  }

  const isDraft = quoteDetail?.status === 'DRAFT';
  const isPending = quoteDetail?.status === 'APPROVAL_PENDING';
  const canCreateShipment = isPending || quoteDetail?.status === 'CONFIRMED';

  async function handleSubmitQuote() {
    if (detailId == null) return;
    setSubmitting(true);
    try {
      await request.post(`/v1/quotes/${detailId}/submit`);
      message.success('提交成功');
      await loadDetail(detailId);
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      message.error(err?.response?.data?.message ?? '提交失败');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleConfirmQuote() {
    if (detailId == null) return;
    setConfirmingQuote(true);
    try {
      await request.post(`/v1/quotes/${detailId}/confirm`);
      message.success('已确认');
      await loadDetail(detailId);
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      message.error(err?.response?.data?.message ?? '确认失败');
    } finally {
      setConfirmingQuote(false);
    }
  }

  return (
    <div style={{ padding: 24 }}>
      <ProTable<QuoteListRow>
        actionRef={actionRef}
        columns={columns}
        request={async (params, sort) => {
          try {
            const page = (params.current ?? 1) - 1;
            const size = params.pageSize ?? 10;
            const sortField = sort?.createdAt === 'ascend' ? 'createdAt,asc' : 'createdAt,desc';
            const { data } = await request.get<SpringPage<QuoteListRow>>('/v1/quotes', {
              params: { page, size, sort: sortField },
            });
            return toProTableResult(data);
          } catch {
            return { data: [], total: 0, success: true };
          }
        }}
        rowKey="id"
        search={false}
        pagination={{ pageSize: 10 }}
        headerTitle="报价单"
        toolBarRender={() => [
          <ModalForm<{ customerId: number; customerName?: string; businessLineId?: number }>
            key="create"
            title="新建报价单"
            trigger={<Button type="primary">New Quote</Button>}
            onFinish={async (values) => {
              await request.post('/v1/quotes', {
                sourceType: 'MANUAL',
                sourceRefId: null,
                customerId: values.customerId ?? null,
                customerName: values.customerName ?? undefined,
                businessLineId: values.businessLineId ?? null,
              });
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
            <ProFormDigit name="businessLineId" label="业务线ID（可选）" placeholder="可选" fieldProps={{ min: 1 }} />
          </ModalForm>,
          <Button key="export" disabled onClick={() => message.info('请从行操作中导出单张报价单')}>导出说明</Button>,
        ]}
      />
      <Drawer
        title={`报价单详情 ${quoteDetail?.quoteNo ?? ''}`}
        open={detailId !== null}
        onClose={() => { setDetailId(null); setQuoteDetail(null); setItems([]); setPayments([]); }}
        width={720}
        footer={
          isDraft || isPending ? (
            <div style={{ display: 'flex', gap: 8 }}>
              {isDraft && (
                <Button type="primary" loading={submitting} onClick={handleSubmitQuote}>
                  提交（Draft → Pending）
                </Button>
              )}
              {isPending && (
                <Button type="primary" loading={confirmingQuote} onClick={handleConfirmQuote}>
                  Confirm / Approve（Pending → Confirmed）
                </Button>
              )}
            </div>
          ) : null
        }
      >
        {quoteDetail && (
          <>
            <div style={{ marginBottom: 16 }}>
              <strong>基本信息</strong>
              <p>单号: {quoteDetail.quoteNo} | 状态: {quoteDetail.status} | 客户: {quoteDetail.customerName}</p>
              <p>收件人: {quoteDetail.recipient ?? '-'} | 电话: {quoteDetail.phone ?? '-'} | 地址: {quoteDetail.address ?? '-'}</p>
              <p>创建: {quoteDetail.createdAt ? dayjs(quoteDetail.createdAt).format('YYYY-MM-DD HH:mm') : '-'} by {quoteDetail.createdBy ?? '-'}</p>
            </div>
            <div style={{ marginBottom: 16 }}>
              <strong>明细</strong>
              {isDraft ? (
                <QuoteItemsEditable
                  quoteId={detailId!}
                  items={items}
                  fetchItems={() => detailId != null && fetchItems(detailId)}
                />
              ) : (
                <Table<QuoteLineItem>
                  dataSource={items}
                  rowKey={(r) => String(r.id ?? r.lineOrder ?? Math.random())}
                  columns={[
                    { title: '类型', dataIndex: 'expenseType', width: 100, render: (v, row) => (row.expenseTypeLabelSnapshot ?? EXPENSE_TYPE_OPTIONS_FALLBACK.find((o) => o.value === v)?.label ?? v) },
                    { title: '描述', dataIndex: 'description' },
                    { title: '数量', dataIndex: 'quantity', width: 80 },
                    { title: '单价', dataIndex: 'unitPrice', width: 100, render: (v) => (v != null ? Number(v) : '-') },
                    { title: '金额', dataIndex: 'amount', width: 100, render: (v) => (v != null ? Number(v) : '-') },
                  ]}
                  pagination={false}
                  size="small"
                />
              )}
            </div>
            {canCreateShipment && (
              <div style={{ marginBottom: 16 }}>
                <strong>发货</strong>
                <div style={{ marginTop: 8 }}>
                  <CreateShipmentFromQuoteButton
                    quoteId={detailId!}
                    customerId={quoteDetail.customerId}
                    customerName={quoteDetail.customerName}
                    onSuccess={() => message.success('发货单已创建')}
                  />
                </div>
              </div>
            )}
            <div style={{ marginBottom: 16 }}>
              <strong>回款记录</strong>
              <QuotePaymentSection
                quoteId={detailId!}
                quoteCurrency={quoteDetail.currency}
                payments={payments}
                onRecordSuccess={() => { fetchPayments(detailId!); loadDetail(detailId!); }}
                onConfirmSuccess={() => { fetchPayments(detailId!); loadDetail(detailId!); }}
              />
            </div>
            <AttachmentList fileUrls={undefined} />
          </>
        )}
      </Drawer>
    </div>
  );
}

/** Editable items table: Add line (Modal), Edit (Modal), Delete; only in DRAFT. */
function QuoteItemsEditable({
  quoteId,
  items,
  fetchItems,
}: {
  quoteId: number;
  items: QuoteLineItem[];
  fetchItems: () => Promise<void>;
}) {
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [editingRow, setEditingRow] = useState<QuoteLineItem | null>(null);
  const [expenseTypeOptions, setExpenseTypeOptions] = useState<{ label: string; value: string; disabled?: boolean }[]>(EXPENSE_TYPE_OPTIONS_FALLBACK);

  useEffect(() => {
    getDictionaryItems('quote_expense_type', { includeDisabled: false })
      .then((res) => {
        const allowed = res.items?.filter((i) => QUOTE_EXPENSE_TYPE_VALUES.includes(i.value as (typeof QUOTE_EXPENSE_TYPE_VALUES)[number])) ?? [];
        setExpenseTypeOptions(allowed.map((i) => ({ label: i.label, value: i.value })));
      })
      .catch(() => {
        setExpenseTypeOptions(EXPENSE_TYPE_OPTIONS_FALLBACK);
      });
  }, []);

  async function handleAdd(values: { expenseType: string; description?: string; quantity: number; unitPrice: number }) {
    await request.post(`/v1/quotes/${quoteId}/items`, {
      expenseType: values.expenseType,
      description: values.description ?? null,
      spec: null,
      unit: null,
      quantity: values.quantity,
      unitPrice: values.unitPrice,
      warehouse: null,
      remark: null,
      manualPriceReason: null,
      partId: null,
    });
    message.success('添加成功');
    setAddModalOpen(false);
    await fetchItems();
  }

  async function handleUpdate(itemId: number, values: { expenseType: string; description?: string; quantity: number; unitPrice: number }) {
    await request.put(`/v1/quotes/items/${itemId}`, {
      expenseType: values.expenseType,
      description: values.description ?? null,
      spec: null,
      unit: null,
      quantity: values.quantity,
      unitPrice: values.unitPrice,
      warehouse: null,
      remark: null,
      manualPriceReason: null,
      partId: null,
    });
    message.success('保存成功');
    setEditingRow(null);
    await fetchItems();
  }

  async function handleDelete(itemId: number) {
    await request.delete(`/v1/quotes/items/${itemId}`);
    message.success('已删除');
    await fetchItems();
  }

  return (
    <>
      <div style={{ marginBottom: 8 }}>
        <Button type="primary" size="small" onClick={() => setAddModalOpen(true)}>
          Add Line
        </Button>
      </div>
      <Table<QuoteLineItem>
        dataSource={items}
        rowKey={(r) => String(r.id ?? r.lineOrder ?? Math.random())}
        size="small"
        columns={[
          { title: '类型', dataIndex: 'expenseType', width: 100, render: (v, row) => (row.expenseTypeLabelSnapshot ?? EXPENSE_TYPE_OPTIONS_FALLBACK.find((o) => o.value === v)?.label ?? v) },
          { title: '描述', dataIndex: 'description', ellipsis: true },
          { title: '数量', dataIndex: 'quantity', width: 80 },
          { title: '单价', dataIndex: 'unitPrice', width: 100, render: (v) => (v != null ? Number(v) : '-') },
          { title: '金额', dataIndex: 'amount', width: 100, render: (v) => (v != null ? Number(v) : '-') },
          {
            title: '操作',
            width: 140,
            render: (_, row) => {
              if (row.id == null) return null;
              return (
                <>
                  <Button type="link" size="small" onClick={() => setEditingRow(row)}>编辑</Button>
                  <Button type="link" size="small" danger onClick={() => handleDelete(row.id!)}>删除</Button>
                </>
              );
            },
          },
        ]}
        pagination={false}
      />
      <ModalForm<{ expenseType: string; description?: string; quantity: number; unitPrice: number }>
        title="添加明细行"
        open={addModalOpen}
        onOpenChange={setAddModalOpen}
        onFinish={async (values) => {
          await handleAdd(values);
          return true;
        }}
        initialValues={{ expenseType: 'REPAIR', quantity: 1, unitPrice: 0 }}
      >
        <ProFormSelect name="expenseType" label="类型" options={expenseTypeOptions} rules={[{ required: true }]} />
        <ProFormText name="description" label="描述" placeholder="可选" />
        <ProFormDigit name="quantity" label="数量" min={1} rules={[{ required: true }]} />
        <ProFormDigit name="unitPrice" label="单价" min={0} fieldProps={{ precision: 2 }} rules={[{ required: true }]} />
      </ModalForm>
      {editingRow && (
        <ModalForm<{ expenseType: string; description?: string; quantity: number; unitPrice: number }>
          title="编辑明细行"
          open={!!editingRow}
          onOpenChange={(open) => !open && setEditingRow(null)}
          onFinish={async (values) => {
            if (editingRow.id != null) await handleUpdate(editingRow.id, values);
            return true;
          }}
          initialValues={{
            expenseType: editingRow.expenseType ?? 'REPAIR',
            description: editingRow.description,
            quantity: editingRow.quantity ?? 1,
            unitPrice: editingRow.unitPrice ?? 0,
          }}
        >
          <ProFormSelect
            name="expenseType"
            label="类型"
            options={
              editingRow.expenseType && !expenseTypeOptions.some((o) => o.value === editingRow.expenseType)
                ? [
                    ...expenseTypeOptions,
                    {
                      label: editingRow.expenseTypeLabelSnapshot ?? EXPENSE_TYPE_OPTIONS_FALLBACK.find((o) => o.value === editingRow.expenseType)?.label ?? editingRow.expenseType,
                      value: editingRow.expenseType,
                      disabled: true,
                    },
                  ]
                : expenseTypeOptions
            }
            rules={[{ required: true }]}
          />
          <ProFormText name="description" label="描述" placeholder="可选" />
          <ProFormDigit name="quantity" label="数量" min={1} rules={[{ required: true }]} />
          <ProFormDigit name="unitPrice" label="单价" min={0} fieldProps={{ precision: 2 }} rules={[{ required: true }]} />
        </ModalForm>
      )}
    </>
  );
}

/** Create Shipment from Quote: pre-fill customerId, pass quoteId to create-from-quote. */
function CreateShipmentFromQuoteButton({
  quoteId,
  customerId,
  customerName,
  onSuccess,
}: {
  quoteId: number;
  customerId: number;
  customerName: string;
  onSuccess: () => void;
}) {
  return (
    <ModalForm<{ customerId: number; shipmentType: string }>
      title="从报价单创建发货"
      trigger={<Button size="small">Create Shipment</Button>}
      initialValues={{ customerId, shipmentType: 'STANDARD' }}
      onFinish={async () => {
        const today = new Date().toISOString().slice(0, 10);
        await request.post('/v1/shipments/create-from-quote', {
          quoteId,
          initiatorId: 1,
          entrustMatter: 'From Quote',
          shipDate: today,
          quantity: 1,
          model: '-',
          needPackaging: false,
          pickupContact: '-',
          pickupPhone: '-',
          needLoading: false,
          pickupAddress: '-',
          receiverContact: customerName,
          receiverPhone: '-',
          needUnloading: false,
          deliveryAddress: '-',
          remark: null,
        });
        onSuccess();
        return true;
      }}
    >
      <ProFormSelect
        name="customerId"
        label="客户"
        disabled
        options={[{ label: customerName, value: customerId }]}
      />
      <ProFormSelect
        name="shipmentType"
        label="类型"
        options={[
          { label: '标准', value: 'STANDARD' },
          { label: '加急', value: 'EXPRESS' },
        ]}
      />
    </ModalForm>
  );
}

/** Payment History: list + Record Payment modal + Confirm for unconfirmed. */
function QuotePaymentSection({
  quoteId,
  quoteCurrency,
  payments,
  onRecordSuccess,
  onConfirmSuccess,
}: {
  quoteId: number;
  quoteCurrency: string;
  payments: QuotePaymentRow[];
  onRecordSuccess: () => void;
  onConfirmSuccess: () => void;
}) {
  const [recordModalOpen, setRecordModalOpen] = useState(false);

  async function handleRecordPayment(values: { amount: number; currency: string; transactionDate: string | unknown }) {
    const dateStr = values.transactionDate
      ? dayjs(values.transactionDate as string).format('YYYY-MM-DD')
      : dayjs().format('YYYY-MM-DD');
    const paidAt = dateStr + 'T12:00:00';
    await request.post('/v1/payments/submit', {
      quoteId,
      amount: values.amount,
      methodId: 1,
      paidAt,
      submitterId: 1,
      isFinance: false,
      attachmentUrls: null,
      paymentBatchNo: null,
      currency: values.currency ?? quoteCurrency,
      note: null,
    });
    message.success('回款已登记');
    setRecordModalOpen(false);
    onRecordSuccess();
  }

  async function handleConfirmPayment(paymentId: number) {
    await request.post(`/v1/payments/${paymentId}/confirm`, {
      action: 'CONFIRM',
      confirmerId: 1,
      confirmNote: null,
      overpaymentStrategy: null,
    });
    message.success('已确认');
    onConfirmSuccess();
  }

  return (
    <>
      <div style={{ marginTop: 8, marginBottom: 8 }}>
        <Button type="primary" size="small" onClick={() => setRecordModalOpen(true)}>
          Record Payment
        </Button>
      </div>
      <Table<QuotePaymentRow>
        dataSource={payments}
        rowKey="id"
        size="small"
        columns={[
          { title: '金额', dataIndex: 'amount', width: 100, render: (v) => (v != null ? Number(v) : '-') },
          { title: '币种', dataIndex: 'currency', width: 80 },
          { title: '状态', dataIndex: 'status', width: 100 },
          { title: '回款时间', dataIndex: 'paidAt', width: 160, render: (v) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '-') },
          {
            title: '操作',
            width: 80,
            render: (_, row) =>
              row.isFinanceConfirmed === false ? (
                <Button type="link" size="small" onClick={() => handleConfirmPayment(row.id)}>Confirm</Button>
              ) : null,
          },
        ]}
        pagination={false}
      />
      <ModalForm<{ amount: number; currency: string; transactionDate: string }>
        title="登记回款"
        open={recordModalOpen}
        onOpenChange={setRecordModalOpen}
        onFinish={async (values) => {
          await handleRecordPayment(values);
          return true;
        }}
        initialValues={{
          currency: quoteCurrency ?? 'CNY',
          transactionDate: dayjs().format('YYYY-MM-DD'),
        }}
      >
        <ProFormDigit name="amount" label="金额" min={0.01} fieldProps={{ precision: 2 }} rules={[{ required: true }]} />
        <ProFormSelect
          name="currency"
          label="币种"
          options={[
            { label: 'CNY', value: 'CNY' },
            { label: 'USD', value: 'USD' },
          ]}
        />
        <ProFormDatePicker name="transactionDate" label="回款日期" rules={[{ required: true }]} />
      </ModalForm>
    </>
  );
}
