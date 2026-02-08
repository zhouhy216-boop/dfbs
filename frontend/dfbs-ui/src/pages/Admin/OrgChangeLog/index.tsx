import { useEffect, useState } from 'react';
import { DatePicker, Select, Space, Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { listChangeLogs, type ChangeLogItem } from '@/features/orgstructure/services/orgStructure';
import dayjs from 'dayjs';

const OBJECT_TYPE_OPTIONS = [
  { label: '全部', value: '' },
  { label: '层级', value: 'LEVEL' },
  { label: '组织节点', value: 'ORG_NODE' },
  { label: '人员', value: 'PERSON' },
];

export default function OrgChangeLogPage() {
  const [data, setData] = useState<ChangeLogItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [objectType, setObjectType] = useState<string>('');
  const [from, setFrom] = useState<string | undefined>(undefined);
  const [to, setTo] = useState<string | undefined>(undefined);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);

  const fetchList = async () => {
    setLoading(true);
    try {
      const res = await listChangeLogs({
        objectType: objectType || undefined,
        from,
        to,
        page,
        size: pageSize,
      });
      setData(res.content ?? []);
      setTotal(res.totalElements ?? 0);
    } catch {
      setData([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchList();
  }, [objectType, from, to, page, pageSize]);

  const columns: ColumnsType<ChangeLogItem> = [
    { title: '时间', dataIndex: 'timestamp', width: 180, render: (v: string) => v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '-' },
    { title: '对象类型', dataIndex: 'objectType', width: 90 },
    { title: '对象ID', dataIndex: 'objectId', width: 80 },
    { title: '操作', dataIndex: 'action', width: 90 },
    { title: '操作人', dataIndex: 'operatorName', width: 100 },
    { title: '摘要', dataIndex: 'summaryText', ellipsis: true },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Space style={{ marginBottom: 16 }} wrap>
        <Select
          placeholder="对象类型"
          style={{ width: 120 }}
          options={OBJECT_TYPE_OPTIONS}
          value={objectType}
          onChange={setObjectType}
        />
        <DatePicker.RangePicker
          onChange={(dates) => {
            if (dates && dates[0]) setFrom(dates[0].format('YYYY-MM-DD'));
            else setFrom(undefined);
            if (dates && dates[1]) setTo(dates[1].format('YYYY-MM-DD'));
            else setTo(undefined);
          }}
        />
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
    </div>
  );
}
