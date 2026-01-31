import { useEffect, useState } from 'react';
import { Drawer, Table, Button, Input, Radio, Space, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import request from '@/utils/request';

export interface BomConflictRow {
  id: number;
  bomId: number;
  rowPartNo: string | null;
  rowName: string | null;
  rowIndex: number | null;
  type: string;
  status: string;
}

interface ConflictDrawerProps {
  bomId: number | null;
  open: boolean;
  onClose: () => void;
  onResolved: () => void;
  onPublishNow?: (bomId: number) => void;
}

const RESOLUTION_OPTIONS = [
  { label: 'A: 保留原名称 (忽略 BOM)', value: 'KEEP_MASTER' },
  { label: 'B: 覆盖原名称 (使用 BOM 名称)', value: 'OVERWRITE_MASTER' },
  { label: 'C: 添加为别名', value: 'ADD_ALIAS' },
];

export default function ConflictDrawer({ bomId, open, onClose, onResolved, onPublishNow }: ConflictDrawerProps) {
  const [conflicts, setConflicts] = useState<BomConflictRow[]>([]);
  const [loading, setLoading] = useState(false);
  const [resolvingId, setResolvingId] = useState<number | null>(null);
  const [resolutionType, setResolutionType] = useState<Record<number, string>>({});
  const [fixNoValue, setFixNoValue] = useState<Record<number, string>>({});

  const fetchConflicts = async () => {
    if (!bomId) return;
    setLoading(true);
    try {
      const { data } = await request.get<BomConflictRow[]>(`/v1/masterdata/model-part-lists/${bomId}/conflicts`);
      setConflicts(Array.isArray(data) ? data : []);
    } catch {
      setConflicts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open && bomId) fetchConflicts();
  }, [open, bomId]);

  const pendingList = conflicts.filter((c) => c.status === 'PENDING');

  async function handleResolve(conflict: BomConflictRow) {
    const type = conflict.type === 'NAME_MISMATCH' ? resolutionType[conflict.id] : 'FIX_NO';
    const customValue = conflict.type === 'MISSING_NO' ? fixNoValue[conflict.id] : undefined;
    if (conflict.type === 'NAME_MISMATCH' && !type) {
      message.warning('请选择解决方式');
      return;
    }
    if (conflict.type === 'MISSING_NO' && !(customValue?.trim())) {
      message.warning('请输入新图号');
      return;
    }
    setResolvingId(conflict.id);
    try {
      await request.post(`/v1/masterdata/model-part-lists/conflicts/${conflict.id}/resolve`, {
        resolutionType: type ?? 'KEEP_MASTER',
        customValue: customValue?.trim() || null,
      });
      message.success('已解决');
      setResolutionType((s) => ({ ...s, [conflict.id]: undefined }));
      setFixNoValue((s) => ({ ...s, [conflict.id]: '' }));
      await fetchConflicts();
      onResolved();
    } catch (e: unknown) {
      message.error((e as { message?: string })?.message ?? '解决失败');
    } finally {
      setResolvingId(null);
    }
  }

  function conflictTypeText(type: string) {
    const map: Record<string, string> = {
      NAME_MISMATCH: '名称不一致',
      MISSING_NO: '缺图号',
    };
    return map[type] ?? type;
  }

  function conflictStatusText(status: string) {
    const map: Record<string, string> = {
      PENDING: '待处理',
      RESOLVED: '已解决',
    };
    return map[status] ?? status;
  }

  const columns: ColumnsType<BomConflictRow> = [
    { title: 'ID', dataIndex: 'id', width: 70 },
    { title: 'BOM 图号', dataIndex: 'rowPartNo', width: 120, render: (v) => v ?? '-' },
    { title: 'BOM 名称', dataIndex: 'rowName', width: 140, render: (v) => v ?? '-' },
    { title: '冲突类型', dataIndex: 'type', width: 120, render: (v) => conflictTypeText(v) },
    { title: '状态', dataIndex: 'status', width: 90, render: (v) => conflictStatusText(v) },
    {
      title: '操作',
      key: 'action',
      width: 340,
      render: (_, row) => {
        if (row.status === 'RESOLVED') return <span>已解决</span>;
        if (row.type === 'NAME_MISMATCH') {
          return (
            <Space>
              <Radio.Group
                size="small"
                value={resolutionType[row.id]}
                onChange={(e) => setResolutionType((s) => ({ ...s, [row.id]: e.target.value }))}
                options={RESOLUTION_OPTIONS}
              />
              <Button
                type="primary"
                size="small"
                loading={resolvingId === row.id}
                onClick={() => handleResolve(row)}
              >
                解决
              </Button>
            </Space>
          );
        }
        if (row.type === 'MISSING_NO') {
          return (
            <Space>
              <Input
                placeholder="请输入新图号"
                value={fixNoValue[row.id] ?? ''}
                onChange={(e) => setFixNoValue((s) => ({ ...s, [row.id]: e.target.value }))}
                style={{ width: 140 }}
                size="small"
              />
              <Button
                type="primary"
                size="small"
                loading={resolvingId === row.id}
                onClick={() => handleResolve(row)}
              >
                修正图号
              </Button>
            </Space>
          );
        }
        return null;
      },
    },
  ];

  return (
    <Drawer
      title="冲突处理中心"
      open={open}
      onClose={onClose}
      width={720}
      footer={
        pendingList.length === 0 && conflicts.length > 0 ? (
          <Space>
            <span style={{ color: '#52c41a' }}>冲突已全部解决</span>
            {onPublishNow && bomId && (
              <Button type="primary" onClick={() => onPublishNow(bomId)}>
                立即发布
              </Button>
            )}
          </Space>
        ) : null
      }
    >
      <Table<BomConflictRow>
        dataSource={conflicts}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={false}
        size="small"
      />
    </Drawer>
  );
}
