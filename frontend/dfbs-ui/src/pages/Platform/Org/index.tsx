import { useEffect, useMemo, useRef, useState } from 'react';
import { Button, Form, Input, message, Modal, Select, Switch, Tag } from 'antd';
import type { FormInstance } from 'antd/es/form';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import type { AxiosError } from 'axios';
import request from '@/utils/request';
import SmartReferenceSelect from '@/components/SmartReferenceSelect';
import type { SpringPage } from '@/utils/adapters';

type PlatformType = 'INHAND' | 'HENDONG' | 'JINGPIN' | 'OTHER';

interface LinkedCustomer {
  id: number;
  name: string;
}

interface PlatformOrgRow {
  id: number;
  platform: PlatformType;
  orgCodeShort: string;
  orgFullName: string;
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
  createdAt?: string;
  updatedAt?: string;
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
}

const PLATFORM_LABELS: Record<PlatformType, string> = {
  INHAND: '映翰通',
  HENDONG: '恒动',
  JINGPIN: '京品',
  OTHER: '其他',
};

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

function CustomerSelectField({
  form,
  disabled,
}: {
  form: FormInstance<PlatformOrgFormValues>;
  disabled?: boolean;
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
  const [currentRow, setCurrentRow] = useState<PlatformOrgRow | null>(null);
  const [editCustomerOptions, setEditCustomerOptions] = useState<{ label: string; value: number }[]>([]);
  const [createForm] = Form.useForm<PlatformOrgFormValues>();
  const [editForm] = Form.useForm<PlatformOrgFormValues>();

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

  const platformOptions = useMemo(
    () => Object.entries(PLATFORM_LABELS).map(([value, label]) => ({ label, value })),
    [],
  );

  const handleToggleActive = async (row: PlatformOrgRow, checked: boolean) => {
    try {
      await request.put(`/v1/platform-orgs/${row.id}`, {
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
        isActive: checked,
      });
      message.success('状态已更新');
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
    setCreateOpen(true);
  };

  const handleCreate = async () => {
    try {
      const values = await createForm.validateFields();
      await request.post('/v1/platform-orgs', {
        platform: values.platform,
        orgCodeShort: values.orgCodeShort,
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

  const columns: ProColumns<PlatformOrgRow>[] = [
    {
      title: '平台',
      dataIndex: 'platform',
      width: 120,
      valueType: 'select',
      valueEnum: Object.fromEntries(
        Object.entries(PLATFORM_LABELS).map(([key, label]) => [key, { text: label }]),
      ),
      render: (_, row) => <Tag color="blue">{PLATFORM_LABELS[row.platform]}</Tag>,
    },
    {
      title: '机构代码/简称',
      dataIndex: 'orgCodeShort',
      width: 160,
    },
    {
      title: '机构全称',
      dataIndex: 'orgFullName',
      width: 200,
      ellipsis: true,
    },
    {
      title: '所属客户',
      dataIndex: 'linkedCustomers',
      width: 260,
      render: (_, row) => {
        const list = row.linkedCustomers ?? [];
        if (list.length === 0) return <span style={{ color: '#999' }}>—</span>;
        return (
          <>
            {list.map((c) => (
              <Tag key={c.id} color="blue" style={{ marginBottom: 4 }}>
                {c.name}
              </Tag>
            ))}
          </>
        );
      },
    },
    {
      title: '区域',
      dataIndex: 'region',
      width: 160,
      search: false,
      ellipsis: true,
    },
    {
      title: '销售负责人',
      dataIndex: 'salesPerson',
      width: 160,
      search: false,
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'isActive',
      width: 120,
      valueType: 'select',
      valueEnum: {
        true: { text: '启用' },
        false: { text: '停用' },
      },
      render: (_, row) => (
        <Switch
          checked={row.isActive ?? true}
          onChange={(checked) => handleToggleActive(row, checked)}
          size="small"
        />
      ),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      width: 200,
      search: false,
      ellipsis: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      valueType: 'dateTime',
      width: 180,
      search: false,
    },
    {
      title: '操作',
      valueType: 'option',
      width: 120,
      fixed: 'right',
      render: (_, row) => [
        <a
          key="edit"
          onClick={() => {
            setCurrentRow(row);
            editForm.setFieldsValue({
              ...row,
              orgCodeShort: row.orgCodeShort ?? row.orgCode,
              orgFullName: row.orgFullName ?? row.orgName,
              customerIds: row.customerIds ?? (row.customerId != null ? [row.customerId] : []),
              isActive: row.isActive ?? true,
            });
            setEditOpen(true);
          }}
        >
          编辑
        </a>,
      ],
    },
    {
      title: '客户筛选',
      dataIndex: 'customerId',
      hideInTable: true,
      renderFormItem: (_, { form }) => {
        const formInstance = form as FormInstance<PlatformOrgFormValues & { customerNameSearch?: string }>;
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
          <Button key="create" type="primary" onClick={openCreateModal}>
            新建机构
          </Button>,
        ]}
      />

      <Modal
        title="新建平台机构"
        open={createOpen}
        destroyOnClose
        onCancel={() => setCreateOpen(false)}
        onOk={handleCreate}
        okText="提交"
      >
        <Form form={createForm} layout="vertical">
          <Form.Item
            name="platform"
            label="平台"
            rules={[{ required: true, message: '请选择平台' }]}
          >
            <Select options={platformOptions} placeholder="选择平台" />
          </Form.Item>
          <CustomerSelectField form={createForm} />
          <Form.Item
            name="orgCodeShort"
            label="机构代码/简称"
            rules={[{ required: true, message: '请输入机构代码/简称' }]}
          >
            <Input placeholder="请输入机构代码/简称" />
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
            <Input placeholder="地区" />
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
            <Input placeholder="地区" />
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
    </div>
  );
}
