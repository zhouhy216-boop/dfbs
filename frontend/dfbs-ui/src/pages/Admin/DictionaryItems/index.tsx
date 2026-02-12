import { useRef, useState, useEffect } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { Button, Form, Input, InputNumber, message, Modal, Select, Switch, Tag } from 'antd';
import type { ActionType, ProColumns } from '@ant-design/pro-components';
import { ProTable } from '@ant-design/pro-components';
import dayjs from 'dayjs';
import {
  listItems,
  createItem,
  updateItem,
  enableItem,
  disableItem,
  type DictItem,
  type CreateDictItemRequest,
  type UpdateDictItemRequest,
} from '@/features/dicttype/services/dictItem';

const DICT_ITEM_VALUE_EXISTS = 'DICT_ITEM_VALUE_EXISTS';
const DICT_ITEM_PARENT_INVALID = 'DICT_ITEM_PARENT_INVALID';
const DICT_TYPE_NOT_FOUND = 'DICT_TYPE_NOT_FOUND';

function showItemError(e: unknown) {
  const err = e as { response?: { data?: { message?: string; machineCode?: string } }; message?: string };
  const code = err.response?.data?.machineCode;
  if (code === DICT_ITEM_VALUE_EXISTS) {
    message.error('字典项值已存在，请换一个');
    return;
  }
  if (code === DICT_ITEM_PARENT_INVALID) {
    message.error('父级无效（只能选择同类型的根节点）');
    return;
  }
  if (code === DICT_TYPE_NOT_FOUND) {
    message.error('字典类型不存在或已删除');
    return;
  }
  message.error(err?.response?.data?.message ?? err?.message ?? '操作失败，请重试');
}

type LocationState = { typeCode?: string; typeName?: string } | null;

export default function DictionaryItemsPage() {
  const { typeId } = useParams<{ typeId: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const state = (location.state as LocationState) ?? {};
  const typeIdNum = typeId ? parseInt(typeId, 10) : NaN;

  const actionRef = useRef<ActionType>(null);
  const searchQRef = useRef('');
  const enabledFilterRef = useRef<'all' | boolean>('all');
  const parentFilterRef = useRef<number | null | 'all'>(null);
  const lastItemsRef = useRef<DictItem[]>([]);

  const [enabledFilter, setEnabledFilter] = useState<'all' | boolean>('all');
  const [parentFilter, setParentFilter] = useState<number | null | 'all'>(null);
  const [rootItems, setRootItems] = useState<DictItem[]>([]);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<DictItem | null>(null);
  const [form] = Form.useForm<CreateDictItemRequest & { id?: number }>();

  const typeDisplay = state.typeName ?? state.typeCode ?? (typeId ? `ID: ${typeId}` : '');

  useEffect(() => {
    if (!typeIdNum || isNaN(typeIdNum)) return;
    listItems(typeIdNum, { page: 0, pageSize: 500 })
      .then((res) => setRootItems(res.items.filter((i) => i.parentId == null)))
      .catch(() => setRootItems([]));
  }, [typeIdNum]);

  const runQuery = () => {
    actionRef.current?.reload();
  };

  const openCreate = () => {
    setEditingItem(null);
    form.resetFields();
    form.setFieldsValue({
      itemValue: '',
      itemLabel: '',
      note: '',
      sortOrder: 0,
      enabled: true,
      parentId: null,
    });
    setModalOpen(true);
  };

  const openEdit = (row: DictItem) => {
    setEditingItem(row);
    form.resetFields();
    form.setFieldsValue({
      itemValue: row.itemValue,
      itemLabel: row.itemLabel,
      note: row.note ?? '',
      sortOrder: row.sortOrder,
      enabled: row.enabled,
      parentId: row.parentId ?? null,
    });
    setModalOpen(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingItem) {
        const body: UpdateDictItemRequest = {
          itemLabel: values.itemLabel?.trim(),
          sortOrder: values.sortOrder,
          enabled: values.enabled,
          note: values.note?.trim() || null,
          parentId: values.parentId ?? null,
        };
        await updateItem(editingItem.id, body);
        message.success('更新成功');
      } else {
        const body: CreateDictItemRequest = {
          itemValue: (values.itemValue ?? '').toString().trim(),
          itemLabel: (values.itemLabel ?? '').toString().trim(),
          sortOrder: values.sortOrder,
          enabled: values.enabled ?? true,
          note: values.note?.trim() || null,
          parentId: values.parentId ?? null,
        };
        await createItem(typeIdNum, body);
        message.success('添加成功');
      }
      setModalOpen(false);
      runQuery();
      if (rootItems.length < 500) {
        listItems(typeIdNum, { page: 0, pageSize: 500 })
          .then((res) => setRootItems(res.items.filter((i) => i.parentId == null)))
          .catch(() => {});
      }
    } catch (e) {
      if ((e as { errorFields?: unknown[] })?.errorFields) return;
      showItemError(e);
    }
  };

  const handleDisable = (row: DictItem) => {
    Modal.confirm({
      title: '确认禁用',
      content: '确认禁用该字典项？',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          await disableItem(row.id);
          message.success('已禁用');
          runQuery();
        } catch (err) {
          showItemError(err);
        }
      },
    });
  };

  const handleEnable = (row: DictItem) => {
    Modal.confirm({
      title: '确认启用',
      content: '确认启用该字典项？',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        try {
          await enableItem(row.id);
          message.success('已启用');
          runQuery();
        } catch (err) {
          showItemError(err);
        }
      },
    });
  };

  const parentIdToLabel = (parentId: number | null) => {
    if (parentId == null) return '无';
    const p = lastItemsRef.current.find((i) => i.id === parentId) ?? rootItems.find((i) => i.id === parentId);
    return p ? p.itemLabel : `#${parentId}`;
  };

  const columns: ProColumns<DictItem>[] = [
    { title: '值', dataIndex: 'itemValue', width: 120, ellipsis: true },
    { title: '名称', dataIndex: 'itemLabel', width: 140, ellipsis: true },
    { title: '备注', dataIndex: 'note', ellipsis: true, render: (v) => v ?? '—' },
    { title: '排序', dataIndex: 'sortOrder', width: 80 },
    {
      title: '父级',
      dataIndex: 'parentId',
      width: 120,
      render: (_, row) => parentIdToLabel(row.parentId),
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      width: 90,
      render: (_, row) =>
        row.enabled ? <Tag color="green">启用</Tag> : <Tag color="default">禁用</Tag>,
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      width: 160,
      render: (_: unknown, row: DictItem) =>
        row.updatedAt ? dayjs(row.updatedAt).format('YYYY-MM-DD HH:mm') : '—',
    },
    {
      title: '操作',
      valueType: 'option',
      width: 160,
      fixed: 'right',
      render: (_, row) => [
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

  if (!typeId || isNaN(typeIdNum)) {
    return (
      <div style={{ padding: 24 }}>
        <p>无效的类型 ID</p>
        <Button type="link" onClick={() => navigate('/admin/dictionary-types')}>
          返回字典类型
        </Button>
      </div>
    );
  }

  return (
    <div style={{ padding: 24 }}>
      <div style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 16 }}>
        <Button onClick={() => navigate('/admin/dictionary-types')}>返回</Button>
        <span style={{ fontWeight: 600 }}>
          字典项管理
          {typeDisplay ? ` · ${typeDisplay}` : ''}
        </span>
      </div>

      <ProTable<DictItem>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={async ({ current = 1, pageSize = 20 }) => {
          const q = searchQRef.current?.trim() || undefined;
          const en = enabledFilterRef.current === 'all' ? undefined : enabledFilterRef.current;
          const parentId =
            parentFilterRef.current === 'all' || parentFilterRef.current === null
              ? undefined
              : parentFilterRef.current;
          const res = await listItems(typeIdNum, {
            page: (current ?? 1) - 1,
            pageSize: pageSize ?? 20,
            q,
            enabled: en,
            parentId: parentId !== undefined ? parentId : undefined,
          });
          lastItemsRef.current = res.items;
          return { data: res.items, success: true, total: res.total };
        }}
        search={false}
        pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        toolBarRender={() => [
          <Input.Search
            key="search"
            placeholder="搜索：值/名称"
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
          <Select
            key="parent"
            placeholder="父级"
            value={parentFilter === null ? 'all' : parentFilter}
            onChange={(v) => {
              const val = v === 'all' ? 'all' : v;
              parentFilterRef.current = val;
              setParentFilter(val as number | 'all');
              runQuery();
            }}
            style={{ width: 160, marginRight: 8 }}
            options={[
              { label: '全部', value: 'all' },
              ...rootItems.map((r) => ({ label: r.itemLabel, value: r.id })),
            ]}
          />,
          <Button key="add" type="primary" onClick={openCreate}>
            新建字典项
          </Button>,
        ]}
        headerTitle=""
      />

      <Modal
        title={editingItem ? '编辑字典项' : '新建字典项'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        okText="保存"
        destroyOnClose
        width={480}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="itemValue"
            label="值"
            rules={[{ required: !editingItem, message: '请输入值' }]}
          >
            <Input placeholder="字母、数字、下划线、连字符" disabled={!!editingItem} />
          </Form.Item>
          <Form.Item
            name="itemLabel"
            label="名称"
            rules={[{ required: true, message: '请输入名称' }]}
          >
            <Input placeholder="显示名称" />
          </Form.Item>
          <Form.Item name="note" label="备注">
            <Input.TextArea rows={2} placeholder="选填" />
          </Form.Item>
          <Form.Item name="sortOrder" label="排序">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="parentId" label="父级">
            <Select
              allowClear
              placeholder="无（根节点）"
              options={[
                { label: '无', value: null },
                ...rootItems.map((r) => ({ label: r.itemLabel, value: r.id })),
              ]}
            />
          </Form.Item>
          <Form.Item name="enabled" label="状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
