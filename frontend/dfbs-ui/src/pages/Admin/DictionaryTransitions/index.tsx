import { useState, useEffect, useCallback } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { Button, Collapse, Form, message, Select, Switch, Table, Tag } from 'antd';
import {
  listItems,
  type DictItem,
} from '@/features/dicttype/services/dictItem';
import {
  listTransitionsAdmin,
  upsertTransitionsAdmin,
  getTransitionsRead,
  type TransitionsReadResponse,
} from '@/features/dicttype/services/dictTransition';

type LocationState = { typeCode?: string; typeName?: string } | null;

/** Local row for editing (fromValue, toValue, enabled, labels for display). */
type TransitionRow = {
  id?: number;
  fromValue: string;
  toValue: string;
  fromLabel?: string;
  toLabel?: string;
  enabled: boolean;
};

export default function DictionaryTransitionsPage() {
  const { typeId } = useParams<{ typeId: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const state = (location.state as LocationState) ?? {};
  const typeIdNum = typeId ? parseInt(typeId, 10) : NaN;

  const [items, setItems] = useState<DictItem[]>([]);
  const [transitions, setTransitions] = useState<TransitionRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [readPreview, setReadPreview] = useState<TransitionsReadResponse | null>(null);
  const [readPreviewLoading, setReadPreviewLoading] = useState(false);
  const [addForm] = Form.useForm<{ fromValue: string; toValue: string; enabled: boolean }>();

  const typeCode = state?.typeCode;
  const typeName = state?.typeName ?? state?.typeCode ?? (typeId ? `ID: ${typeId}` : '');

  const loadItems = useCallback(async () => {
    if (!typeIdNum || isNaN(typeIdNum)) return;
    try {
      const res = await listItems(typeIdNum, { page: 0, pageSize: 500, enabled: true });
      setItems(res.items);
    } catch {
      setItems([]);
    }
  }, [typeIdNum]);

  const loadTransitions = useCallback(async () => {
    if (!typeIdNum || isNaN(typeIdNum)) return;
    setLoading(true);
    try {
      const res = await listTransitionsAdmin(typeIdNum);
      setTransitions(
        res.transitions.map((t) => ({
          id: t.id,
          fromValue: t.fromValue,
          toValue: t.toValue,
          fromLabel: t.fromLabel,
          toLabel: t.toLabel,
          enabled: t.enabled,
        }))
      );
    } catch (e) {
      message.error((e as Error)?.message ?? '加载迁移规则失败');
      setTransitions([]);
    } finally {
      setLoading(false);
    }
  }, [typeIdNum]);

  useEffect(() => {
    loadItems();
  }, [loadItems]);

  useEffect(() => {
    loadTransitions();
  }, [loadTransitions]);

  const itemOptions = items.map((i) => ({ label: `${i.itemLabel} (${i.itemValue})`, value: i.itemValue }));

  const hasDuplicate = (fromValue: string, toValue: string, excludeId?: number) =>
    transitions.some(
      (t) =>
        t.fromValue === fromValue &&
        t.toValue === toValue &&
        (excludeId == null || t.id !== excludeId)
    );

  const handleAdd = () => {
    addForm.validateFields().then((values) => {
      const from = (values.fromValue ?? '').trim();
      const to = (values.toValue ?? '').trim();
      if (from === to) {
        message.warning('起始与目标不能相同（不允许自环）');
        return;
      }
      if (hasDuplicate(from, to)) {
        message.warning('该迁移规则已存在，请勿重复添加');
        return;
      }
      const fromItem = items.find((i) => i.itemValue === from);
      const toItem = items.find((i) => i.itemValue === to);
      setTransitions((prev) => [
        ...prev,
        {
          fromValue: from,
          toValue: to,
          fromLabel: fromItem?.itemLabel,
          toLabel: toItem?.itemLabel,
          enabled: values.enabled ?? true,
        },
      ]);
      addForm.resetFields();
      addForm.setFieldsValue({ enabled: true });
    }).catch(() => {});
  };

  const setEnabled = (fromValue: string, toValue: string, enabled: boolean) => {
    setTransitions((prev) =>
      prev.map((t) =>
        t.fromValue === fromValue && t.toValue === toValue ? { ...t, enabled } : t
      )
    );
  };

  const handleSave = async () => {
    if (!typeIdNum || isNaN(typeIdNum)) return;
    setSaving(true);
    try {
      const body = {
        transitions: transitions.map((t) => ({
          fromValue: t.fromValue,
          toValue: t.toValue,
          enabled: t.enabled,
        })),
      };
      await upsertTransitionsAdmin(typeIdNum, body);
      message.success('保存成功');
      await loadTransitions();
    } catch (e) {
      const err = e as { response?: { data?: { message?: string; machineCode?: string } }; message?: string };
      const code = err.response?.data?.machineCode;
      if (code === 'DICT_TRANSITION_SELF_LOOP') {
        message.error('不允许自环（起始与目标相同）');
        return;
      }
      if (code === 'DICT_TRANSITION_ITEM_NOT_FOUND') {
        message.error(err.response?.data?.message ?? '字典项不存在');
        return;
      }
      message.error(err?.response?.data?.message ?? (err as Error)?.message ?? '保存失败');
    } finally {
      setSaving(false);
    }
  };

  const loadReadPreview = () => {
    if (!typeCode?.trim()) {
      message.info('请从字典类型列表进入本页以使用读取预览');
      return;
    }
    setReadPreviewLoading(true);
    getTransitionsRead(typeCode, false)
      .then((res) => setReadPreview(res))
      .catch(() => message.error('读取预览失败'))
      .finally(() => setReadPreviewLoading(false));
  };

  return (
    <div style={{ padding: 24 }}>
      <div style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 12 }}>
        <Button type="link" onClick={() => navigate('/admin/dictionary-types')}>
          返回字典类型
        </Button>
        <span style={{ color: '#666' }}>
          {typeName} {typeCode && `(${typeCode})`} — 状态流（迁移规则）
        </span>
      </div>

      <p style={{ color: '#666', marginBottom: 16 }}>
        定义允许的状态迁移边（从 → 到）。禁用后的边在业务侧读取时默认不返回。
      </p>

      <div style={{ marginBottom: 16, display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'flex-start' }}>
        <Form form={addForm} layout="inline" initialValues={{ enabled: true }}>
          <Form.Item name="fromValue" label="起始" rules={[{ required: true, message: '请选择' }]}>
            <Select
              placeholder="起始状态"
              options={itemOptions}
              style={{ width: 180 }}
              allowClear
            />
          </Form.Item>
          <Form.Item name="toValue" label="目标" rules={[{ required: true, message: '请选择' }]}>
            <Select
              placeholder="目标状态"
              options={itemOptions}
              style={{ width: 180 }}
              allowClear
            />
          </Form.Item>
          <Form.Item name="enabled" label="启用" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" onClick={handleAdd}>
              添加
            </Button>
          </Form.Item>
        </Form>
        <Button type="primary" onClick={handleSave} loading={saving}>
          保存
        </Button>
      </div>

      <Table<TransitionRow>
        rowKey={(r) => `${r.fromValue}-${r.toValue}`}
        loading={loading}
        size="small"
        pagination={false}
        dataSource={transitions}
        columns={[
          { title: '起始值', dataIndex: 'fromValue', width: 120 },
          { title: '起始标签', dataIndex: 'fromLabel', width: 140, render: (v) => v ?? '—' },
          { title: '目标值', dataIndex: 'toValue', width: 120 },
          { title: '目标标签', dataIndex: 'toLabel', width: 140, render: (v) => v ?? '—' },
          {
            title: '启用',
            dataIndex: 'enabled',
            width: 80,
            render: (v: boolean) => (v ? <Tag color="green">启用</Tag> : <Tag color="default">禁用</Tag>),
          },
          {
            title: '操作',
            width: 120,
            render: (_, row) =>
              row.enabled ? (
                <Button type="link" size="small" danger onClick={() => setEnabled(row.fromValue, row.toValue, false)}>
                  禁用
                </Button>
              ) : (
                <Button type="link" size="small" onClick={() => setEnabled(row.fromValue, row.toValue, true)}>
                  启用
                </Button>
              ),
          },
        ]}
        locale={{ emptyText: '暂无迁移规则，请在上方添加' }}
      />

      {typeCode && (
        <Collapse
          style={{ marginTop: 24 }}
          items={[
            {
              key: 'read-preview',
              label: '业务读取预览（仅启用边，与 GET /api/v1/dictionaries/{typeCode}/transitions 一致）',
              children: (
                <div>
                  <Button size="small" onClick={loadReadPreview} loading={readPreviewLoading} style={{ marginBottom: 8 }}>
                    刷新预览
                  </Button>
                  {readPreview && (
                    <Table
                      size="small"
                      rowKey={(r) => `${r.fromValue}-${r.toValue}`}
                      pagination={false}
                      dataSource={readPreview.transitions}
                      columns={[
                        { title: '起始值', dataIndex: 'fromValue', width: 120 },
                        { title: '起始标签', dataIndex: 'fromLabel', width: 140 },
                        { title: '目标值', dataIndex: 'toValue', width: 120 },
                        { title: '目标标签', dataIndex: 'toLabel', width: 140 },
                        { title: '启用', dataIndex: 'enabled', width: 80, render: (v: boolean) => (v ? '是' : '否') },
                      ]}
                      locale={{ emptyText: '暂无数据' }}
                    />
                  )}
                </div>
              ),
            },
          ]}
        />
      )}
    </div>
  );
}
