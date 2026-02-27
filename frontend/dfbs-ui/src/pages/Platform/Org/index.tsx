import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Button, Form, Input, message, Modal, Popconfirm, Select, Switch, Tag } from 'antd';
import type { FormInstance } from 'antd/es/form';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable, ProDescriptions } from '@ant-design/pro-components';
import type { AxiosError } from 'axios';
import request from '@/shared/utils/request';
import SmartReferenceSelect from '@/shared/components/SmartReferenceSelect';
import SmartInput from '@/shared/components/SmartInput';
import DuplicateCheckModal from '@/features/platform/components/DuplicateCheckModal';
import type { DuplicateMatchItem } from '@/features/platform/components/DuplicateCheckModal';
import { getPlatformStatusLabelForOrg } from '@/features/platform/components/HitAnalysisPanel';
import type { SpringPage } from '@/shared/utils/adapters';
import { PhoneRule, EmailRule } from '@/shared/utils/validators/common';
import { OrgCodeRule, OrgCodeUppercaseRule } from '@/features/platform/utils/validators';
import { getPlatformConfigs, type PlatformConfigItem } from '@/features/platform/services/platformConfig';
import { useDraftForm } from '@/shared/hooks/useDraftForm';
import { useEffectivePermissions } from '@/shared/hooks/useEffectivePermissions';

const PERM_ORGS_CREATE = 'platform_application.orgs:CREATE';
const PERM_ORGS_EDIT = 'platform_application.orgs:EDIT';
const PERM_ORGS_DELETE = 'platform_application.orgs:DELETE';

/** Region options for create/edit org (same as Platform Application flow). */
const REGION_OPTIONS = [
  { label: '华东', value: '华东' },
  { label: '华北', value: '华北' },
  { label: '华南', value: '华南' },
  { label: '西部', value: '西部' },
  { label: '海外', value: '海外' },
];

type PlatformType = 'INHAND' | 'HENDONG' | 'JINGPIN' | 'OTHER';

interface LinkedCustomer {
  id: number;
  name: string;
}

interface PlatformOrgRow {
  id: number;
  platform: PlatformType;
  orgCodeShort: string;
  orgCode?: string;
  orgFullName: string;
  orgName?: string;
  customerIds?: number[];
  linkedCustomers?: LinkedCustomer[];
  customerId?: number;
  customerName?: string;
  contactPerson?: string | null;
  contactPhone?: string | null;
  contactEmail?: string | null;
  salesPerson?: string | null;
  region?: string | null;
  remark?: string | null;
  isActive?: boolean | null;
  status?: 'ACTIVE' | 'ARREARS' | 'DELETED' | null;
  createdAt?: string;
  updatedAt?: string;
  sourceInfo?: SourceInfo | null;
}

interface SourceInfo {
  type: 'LEGACY' | 'SALES' | 'SERVICE' | 'MANUAL';
  applicationNo?: string | null;
  applicantName?: string | null;
  plannerName?: string | null;
  adminName?: string | null;
}

interface PlatformOrgFormValues {
  platform: PlatformType;
  orgCodeShort: string;
  orgFullName: string;
  customerId: number | null;
  customerIds?: number[];
  customerName?: string;
  contactPerson?: string;
  contactPhone?: string;
  contactEmail?: string;
  salesPerson?: string;
  region?: string;
  remark?: string;
  isActive?: boolean;
  status?: 'ACTIVE' | 'ARREARS' | 'DELETED';
}

function showError(e: unknown) {
  const err = e as AxiosError<{ message?: string }>;
  const msg = err.response?.data?.message ?? err.message ?? '操作失败';
  message.error(msg);
}

async function fetchCustomerNameMap(ids: number[]) {
  const uniqueIds = Array.from(new Set(ids.filter((id) => typeof id === 'number'))) as number[];
  if (uniqueIds.length === 0) {
    return {} as Record<number, string>;
  }
  const entries = await Promise.all(
    uniqueIds.map(async (id) => {
      try {
        const { data } = await request.get<{ name?: string }>(`/masterdata/customers/${id}`);
        return [id, data?.name ?? `客户#${id}`] as [number, string];
      } catch {
        return [id, `客户#${id}`] as [number, string];
      }
    }),
  );
  return Object.fromEntries(entries) as Record<number, string>;
}

/**
 * Customer select with orgFullName auto-fill (parity with Application create flow).
 * When customer changes: set orgFullName to customer name only if orgFullName is empty
 * or still equals the previous auto-filled value (do not overwrite manual edits).
 */
function CustomerSelectField({
  form,
  disabled,
  lastAutofillOrgFullName,
  setLastAutofillOrgFullName,
}: {
  form: FormInstance<PlatformOrgFormValues>;
  disabled?: boolean;
  lastAutofillOrgFullName?: string;
  setLastAutofillOrgFullName?: (v: string) => void;
}) {
  const name = Form.useWatch('customerName', form) ?? '';
  return (
    <>
      <Form.Item name="customerId" hidden rules={[{ required: true, message: '请选择客户' }]} />
      <Form.Item
        name="customerName"
        label="客户"
        rules={[{ required: true, message: '请选择客户' }]}
      >
        <SmartReferenceSelect
          entityType="CUSTOMER"
          value={name}
          onChange={(ref) => {
            form.setFieldValue('customerId', ref.id);
            form.setFieldValue('customerName', ref.name);
            const customerName = ref.name ?? '';
            const currentOrgFullName = form.getFieldValue('orgFullName') ?? '';
            if (currentOrgFullName === '' || currentOrgFullName === lastAutofillOrgFullName) {
              form.setFieldValue('orgFullName', customerName);
              setLastAutofillOrgFullName?.(customerName);
            }
          }}
          disabled={disabled}
          placeholder="选择或输入客户名称"
        />
      </Form.Item>
    </>
  );
}

export default function PlatformOrg() {
  const actionRef = useRef<ActionType>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [currentRow, setCurrentRow] = useState<PlatformOrgRow | null>(null);
  const [editCustomerOptions, setEditCustomerOptions] = useState<{ label: string; value: number }[]>([]);
  const [duplicateMatches, setDuplicateMatches] = useState<DuplicateMatchItem[]>([]);
  const [duplicateVisible, setDuplicateVisible] = useState(false);
  const [duplicateCurrentInput, setDuplicateCurrentInput] = useState<{ customerName: string; email: string; phone: string } | null>(null);
  const [pendingOrgValues, setPendingOrgValues] = useState<PlatformOrgFormValues | null>(null);
  const [createForm] = Form.useForm<PlatformOrgFormValues>();
  const [editForm] = Form.useForm<PlatformOrgFormValues>();
  const createPlatform = Form.useWatch('platform', createForm) as string | undefined;
  const [lastAutofillOrgFullName, setLastAutofillOrgFullName] = useState('');

  const { saveDraft, loadDraft, clearDraft, hasDraft } = useDraftForm<PlatformOrgFormValues>('platform-org-create-admin');
  const { has: hasPermission } = useEffectivePermissions();

  const [platformConfigs, setPlatformConfigs] = useState<PlatformConfigItem[]>([]);
  useEffect(() => {
    getPlatformConfigs().then(setPlatformConfigs).catch(() => setPlatformConfigs([]));
  }, []);
  const platformOptions = useMemo(
    () => (platformConfigs?.filter((p) => p.isActive) ?? []).map((p) => ({ label: p.platformName, value: p.platformCode })),
    [platformConfigs]
  );
  const getConfig = (code: string) => platformConfigs?.find((p) => p.platformCode === code);

  useEffect(() => {
    if (!editOpen || !currentRow) {
      setEditCustomerOptions([]);
      return;
    }
    let cancelled = false;
    (async () => {
      try {
        const { data } = await request.get<SpringPage<{ id: number; name: string; customerCode?: string }>>(
          '/v1/customers',
          { params: { page: 0, size: 500, sort: 'id,asc' } },
        );
        const list = data?.content ?? [];
        const fromApi = list.map((c) => ({
          label: c.customerCode ? `${c.name} (${c.customerCode})` : c.name,
          value: c.id,
        }));
        const idsFromApi = new Set(fromApi.map((o) => o.value));
        const fromLinked = (currentRow.linkedCustomers ?? [])
          .filter((c) => !idsFromApi.has(c.id))
          .map((c) => ({ label: c.name, value: c.id }));
        if (!cancelled) setEditCustomerOptions([...fromApi, ...fromLinked]);
      } catch {
        const fromLinked = (currentRow.linkedCustomers ?? []).map((c) => ({ label: c.name, value: c.id }));
        if (!cancelled) setEditCustomerOptions(fromLinked);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [editOpen, currentRow]);

  const buildOrgPayload = (row: PlatformOrgRow, overrides: Partial<PlatformOrgRow> = {}) => ({
    platform: row.platform,
    orgCodeShort: row.orgCodeShort,
    orgFullName: row.orgFullName,
    customerIds: row.customerIds?.length ? row.customerIds : (row.customerId != null ? [row.customerId] : []),
    contactPerson: row.contactPerson ?? undefined,
    contactPhone: row.contactPhone ?? undefined,
    contactEmail: row.contactEmail ?? undefined,
    salesPerson: row.salesPerson ?? undefined,
    region: row.region ?? undefined,
    remark: row.remark ?? undefined,
    isActive: row.isActive ?? true,
    status: row.status ?? 'ACTIVE',
    ...overrides,
  });

  const handleToggleActive = async (row: PlatformOrgRow, checked: boolean) => {
    try {
      await request.put(`/v1/platform-orgs/${row.id}`, buildOrgPayload(row, { isActive: checked }));
      message.success('状态已更新');
      actionRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  };

  const handleDelete = async (row: PlatformOrgRow) => {
    try {
      await request.put(`/v1/platform-orgs/${row.id}`, buildOrgPayload(row, { status: 'DELETED', isActive: false }));
      message.success('已删除');
      actionRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  };

  const fetchTableData = async (
    params: Record<string, string | number | boolean | undefined>,
  ) => {
    const query: Record<string, unknown> = {};
    if (params.platform) {
      query.platform = params.platform;
    }
    if (params.customerId) {
      query.customerId = params.customerId;
    }

    const { data } = await request.get<PlatformOrgRow[]>('/v1/platform-orgs', { params: query });
    const rawList = Array.isArray(data) ? data : [];

    const filtered = rawList.filter((item) => {
      const codeMatch = params.orgCodeShort
        ? (item.orgCodeShort ?? item.orgCode)?.toLowerCase().includes(String(params.orgCodeShort).toLowerCase())
        : true;
      const nameMatch = params.orgFullName
        ? (item.orgFullName ?? item.orgName)?.toLowerCase().includes(String(params.orgFullName).toLowerCase())
        : true;
      const statusFilter =
        params.isActive === undefined || params.isActive === ''
          ? true
          : item.isActive === (params.isActive === true || params.isActive === 'true');
      return codeMatch && nameMatch && statusFilter;
    });

    return { data: filtered, success: true };
  };

  const openCreateModal = () => {
    createForm.resetFields();
    createForm.setFieldsValue({ isActive: true });
    setLastAutofillOrgFullName('');
    setCreateOpen(true);
  };

  const doCreateOrg = async (values: PlatformOrgFormValues) => {
    await request.post('/v1/platform-orgs', {
      platform: values.platform,
      orgCodeShort: values.orgCodeShort?.trim(),
      orgFullName: values.orgFullName,
      customerIds: values.customerId != null ? [values.customerId] : [],
      contactPerson: values.contactPerson ?? undefined,
      contactPhone: values.contactPhone ?? undefined,
      contactEmail: values.contactEmail ?? undefined,
      salesPerson: values.salesPerson ?? undefined,
      region: values.region ?? undefined,
      remark: values.remark ?? undefined,
      isActive: values.isActive ?? true,
    });
  };

  const handleCreate = async () => {
    try {
      const values = await createForm.validateFields();
      const { data: matches } = await request.post<DuplicateMatchItem[]>(
        '/v1/platform-account-applications/check-duplicates',
        {
          platform: values.platform,
          customerName: values.customerName ? String(values.customerName).trim() : '',
          email: values.contactEmail ?? '',
          contactPhone: values.contactPhone ?? '',
        }
      );
      if (matches?.length) {
        setPendingOrgValues(values);
        setDuplicateMatches(matches);
        setDuplicateCurrentInput({
          customerName: values.customerName ? String(values.customerName).trim() : '',
          email: (values.contactEmail ?? '') as string,
          phone: (values.contactPhone ?? '') as string,
        });
        setDuplicateVisible(true);
        return;
      }
      await doCreateOrg(values);
      clearDraft();
      message.success('创建成功');
      setCreateOpen(false);
      createForm.resetFields();
      actionRef.current?.reload();
    } catch (e: any) {
      if (e?.errorFields) {
        return;
      }
      showError(e);
    }
  };

  const handleDuplicateReturn = () => {
    setDuplicateVisible(false);
    setDuplicateMatches([]);
    setDuplicateCurrentInput(null);
  };

  const handleDuplicateConfirmAdd = async () => {
    if (!pendingOrgValues) return;
    try {
      await doCreateOrg(pendingOrgValues);
      clearDraft();
      message.success('创建成功');
      setPendingOrgValues(null);
      setDuplicateVisible(false);
      setDuplicateMatches([]);
      setDuplicateCurrentInput(null);
      setCreateOpen(false);
      createForm.resetFields();
      actionRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  };

  const handleEdit = async () => {
    if (!currentRow) return;
    try {
      const values = await editForm.validateFields();
      await request.put(`/v1/platform-orgs/${currentRow.id}`, {
        platform: values.platform,
        orgCodeShort: values.orgCodeShort,
        orgFullName: values.orgFullName,
        customerIds: Array.isArray(values.customerIds) ? values.customerIds : (values.customerId != null ? [values.customerId] : []),
        contactPerson: values.contactPerson ?? undefined,
        contactPhone: values.contactPhone ?? undefined,
        contactEmail: values.contactEmail ?? undefined,
        salesPerson: values.salesPerson ?? undefined,
        region: values.region ?? undefined,
        remark: values.remark ?? undefined,
        isActive: values.isActive ?? true,
        status: currentRow.status ?? 'ACTIVE',
      });
      message.success('更新成功');
      setEditOpen(false);
      setCurrentRow(null);
      actionRef.current?.reload();
    } catch (e: any) {
      if (e?.errorFields) {
        return;
      }
      showError(e);
    }
  };

  const deletedCellStyle: React.CSSProperties = { color: '#999', textDecoration: 'line-through' };

  const columns: ProColumns<PlatformOrgRow>[] = [
    {
      title: '平台',
      dataIndex: 'platform',
      width: 120,
      render: (_, row) => (
        <span style={row.status === 'DELETED' ? deletedCellStyle : undefined}>
          <Tag color="blue">{getConfig(row.platform)?.platformName ?? row.platform}</Tag>
        </span>
      ),
    },
    {
      title: '机构代码/简称',
      dataIndex: 'orgCodeShort',
      width: 160,
      render: (_, row) => {
        const raw = (row.orgCodeShort ?? row.orgCode ?? '') as string;
        const display = row.status === 'DELETED' ? raw.split('_DEL_')[0] || raw : raw;
        const style = row.status === 'DELETED' ? { textDecoration: 'line-through' as const, color: '#999' } : undefined;
        return <span style={style}>{display || '—'}</span>;
      },
    },
    {
      title: '机构全称',
      dataIndex: 'orgFullName',
      width: 200,
      ellipsis: true,
      render: (_, row) => {
        const text = row.orgFullName ?? row.orgName ?? '—';
        return <span style={row.status === 'DELETED' ? deletedCellStyle : undefined}>{text}</span>;
      },
    },
    {
      title: '联系人',
      dataIndex: 'contactPerson',
      width: 100,
      ellipsis: true,
      search: false,
      render: (_, row) => (
        <span style={row.status === 'DELETED' ? deletedCellStyle : undefined}>{row.contactPerson ?? '—'}</span>
      ),
    },
    {
      title: '联系电话',
      dataIndex: 'contactPhone',
      width: 130,
      ellipsis: true,
      search: false,
      render: (_, row) => (
        <span style={row.status === 'DELETED' ? deletedCellStyle : undefined}>{row.contactPhone ?? '—'}</span>
      ),
    },
    {
      title: '邮箱',
      dataIndex: 'contactEmail',
      width: 180,
      ellipsis: true,
      search: false,
      render: (_, row) => (
        <span style={row.status === 'DELETED' ? deletedCellStyle : undefined}>{row.contactEmail ?? '—'}</span>
      ),
    },
    {
      title: '所属客户',
      dataIndex: 'linkedCustomers',
      width: 260,
      render: (_, row) => {
        const list = row.linkedCustomers ?? [];
        const style = row.status === 'DELETED' ? deletedCellStyle : undefined;
        if (list.length === 0) return <span style={style ?? { color: '#999' }}>—</span>;
        return (
          <span style={style}>
            {list.map((c) => (
              <Tag key={c.id} color="blue" style={{ marginBottom: 4 }}>
                {c.name}
              </Tag>
            ))}
          </span>
        );
      },
    },
    {
      title: '区域',
      dataIndex: 'region',
      width: 160,
      search: false,
      ellipsis: true,
      render: (_, row) => (
        <span style={row.status === 'DELETED' ? deletedCellStyle : undefined}>{row.region ?? '—'}</span>
      ),
    },
    {
      title: '销售负责人',
      dataIndex: 'salesPerson',
      width: 160,
      search: false,
      ellipsis: true,
      render: (_, row) => (
        <span style={row.status === 'DELETED' ? deletedCellStyle : undefined}>{row.salesPerson ?? '—'}</span>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 120,
      valueType: 'select',
      valueEnum: {
        ACTIVE: { text: '已启用', status: 'Success' },
        ARREARS: { text: '已欠费', status: 'Warning' },
        DELETED: { text: '已删除', status: 'Error' },
      },
      render: (_, row) => {
        const label = getPlatformStatusLabelForOrg(row);
        const color = label === '已删除' ? 'error' : label === '已欠费' ? 'warning' : label === '已禁用' ? 'default' : 'green';
        return <Tag color={color}>{label}</Tag>;
      },
    },
    {
      title: '启用状态',
      dataIndex: 'isActive',
      width: 90,
      search: false,
      render: (_, row) =>
        hasPermission(PERM_ORGS_EDIT) ? (
          <Switch
            checked={!!row.isActive}
            onChange={(val) => handleToggleActive(row, val)}
            disabled={row.status === 'DELETED'}
          />
        ) : (
          <span>{row.isActive ? '启用' : '禁用'}</span>
        ),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      width: 200,
      search: false,
      ellipsis: true,
      render: (_, row) => (
        <span style={row.status === 'DELETED' ? deletedCellStyle : undefined}>{row.remark ?? '—'}</span>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      valueType: 'dateTime',
      width: 180,
      search: false,
      render: (_, row) => (
        <span style={row.status === 'DELETED' ? deletedCellStyle : undefined}>{row.createdAt ?? '—'}</span>
      ),
    },
    {
      title: '操作',
      valueType: 'option',
      width: 200,
      fixed: 'right',
      render: (_, row) => [
        <a
          key="detail"
          onClick={async () => {
            try {
              const { data } = await request.get<PlatformOrgRow>(`/v1/platform-orgs/${row.id}`);
              setCurrentRow(data);
              setDetailOpen(true);
            } catch (e) {
              showError(e);
            }
          }}
        >
          详情
        </a>,
        hasPermission(PERM_ORGS_EDIT) && (
          <a
            key="edit"
            onClick={() => {
              setCurrentRow(row);
              editForm.setFieldsValue({
                platform: row.platform,
                orgCodeShort: row.orgCodeShort ?? row.orgCode,
                orgFullName: row.orgFullName ?? row.orgName,
                customerIds: row.customerIds ?? (row.customerId != null ? [row.customerId] : []),
                isActive: row.isActive ?? true,
                status: row.status ?? 'ACTIVE',
                contactPerson: row.contactPerson ?? undefined,
                contactPhone: row.contactPhone ?? undefined,
                contactEmail: row.contactEmail ?? undefined,
                salesPerson: row.salesPerson ?? undefined,
                region: row.region ?? undefined,
                remark: row.remark ?? undefined,
              });
              setEditOpen(true);
            }}
          >
            编辑
          </a>
        ),
        row.status !== 'DELETED' && hasPermission(PERM_ORGS_DELETE) && (
          <Popconfirm
            key="delete"
            title="确认删除该机构？"
            description="删除后代码将被释放"
            onConfirm={() => handleDelete(row)}
            okText="确认"
            cancelText="取消"
          >
            <a key="delete-btn" style={{ color: 'var(--ant-color-error)' }}>
              删除
            </a>
          </Popconfirm>
        ),
      ].filter(Boolean),
    },
    {
      title: '客户筛选',
      dataIndex: 'customerId',
      hideInTable: true,
      renderFormItem: (_, config) => {
        const formInstance = (config as { form?: FormInstance }).form as FormInstance<PlatformOrgFormValues & { customerNameSearch?: string }> | undefined;
        return (
          <>
            <Form.Item name="customerNameSearch" hidden>
              <Input />
            </Form.Item>
            <SmartReferenceSelect
              entityType="CUSTOMER"
              value={formInstance?.getFieldValue('customerNameSearch') ?? ''}
              onChange={(ref) => {
                formInstance?.setFieldsValue({
                  customerId: ref.id ?? null,
                  customerNameSearch: ref.name,
                });
              }}
              allowClear
              placeholder="选择客户"
            />
          </>
        );
      },
    },
  ];

  const navigate = useNavigate();

  return (
    <div>
      <ProTable<PlatformOrgRow>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={fetchTableData}
        search={{ labelWidth: 100 }}
        pagination={{ pageSize: 10 }}
        scroll={{ x: 1200 }}
        toolBarRender={() => [
          <Button key="sales" type="primary" onClick={() => navigate('/platform/apply?source=sales')}>
            销售申请
          </Button>,
          <Button key="service" onClick={() => navigate('/platform/apply?source=service')}>
            服务申请
          </Button>,
          <Button key="enterprise" onClick={() => navigate('/platform/apply?source=enterprise')}>
            营企申请
          </Button>,
          ...(hasPermission(PERM_ORGS_CREATE)
            ? [<Button key="create" onClick={openCreateModal}>新建机构</Button>]
            : []),
        ]}
      />

      {/* Parity with standard create-org: draft (platform-org-create-admin), customerName→orgFullName autofill, region dropdown, duplicate-check, required fields, isActive default true. */}
      <Modal
        title="新建平台机构"
        open={createOpen}
        destroyOnClose
        onCancel={() => setCreateOpen(false)}
        onOk={handleCreate}
        okText="提交"
        footer={[
          <Button key="cancel" onClick={() => setCreateOpen(false)}>
            取消
          </Button>,
          <Button
            key="draft"
            onClick={() => {
              const values = createForm.getFieldsValue();
              saveDraft(values);
              message.success('草稿已保存');
              setCreateOpen(false);
            }}
          >
            保存草稿
          </Button>,
          <Button key="submit" type="primary" onClick={() => handleCreate()}>
            提交
          </Button>,
        ]}
      >
        <Form form={createForm} layout="vertical">
          {hasDraft && (
            <Alert
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
              message={
                <>
                  您有未提交的草稿。
                  <Button
                    type="link"
                    size="small"
                    onClick={() => {
                      const v = loadDraft();
                      if (v && typeof v === 'object') {
                        createForm.setFieldsValue(v);
                        setLastAutofillOrgFullName((v as PlatformOrgFormValues).orgFullName ?? '');
                      }
                    }}
                  >
                    恢复草稿
                  </Button>
                  <Button type="link" size="small" onClick={clearDraft}>
                    清除草稿
                  </Button>
                </>
              }
            />
          )}
          <Form.Item
            name="platform"
            label="平台"
            rules={[{ required: true, message: '请选择平台' }]}
          >
            <Select options={platformOptions} placeholder="选择平台" />
          </Form.Item>
          <CustomerSelectField
            form={createForm}
            lastAutofillOrgFullName={lastAutofillOrgFullName}
            setLastAutofillOrgFullName={setLastAutofillOrgFullName}
          />
          {(() => {
            const config = getConfig(createPlatform ?? '');
            const validatorType = config?.codeValidatorType ?? 'NONE';
            const isUppercase = validatorType === 'UPPERCASE';
            const isMixedOrChinese = validatorType === 'MIXED' || validatorType === 'CHINESE';
            const label = isUppercase ? '机构代码 (仅限大写)' : isMixedOrChinese ? '机构代码/简称 (建议汉字)' : '机构代码/简称';
            const placeholder = isUppercase ? '机构代码 (仅限大写字母)' : isMixedOrChinese ? '机构简称 (建议汉字)' : '机构代码/简称（大写字母+汉字）';
            const rules = isUppercase
              ? [{ required: true, message: '请填写机构代码' }, OrgCodeUppercaseRule]
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
          <Form.Item
            name="orgFullName"
            label="机构全称"
            rules={[{ required: true, message: '请输入机构全称' }]}
          >
            <Input placeholder="请输入机构全称" />
          </Form.Item>
          <Form.Item name="contactPerson" label="联系人" rules={[{ required: true, message: '请输入联系人' }]}>
            <Input placeholder="联系人姓名" />
          </Form.Item>
          <Form.Item
            name="contactPhone"
            label="联系电话"
            rules={[{ required: true, message: '请输入联系电话' }, PhoneRule]}
          >
            <SmartInput name="contactPhone" noSpaces placeholder="11位手机号或区号-号码" />
          </Form.Item>
          <Form.Item
            name="contactEmail"
            label="联系邮箱"
            rules={[{ required: true, message: '请输入联系邮箱' }, EmailRule]}
          >
            <SmartInput name="contactEmail" type="email" noSpaces placeholder="联系邮箱" />
          </Form.Item>
          <Form.Item name="region" label="地区">
            <Select options={REGION_OPTIONS} placeholder="请选择地区" allowClear />
          </Form.Item>
          <Form.Item
            name="salesPerson"
            label="申请人"
            rules={[{ required: true, message: '请输入申请人' }]}
          >
            <Input placeholder="申请人" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} placeholder="备注信息" />
          </Form.Item>
          <Form.Item name="isActive" label="启用状态" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>

      <DuplicateCheckModal
        visible={duplicateVisible}
        matches={duplicateMatches}
        platform={pendingOrgValues?.platform ?? ''}
        platformLabel={pendingOrgValues?.platform ? getConfig(pendingOrgValues.platform)?.platformName : undefined}
        currentInput={duplicateCurrentInput ?? undefined}
        title="重复提醒"
        stage="enterprise_direct"
        violatesHouseRules={(() => {
          const platformCode = pendingOrgValues?.platform ?? '';
          const config = getConfig(platformCode);
          const reason = (h: DuplicateMatchItem) => h.matchReason ?? '';
          const violationEmail = !!config?.ruleUniqueEmail && duplicateMatches.some((h) => reason(h).includes('邮箱'));
          const violationPhone = !!config?.ruleUniquePhone && duplicateMatches.some((h) => reason(h).includes('电话'));
          const violationName = !!config?.ruleUniqueOrgName && duplicateMatches.some((h) => reason(h).includes('全称'));
          return violationEmail || violationPhone || violationName;
        })()}
        onCancel={handleDuplicateReturn}
        renderFooter={(_close, ctx) => [
          <Button key="return" onClick={handleDuplicateReturn}>
            返回编辑
          </Button>,
          !ctx.violatesHouseRules && (
            <Button key="confirm" type="primary" onClick={handleDuplicateConfirmAdd} style={{ marginLeft: 8 }}>
              确认新增
            </Button>
          ),
          ctx.selectedHitCanReuse && (
            <Button key="reuse" onClick={() => message.info('功能暂留：申请复用')} style={{ marginLeft: 8 }}>
              申请复用
            </Button>
          ),
        ].filter(Boolean)}
      />

      <Modal
        title="编辑平台机构"
        open={editOpen}
        destroyOnClose
        onCancel={() => {
          setEditOpen(false);
          setCurrentRow(null);
        }}
        onOk={handleEdit}
        okText="保存"
      >
        {currentRow && (() => {
          const statusLabel = getPlatformStatusLabelForOrg(currentRow);
          const color = statusLabel === '已删除' ? 'error' : statusLabel === '已欠费' ? 'warning' : statusLabel === '已禁用' ? 'default' : 'green';
          return (
            <div style={{ marginBottom: 16 }}>
              当前状态： <Tag color={color}>{statusLabel}</Tag>
            </div>
          );
        })()}
        <Form form={editForm} layout="vertical">
          <Form.Item name="platform" label="平台" rules={[{ required: true }]}>
            <Select options={platformOptions} disabled />
          </Form.Item>
          <Form.Item name="customerIds" label="所属客户">
            <Select
              mode="multiple"
              options={editCustomerOptions}
              placeholder="选择关联客户（可多选）"
              showSearch
              optionFilterProp="label"
              allowClear
              filterOption={(input, option) =>
                (option?.label ?? '').toString().toLowerCase().includes((input ?? '').toLowerCase())
              }
            />
          </Form.Item>
          <Form.Item name="orgCodeShort" label="机构代码/简称" rules={[{ required: true }]}>
            <Input disabled />
          </Form.Item>
          <Form.Item
            name="orgFullName"
            label="机构全称"
            rules={[{ required: true, message: '请输入机构全称' }]}
          >
            <Input placeholder="请输入机构全称" />
          </Form.Item>
          <Form.Item name="contactPerson" label="联系人">
            <Input placeholder="联系人姓名" />
          </Form.Item>
          <Form.Item name="contactPhone" label="联系电话">
            <Input placeholder="联系电话" />
          </Form.Item>
          <Form.Item name="contactEmail" label="联系邮箱">
            <Input placeholder="联系邮箱" />
          </Form.Item>
          <Form.Item name="region" label="地区">
            <Select options={REGION_OPTIONS} placeholder="请选择地区" allowClear />
          </Form.Item>
          <Form.Item name="salesPerson" label="销售负责人">
            <Input placeholder="销售负责人" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} placeholder="备注信息" />
          </Form.Item>
          <Form.Item name="isActive" label="启用状态" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="机构详情"
        open={detailOpen}
        onCancel={() => { setDetailOpen(false); setCurrentRow(null); }}
        footer={null}
        width={560}
        destroyOnClose
      >
        {currentRow && (
          <ProDescriptions column={1} dataSource={currentRow}>
            <ProDescriptions.Item dataIndex="platform" label="平台" render={() => getConfig(currentRow.platform)?.platformName ?? currentRow.platform} />
            <ProDescriptions.Item dataIndex="orgCodeShort" label="机构代码/简称" />
            <ProDescriptions.Item dataIndex="orgFullName" label="机构全称" />
            <ProDescriptions.Item
              label="所属客户"
              render={() => {
                const list = currentRow.linkedCustomers ?? [];
                if (list.length === 0) return '—';
                return (
                  <>
                    {list.map((c) => (
                      <Tag key={c.id} color="blue" style={{ marginRight: 4, marginBottom: 4 }}>
                        {c.name}
                      </Tag>
                    ))}
                  </>
                );
              }}
            />
            <ProDescriptions.Item dataIndex="contactPerson" label="联系人" render={(_, __) => currentRow.contactPerson ?? '—'} />
            <ProDescriptions.Item dataIndex="contactPhone" label="联系电话" render={(_, __) => currentRow.contactPhone ?? '—'} />
            <ProDescriptions.Item dataIndex="contactEmail" label="邮箱" render={(_, __) => currentRow.contactEmail ?? '—'} />
            <ProDescriptions.Item dataIndex="region" label="区域" render={(_, __) => currentRow.region ?? '—'} />
            <ProDescriptions.Item dataIndex="salesPerson" label="销售负责人" render={(_, __) => currentRow.salesPerson ?? '—'} />
            <ProDescriptions.Item
              dataIndex="status"
              label="状态"
              render={() => {
                const label = getPlatformStatusLabelForOrg(currentRow);
                const color = label === '已删除' ? 'red' : label === '已欠费' ? 'orange' : label === '已禁用' ? 'default' : 'green';
                return <Tag color={color}>{label}</Tag>;
              }}
            />
            <ProDescriptions.Item dataIndex="remark" label="备注" render={(_, __) => currentRow.remark ?? '—'} />
            <ProDescriptions.Item label="机构来源" render={() => {
              const info = currentRow.sourceInfo;
              if (!info) return <Tag color="default">历史数据</Tag>;
              if (info.type === 'LEGACY') return <Tag color="default">历史数据</Tag>;
              if (info.type === 'MANUAL') return <Tag color="default">管理员创建</Tag>;
              if (info.type === 'SALES') return <Tag color="blue">销售申请</Tag>;
              if (info.type === 'SERVICE') return <Tag color="orange">服务申请</Tag>;
              return <Tag color="default">{info.type}</Tag>;
            }} span={1} />
            {currentRow.sourceInfo && (currentRow.sourceInfo.type === 'MANUAL' || currentRow.sourceInfo.type === 'SALES' || currentRow.sourceInfo.type === 'SERVICE') && (
              <>
                {currentRow.sourceInfo.type === 'MANUAL' && currentRow.sourceInfo.adminName != null && (
                  <ProDescriptions.Item label="管理员" render={() => currentRow.sourceInfo?.adminName ?? '—'} />
                )}
                {(currentRow.sourceInfo.type === 'SALES' || currentRow.sourceInfo.type === 'SERVICE') && (
                  <>
                    <ProDescriptions.Item label="审批单号" render={() => currentRow.sourceInfo?.applicationNo ?? '—'} />
                    <ProDescriptions.Item label="申请人" render={() => currentRow.sourceInfo?.applicantName ?? '—'} />
                    {currentRow.sourceInfo.type === 'SALES' && (
                      <ProDescriptions.Item label="营企处理人" render={() => currentRow.sourceInfo?.plannerName ?? '—'} />
                    )}
                    <ProDescriptions.Item label="审批人" render={() => currentRow.sourceInfo?.adminName ?? '—'} />
                  </>
                )}
              </>
            )}
            <ProDescriptions.Item dataIndex="createdAt" label="创建时间" valueType="dateTime" />
            <ProDescriptions.Item dataIndex="updatedAt" label="更新时间" valueType="dateTime" />
          </ProDescriptions>
        )}
      </Modal>
    </div>
  );
}
