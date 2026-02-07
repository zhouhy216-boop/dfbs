import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ProDescriptions, ProTable } from '@ant-design/pro-components';
import type { ProColumns } from '@ant-design/pro-components';
import { Card, Tabs, Button } from 'antd';
import request from '@/shared/utils/request';
import type { SpringPage } from '@/shared/utils/adapters';

interface SimCardDetail {
  id: number;
  cardNo: string;
  operator?: string;
  planInfo?: string;
  machineId?: number;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

interface BindingLogRow {
  id: number;
  simId: number;
  action: string;
  machineId?: number;
  changedAt: string;
  changedBy?: string;
  remark?: string;
}

export default function SimCardDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<SimCardDetail | null>(null);
  const [historyData, setHistoryData] = useState<BindingLogRow[]>([]);
  const [historyLoading, setHistoryLoading] = useState(false);

  useEffect(() => {
    if (!id) return;
    (async () => {
      try {
        const { data } = await request.get<SimCardDetail>(`/v1/masterdata/sim-cards/${id}`);
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
      .get<SpringPage<BindingLogRow>>(`/v1/masterdata/sim-cards/${id}/history`, {
        params: { page: 0, size: 100, sort: 'changedAt,desc' },
      })
      .then((res) => {
        setHistoryData(res.data?.content ?? []);
      })
      .catch(() => setHistoryData([]))
      .finally(() => setHistoryLoading(false));
  }, [id]);

  const historyColumns: ProColumns<BindingLogRow>[] = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    { title: '操作', dataIndex: 'action', width: 100 },
    { title: '机器ID', dataIndex: 'machineId', width: 120 },
    { title: '变更时间', dataIndex: 'changedAt', valueType: 'dateTime', width: 180 },
    { title: '操作人', dataIndex: 'changedBy', width: 120 },
    { title: '备注', dataIndex: 'remark', ellipsis: true },
  ];

  if (!id) return null;

  return (
    <div style={{ padding: 24 }}>
      <Button type="link" onClick={() => navigate('/master-data/sim-cards')} style={{ marginBottom: 16 }}>
        ← 返回列表
      </Button>
      {detail && (
        <Card title="SIM卡信息" style={{ marginBottom: 24 }}>
          <ProDescriptions column={2} dataSource={detail}>
            <ProDescriptions.Item dataIndex="id" label="ID" />
            <ProDescriptions.Item dataIndex="cardNo" label="卡号" />
            <ProDescriptions.Item dataIndex="operator" label="运营商" />
            <ProDescriptions.Item dataIndex="planInfo" label="套餐信息" />
            <ProDescriptions.Item dataIndex="machineId" label="绑定机器ID" />
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
              label: '绑定历史',
              children: (
                <ProTable<BindingLogRow>
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
