import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Form, Input, message, Modal, Select, Switch, Tag } from 'antd';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import dayjs from 'dayjs';
import {
  listDictTypes,
  createDictType,
  updateDictType,
  enableDictType,
  disableDictType,
  type DictTypeItem,
  type CreateDictTypeRequest,
  type UpdateDictTypeRequest,
} from '@/features/dicttype/services/dictType';

const DICT_TYPE_CODE_EXISTS = 'DICT_TYPE_CODE_EXISTS';

function showError(e: unknown, customMessage?: string) {
  const err = e as { response?: { data?: { message?: string; machineCode?: string } }; message?: string };
  const code = err.response?.data?.machineCode;
  if (code === DICT_TYPE_CODE_EXISTS) {
    message.error(customMessage ?? '编码已存在，请换一个');
    return;
  }
  message.error(err?.response?.data?.message ?? err?.message ?? customMessage ?? '操作失败');
}

export default function DictionaryTypesPage() {
  const navigate = useNavigate();
  const actionRef = useRef<ActionType>(null);
  const searchQRef = useRef('');
  const enabledFilterRef = useRef<'all' | boolean>('all');
  const [enabledFilter, setEnabledFilter] = useState<'all' | boolean>('all');
  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<DictTypeItem | null>(null);
  const [form] = Form.useForm<CreateDictTypeRequest & { id?: number }>();

  const runQuery = () => {
    actionRef.current?.reload();
  };

  const openCreate = () => {
    setEditingItem(null);
    form.resetFields();
    form.setFieldsValue({ typeCode: '', typeName: '', description: '', enabled: true });
    setModalOpen(true);
  };

  const openEdit = (row: DictTypeItem) => {
    setEditingItem(row);
    form.resetFields();
    form.setFieldsValue({
      typeCode: row.typeCode,
      typeName: row.typeName,
      description: row.description ?? '',
      enabled: row.enabled,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingItem) {
        const body: UpdateDictTypeRequest = {
          typeName: values.typeName?.trim(),
          description: values.description?.trim() || null,
          enabled: values.enabled,
        };
        await updateDictType(editingItem.id, body);
        message.success('更新成功');
      } else {
        const body: CreateDictTypeRequest = {
          typeCode: (values.typeCode ?? '').toString().trim(),
          typeName: (values.typeName ?? '').toString().trim(),
          description: values.description?.trim() || undefined,
          enabled: values.enabled ?? true,
        };
        await createDictType(body);
        message.success('添加成功');
      }
      setModalOpen(false);
      actionRef.current?.reload();
    } catch (e) {
      if ((e as { errorFields?: unknown[] })?.errorFields) return;
      showError(e);
    }
  };

  const handleDisable = (row: DictTypeItem) => {
    Modal.confirm({
      title: '确认禁用',
      content: '确认禁用该字典类型？',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          await disableDictType(row.id);
          message.success('已禁用');
          actionRef.current?.reload();
        } catch (err) {
          showError(err);
        }
      },
    });
  };

  const handleEnable = (row: DictTypeItem) => {
    Modal.confirm({
      title: '确认启用',
      content: '确认启用该字典类型？',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          await enableDictType(row.id);
          message.success('已启用');
          actionRef.current?.reload();
        } catch (err) {
          showError(err);
        }
      },
    });
  };

  const columns: ProColumns<DictTypeItem>[] = [
    { title: '编码', dataIndex: 'typeCode', width: 140, ellipsis: true },
    { title: '名称', dataIndex: 'typeName', width: 140, ellipsis: true },
    { title: '描述', dataIndex: 'description', ellipsis: true, render: (v) => v ?? '—' },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 90,
      render: (_, row) => (row.enabled ? <Tag color="green">启用</Tag> : <Tag color="default">禁用</Tag>),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      width: 160,
      render: (_: unknown, row: DictTypeItem) => (row.updatedAt ? dayjs(row.updatedAt).format('YYYY-MM-DD HH:mm') : '—'),
    },
    {
      title: '操作',
      valueType: 'option',
      width: 200,
      fixed: 'right',
      render: (_, row) => [
        <Button
          key="items"
          type="link"
          size="small"
          onClick={() =>
            navigate(`/admin/dictionary-types/${row.id}/items`, {
              state: { typeCode: row.typeCode, typeName: row.typeName },
            })
          }
        >
          字典项
        </Button>,
        <Button key="edit" type="link" size="small" onClick={() => openEdit(row)}>
          编辑
        </Button>,
        row.enabled ? (
          <Button key="disable" type="link" size="small" danger onClick={() => handleDisable(row)}>
            禁用
          </Button>
        ) : (
          <Button key="enable" type="link" size="small" onClick={() => handleEnable(row)}>
            启用
          </Button>
        ),
      ],
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <ProTable<DictTypeItem>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={async ({ current = 1, pageSize = 20 }) => {
          const q = searchQRef.current?.trim() || undefined;
          const en = enabledFilterRef.current === 'all' ? undefined : enabledFilterRef.current;
          const res = await listDictTypes({
            page: (current ?? 1) - 1,
            pageSize: pageSize ?? 20,
            q,
            enabled: en,
          });
          return { data: res.items, success: true, total: res.total };
        }}
        search={false}
        pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        toolBarRender={() => [
          <Input.Search
            key="search"
            placeholder="搜索：编码/名称"
            allowClear
            style={{ width: 200, marginRight: 8 }}
            onSearch={(v) => {
              searchQRef.current = v ?? '';
              runQuery();
            }}
          />,
          <Select
            key="enabled"
            placeholder="状态"
            value={enabledFilter}
            onChange={(v) => {
              enabledFilterRef.current = v;
              setEnabledFilter(v);
              runQuery();
            }}
            style={{ width: 100, marginRight: 8 }}
            options={[
              { label: '全部', value: 'all' },
              { label: '启用', value: true },
              { label: '禁用', value: false },
            ]}
          />,
          <Button key="add" type="primary" onClick={openCreate}>
            新建字典类型
          </Button>,
        ]}
        headerTitle="字典类型"
      />

      <Modal
        title={editingItem ? '编辑字典类型' : '新建字典类型'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        okText="保存"
        destroyOnClose
        width={480}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="typeCode"
            label="编码"
            rules={[{ required: !editingItem, message: '请输入编码' }]}
          >
            <Input placeholder="字母、数字、下划线、连字符" disabled={!!editingItem} />
          </Form.Item>
          <Form.Item
            name="typeName"
            label="名称"
            rules={[{ required: true, message: '请输入名称' }]}
          >
            <Input placeholder="显示名称" />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={2} placeholder="选填" />
          </Form.Item>
          <Form.Item name="enabled" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
