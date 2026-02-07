import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ProDescriptions, ProTable } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Card, Tabs, Button, message } from 'antd';
import request from '@/shared/utils/request';
import type { SpringPage } from '@/shared/utils/adapters';

interface MachineDetail {
  id: number;
  machineNo: string;
  serialNo: string;
  customerId?: number;
  contractId?: number;
  modelId?: number;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

interface OwnershipLogRow {
  id: number;
  machineId: number;
  oldCustomerId?: number;
  newCustomerId?: number;
  changedAt: string;
  changedBy?: string;
  remark?: string;
}

export default function MachineDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<MachineDetail | null>(null);
  const [historyData, setHistoryData] = useState<OwnershipLogRow[]>([]);
  const [historyLoading, setHistoryLoading] = useState(false);

  useEffect(() => {
    if (!id) return;
    (async () => {
      try {
        const { data } = await request.get<MachineDetail>(`/v1/masterdata/machines/${id}`);
        setDetail(data);
      } catch {
        setDetail(null);
      }
    })();
  }, [id]);

  useEffect(() => {
    if (!id) return;
    setHistoryLoading(true);
    request
      .get<SpringPage<OwnershipLogRow>>(`/v1/masterdata/machines/${id}/history`, {
        params: { page: 0, size: 100, sort: 'changedAt,desc' },
      })
      .then((res) => {
        setHistoryData(res.data?.content ?? []);
      })
      .catch(() => setHistoryData([]))
      .finally(() => setHistoryLoading(false));
  }, [id]);

  const historyColumns: ProColumns<OwnershipLogRow>[] = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '原客户ID', dataIndex: 'oldCustomerId', width: 120 },
    { title: '新客户ID', dataIndex: 'newCustomerId', width: 120 },
    { title: '变更时间', dataIndex: 'changedAt', valueType: 'dateTime', width: 180 },
    { title: '操作人', dataIndex: 'changedBy', width: 120 },
    { title: '备注', dataIndex: 'remark', ellipsis: true },
  ];

  if (!id) return null;

  return (
    <div style={{ padding: 24 }}>
      <Button type="link" onClick={() => navigate('/master-data/machines')} style={{ marginBottom: 16 }}>
        ← 返回列表
      </Button>
      {detail && (
        <Card title="机器信息" style={{ marginBottom: 24 }}>
          <ProDescriptions column={2} dataSource={detail}>
            <ProDescriptions.Item dataIndex="id" label="ID" />
            <ProDescriptions.Item dataIndex="machineNo" label="机器编号" />
            <ProDescriptions.Item dataIndex="serialNo" label="序列号" />
            <ProDescriptions.Item dataIndex="customerId" label="客户ID" />
            <ProDescriptions.Item dataIndex="contractId" label="合同ID" />
            <ProDescriptions.Item dataIndex="modelId" label="型号ID" />
            <ProDescriptions.Item dataIndex="status" label="状态" />
            <ProDescriptions.Item dataIndex="createdAt" label="创建时间" valueType="dateTime" />
            <ProDescriptions.Item dataIndex="updatedAt" label="更新时间" valueType="dateTime" />
          </ProDescriptions>
        </Card>
      )}
      <Card>
        <Tabs
          items={[
            {
              key: 'history',
              label: '归属历史',
              children: (
                <ProTable<OwnershipLogRow>
                  columns={historyColumns}
                  dataSource={historyData}
                  loading={historyLoading}
                  rowKey="id"
                  search={false}
                  pagination={false}
                  options={false}
                  toolBarRender={false}
                />
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
}
