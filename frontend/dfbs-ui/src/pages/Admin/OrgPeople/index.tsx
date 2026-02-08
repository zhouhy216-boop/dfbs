import { useEffect, useState } from 'react';
import { Button, Form, Input, message, Modal, Select, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  searchPeople,
  getPerson,
  createPerson,
  updatePerson,
  disablePerson,
  enablePerson,
  listJobLevels,
  getOrgTree,
  type OrgPersonItem,
  type JobLevelItem,
  type OrgTreeNode,
} from '@/features/orgstructure/services/orgStructure';

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string } }; message?: string };
  message.error(err?.response?.data?.message ?? err?.message ?? '操作失败');
}

function flattenTree(nodes: OrgTreeNode[], out: { id: number; name: string; path: string }[] = [], path = ''): { id: number; name: string; path: string }[] {
  for (const n of nodes) {
    const p = path ? `${path} / ${n.name}` : n.name;
    out.push({ id: n.id, name: n.name, path: p });
    if (n.children?.length) flattenTree(n.children, out, p);
  }
  return out;
}

export default function OrgPeoplePage() {
  const [data, setData] = useState<OrgPersonItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [primaryOrgId, setPrimaryOrgId] = useState<number | undefined>(undefined);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [jobLevels, setJobLevels] = useState<JobLevelItem[]>([]);
  const [orgOptions, setOrgOptions] = useState<{ id: number; name: string; path: string }[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm<{
    name: string;
    phone: string;
    email?: string;
    remark?: string;
    jobLevelId: number;
    primaryOrgNodeId: number;
    secondaryOrgNodeIds?: number[];
  }>();

  const fetchList = async () => {
    setLoading(true);
    try {
      const res = await searchPeople({ keyword: keyword || undefined, primaryOrgId, page, size: pageSize });
      setData(res.content ?? []);
      setTotal(res.totalElements ?? 0);
    } catch (e) {
      showError(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();
  }, [keyword, primaryOrgId, page, pageSize]);

  useEffect(() => {
    listJobLevels().then(setJobLevels).catch(() => setJobLevels([]));
    getOrgTree(false).then((tree) => setOrgOptions(flattenTree(tree))).catch(() => setOrgOptions([]));
  }, []);

  const openCreate = () => {
    setEditingId(null);
    form.setFieldsValue({
      name: '',
      phone: '',
      email: '',
      remark: '',
      jobLevelId: jobLevels[0]?.id,
      primaryOrgNodeId: undefined,
      secondaryOrgNodeIds: [],
    });
    setModalOpen(true);
  };

  const openEdit = async (id: number) => {
    setEditingId(id);
    try {
      const row = await getPerson(id);
      form.setFieldsValue({
        name: row.name,
        phone: row.phone,
        email: row.email ?? '',
        remark: row.remark ?? '',
        jobLevelId: row.jobLevelId,
        primaryOrgNodeId: row.primaryOrgNodeId ?? undefined,
        secondaryOrgNodeIds: row.secondaryOrgNodeIds ?? [],
      });
      setModalOpen(true);
    } catch (e) {
      showError(e);
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setSubmitting(true);
      if (editingId == null) {
        await createPerson({
          name: values.name?.trim() ?? '',
          phone: values.phone?.trim() ?? '',
          email: values.email?.trim() || undefined,
          remark: values.remark?.trim() || undefined,
          jobLevelId: values.jobLevelId,
          primaryOrgNodeId: values.primaryOrgNodeId,
          secondaryOrgNodeIds: values.secondaryOrgNodeIds ?? [],
        });
        message.success('新建成功');
      } else {
        await updatePerson(editingId, {
          name: values.name?.trim(),
          phone: values.phone?.trim(),
          email: values.email?.trim() || undefined,
          remark: values.remark?.trim() || undefined,
          jobLevelId: values.jobLevelId,
          primaryOrgNodeId: values.primaryOrgNodeId,
          secondaryOrgNodeIds: values.secondaryOrgNodeIds ?? [],
        });
        message.success('更新成功');
      }
      setModalOpen(false);
      fetchList();
    } catch (e) {
      if (e instanceof Error && e.message?.includes('validateFields')) return;
      showError(e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDisable = (row: OrgPersonItem) => {
    Modal.confirm({
      title: '确认停用',
      content: `确定停用「${row.name}」？`,
      onOk: async () => {
        try {
          await disablePerson(row.id);
          message.success('已停用');
          fetchList();
        } catch (e) {
          showError(e);
        }
      },
    });
  };

  const handleEnable = async (row: OrgPersonItem) => {
    try {
      await enablePerson(row.id);
      message.success('已启用');
      fetchList();
    } catch (e) {
      showError(e);
    }
  };

  const columns: ColumnsType<OrgPersonItem> = [
    { title: '姓名', dataIndex: 'name', width: 100 },
    { title: '手机', dataIndex: 'phone', width: 120 },
    { title: '邮箱', dataIndex: 'email', ellipsis: true },
    { title: '主归属', dataIndex: 'primaryOrgNodeId', width: 100, render: (id) => orgOptions.find((o) => o.id === id)?.name ?? id },
    { title: '状态', dataIndex: 'isActive', width: 70, render: (v: boolean) => (v ? '在岗' : '停用') },
    {
      title: '操作',
      key: 'action',
      width: 160,
      render: (_, row) => (
        <Space>
          <Button type="link" size="small" onClick={() => openEdit(row.id)}>
            编辑
          </Button>
          {row.isActive ? (
            <Button type="link" size="small" danger onClick={() => handleDisable(row)}>
              停用
            </Button>
          ) : (
            <Button type="link" size="small" onClick={() => handleEnable(row)}>
              启用
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Space style={{ marginBottom: 16 }} wrap>
        <Input.Search
          placeholder="姓名/手机/邮箱"
          allowClear
          onSearch={setKeyword}
          style={{ width: 200 }}
        />
        <Select
          placeholder="主归属组织"
          allowClear
          style={{ width: 220 }}
          options={orgOptions.map((o) => ({ label: o.path, value: o.id }))}
          value={primaryOrgId}
          onChange={setPrimaryOrgId}
        />
        <Button type="primary" onClick={openCreate}>
          新建人员
        </Button>
      </Space>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={data}
        loading={loading}
        pagination={{
          current: page,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (t) => `共 ${t} 条`,
          onChange: (p, ps) => {
            setPage(p);
            if (ps) setPageSize(ps);
          },
        }}
      />
      <Modal
        title={editingId == null ? '新建人员' : '编辑人员'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        confirmLoading={submitting}
        width={520}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="姓名" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="手机" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item name="jobLevelId" label="职级" rules={[{ required: true }]}>
            <Select
              placeholder="选择职级"
              options={jobLevels.map((j) => ({ label: j.displayName, value: j.id }))}
            />
          </Form.Item>
          <Form.Item name="primaryOrgNodeId" label="主归属组织" rules={[{ required: true }]}>
            <Select
              placeholder="选择组织"
              showSearch
              optionFilterProp="label"
              options={orgOptions.map((o) => ({ label: o.path, value: o.id }))}
            />
          </Form.Item>
          <Form.Item name="secondaryOrgNodeIds" label="次要归属（可多选）">
            <Select
              mode="multiple"
              placeholder="选填"
              allowClear
              options={orgOptions.map((o) => ({ label: o.path, value: o.id }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
