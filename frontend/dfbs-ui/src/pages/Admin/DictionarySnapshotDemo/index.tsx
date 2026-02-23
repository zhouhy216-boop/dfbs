import { useEffect, useState } from 'react';
import { Button, Form, Input, message, Select, Table } from 'antd';
import dayjs from 'dayjs';
import { getDictionaryItems } from '@/features/dicttype/services/dictRead';
import {
  createSnapshotDemoRecord,
  listSnapshotDemoRecords,
  type DictSnapshotDemoRecord,
} from '@/features/dicttype/services/dictSnapshotDemo';

const DEFAULT_TYPE_CODE = 'payment_method';

export default function DictionarySnapshotDemoPage() {
  const [typeCode, setTypeCode] = useState(DEFAULT_TYPE_CODE);
  const [itemOptions, setItemOptions] = useState<{ value: string; label: string }[]>([]);
  const [itemOptionsLoading, setItemOptionsLoading] = useState(false);
  const [note, setNote] = useState('');
  const [selectedItemValue, setSelectedItemValue] = useState<string | null>(null);
  const [records, setRecords] = useState<DictSnapshotDemoRecord[]>([]);
  const [listLoading, setListLoading] = useState(false);
  const [listError, setListError] = useState<string | null>(null);
  const [saveLoading, setSaveLoading] = useState(false);

  const loadOptions = async () => {
    if (!typeCode?.trim()) {
      setItemOptions([]);
      return;
    }
    setItemOptionsLoading(true);
    try {
      const res = await getDictionaryItems(typeCode.trim(), { includeDisabled: false });
      setItemOptions(
        (res.items ?? []).map((i) => ({ value: i.value, label: i.label }))
      );
    } catch {
      setItemOptions([]);
    } finally {
      setItemOptionsLoading(false);
    }
  };

  useEffect(() => {
    loadOptions();
  }, [typeCode]);

  const loadRecords = async () => {
    setListLoading(true);
    setListError(null);
    try {
      const res = await listSnapshotDemoRecords({ page: 0, size: 100 });
      setRecords(res.content ?? []);
    } catch {
      setListError('加载失败，请重试');
      setRecords([]);
    } finally {
      setListLoading(false);
    }
  };

  useEffect(() => {
    loadRecords();
  }, []);

  const handleSave = async () => {
    if (!typeCode?.trim()) {
      message.warning('请输入字典类型');
      return;
    }
    const itemValue = selectedItemValue?.trim();
    if (!itemValue) {
      message.warning('请选择字典项');
      return;
    }
    setSaveLoading(true);
    try {
      await createSnapshotDemoRecord({
        typeCode: typeCode.trim(),
        itemValue,
        note: note.trim() || undefined,
      });
      message.success('保存成功');
      setNote('');
      setSelectedItemValue(null);
      loadRecords();
    } catch {
      message.error('保存失败，请重试');
    } finally {
      setSaveLoading(false);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <h2 style={{ marginBottom: 16 }}>历史显示示例</h2>

      <div style={{ marginBottom: 24 }}>
        <Form layout="inline" style={{ gap: 8, flexWrap: 'wrap' }}>
          <Form.Item label="字典类型(typeCode)">
            <Input
              value={typeCode}
              onChange={(e) => setTypeCode(e.target.value ?? '')}
              placeholder="payment_method"
              style={{ width: 180 }}
            />
          </Form.Item>
          <Form.Item label="字典项">
            <Select
              placeholder="请选择"
              value={selectedItemValue ?? undefined}
              onChange={(v) => setSelectedItemValue(v ?? null)}
              options={itemOptions}
              loading={itemOptionsLoading}
              style={{ width: 200 }}
              allowClear
            />
          </Form.Item>
          <Form.Item label="备注">
            <Input
              value={note}
              onChange={(e) => setNote(e.target.value ?? '')}
              placeholder="可选"
              style={{ width: 160 }}
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" onClick={handleSave} loading={saveLoading}>
              保存示例记录
            </Button>
          </Form.Item>
        </Form>
      </div>

      <div>
        {listError && (
          <div style={{ color: '#ff4d4f', marginBottom: 8 }}>{listError}</div>
        )}
        <Table<DictSnapshotDemoRecord>
          rowKey="id"
          loading={listLoading}
          dataSource={records}
          columns={[
            {
              title: '保存时标签',
              dataIndex: 'itemLabelSnapshot',
              key: 'itemLabelSnapshot',
              width: 140,
              render: (val: string) => val ?? '—',
            },
            {
              title: '保存时值',
              dataIndex: 'itemValue',
              key: 'itemValue',
              width: 120,
            },
            {
              title: '字典类型',
              dataIndex: 'typeCode',
              key: 'typeCode',
              width: 120,
            },
            {
              title: '创建时间',
              dataIndex: 'createdAt',
              key: 'createdAt',
              width: 180,
              render: (v: string) =>
                v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '—',
            },
            {
              title: '备注',
              dataIndex: 'note',
              key: 'note',
              ellipsis: true,
              render: (v: string | null) => v ?? '—',
            },
          ]}
          pagination={false}
          locale={{ emptyText: '暂无记录' }}
        />
      </div>
    </div>
  );
}
