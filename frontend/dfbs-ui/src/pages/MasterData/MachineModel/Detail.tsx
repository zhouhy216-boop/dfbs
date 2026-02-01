import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  Tabs,
  Button,
  message,
  Descriptions,
  Drawer,
  Table,
  Tag,
  Space,
} from 'antd';
import type { ProColumns } from '@ant-design/pro-components';
import { ProTable, ModalForm, ProFormText, ProFormDatePicker, ProFormList, ProFormDigit, ProFormGroup } from '@ant-design/pro-components';
import type { ActionType } from '@ant-design/pro-components';
import request from '@/utils/request';
import { toProTableResult, type SpringPage } from '@/utils/adapters';
import ConflictDrawer from './components/ConflictDrawer';

interface ModelDetail {
  id: number;
  modelName?: string;
  modelNo: string;
  freightInfo?: string;
  warrantyInfo?: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

interface BomVersionRow {
  id: number;
  modelId: number;
  version: string;
  effectiveDate?: string;
  items: string;
  status: string;
  createdAt?: string;
  updatedAt?: string;
}

interface BomItemRow {
  partId: number | null;
  partNo: string;
  name: string;
  quantity: number;
  remark?: string;
}

interface CreateDraftResponse {
  bomId: number;
  pendingConflictsCount: number;
  createdPartsCount: number;
}

function showError(e: unknown) {
  const err = e as { response?: { data?: { message?: string }; status?: number }; message?: string };
  const msg = err.response?.data?.message ?? err.message ?? '操作失败';
  message.error(msg);
}

export default function MachineModelDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const bomTableRef = useRef<ActionType>(null);
  const [model, setModel] = useState<ModelDetail | null>(null);
  const [conflictDrawerOpen, setConflictDrawerOpen] = useState(false);
  const [conflictBomId, setConflictBomId] = useState<number | null>(null);
  const [detailsDrawerOpen, setDetailsDrawerOpen] = useState(false);
  const [detailsBom, setDetailsBom] = useState<BomVersionRow | null>(null);
  const [detailsItems, setDetailsItems] = useState<BomItemRow[]>([]);

  const modelId = id ? Number(id) : NaN;

  useEffect(() => {
    if (!id || Number.isNaN(modelId)) return;
    request
      .get<ModelDetail>(`/v1/masterdata/machine-models/${id}`)
      .then((res) => setModel(res.data))
      .catch(() => setModel(null));
  }, [id, modelId]);

  async function handlePublish(bomId: number) {
    try {
      await request.post(`/v1/masterdata/model-part-lists/${bomId}/publish`);
      message.success('已发布');
      bomTableRef.current?.reload();
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      const msg = err.response?.data?.message ?? '';
      if (msg.includes('resolve conflicts') || msg.includes('Must resolve')) {
        message.error('请先解决所有冲突');
        setConflictBomId(bomId);
        setConflictDrawerOpen(true);
      } else {
        showError(e);
      }
    }
  }

  function openConflictDrawer(bomId: number) {
    setConflictBomId(bomId);
    setConflictDrawerOpen(true);
  }

  function openDetailsDrawer(bom: BomVersionRow) {
    setDetailsBom(bom);
    try {
      const items = JSON.parse(bom.items || '[]') as BomItemRow[];
      setDetailsItems(Array.isArray(items) ? items : []);
    } catch {
      setDetailsItems([]);
    }
    setDetailsDrawerOpen(true);
  }

  async function handleDeprecate(bomId: number) {
    try {
      await request.post(`/v1/masterdata/model-part-lists/${bomId}/disable`, { updatedBy: 'user' });
      message.success('已弃用');
      bomTableRef.current?.reload();
    } catch (e) {
      showError(e);
    }
  }

  function statusTag(status: string) {
    const map: Record<string, { color: string; text: string }> = {
      DRAFT: { color: 'orange', text: '草稿' },
      PUBLISHED: { color: 'green', text: '已发布' },
      DEPRECATED: { color: 'default', text: '已作废' },
    };
    const c = map[status] ?? { color: 'default', text: status };
    return <Tag color={c.color}>{c.text}</Tag>;
  }

  const bomColumns: ProColumns<BomVersionRow>[] = [
    { title: 'ID', dataIndex: 'id', width: 80, search: false },
    { title: '版本号', dataIndex: 'version', width: 120 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 120,
      search: false,
      valueEnum: {
        DRAFT: { text: '草稿', status: 'Warning' },
        PUBLISHED: { text: '已发布', status: 'Success' },
        DEPRECATED: { text: '已作废', status: 'Default' },
      },
      render: (_, row) => statusTag(row.status),
    },
    { title: '生效日期', dataIndex: 'effectiveDate', width: 120, search: false },
    { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime', width: 180, search: false },
    {
      title: '操作',
      valueType: 'option',
      width: 320,
      render: (_, row) => {
        const actions: React.ReactNode[] = [
          <a key="details" onClick={() => openDetailsDrawer(row)}>查看明细</a>,
        ];
        if (row.status === 'DRAFT') {
          actions.push(
            <a key="publish" onClick={() => handlePublish(row.id)}>发布</a>,
            <a key="conflicts" onClick={() => openConflictDrawer(row.id)}>处理冲突</a>
          );
        }
        if (row.status === 'PUBLISHED') {
          actions.push(<a key="deprecate" onClick={() => handleDeprecate(row.id)}>作废</a>);
        }
        return actions;
      },
    },
  ];

  if (!id || Number.isNaN(modelId)) return null;

  return (
    <div style={{ padding: 24 }}>
      <Button type="link" onClick={() => navigate('/master-data/machine-models')} style={{ marginBottom: 16 }}>
        ← 返回列表
      </Button>

      {model && (
        <Card title="机器型号详情" style={{ marginBottom: 24 }}>
          <Tabs
            items={[
              {
                key: 'info',
                label: '型号信息',
                children: (
                  <Descriptions column={2} bordered size="small">
                    <Descriptions.Item label="ID">{model.id}</Descriptions.Item>
                    <Descriptions.Item label="型号编号">{model.modelNo}</Descriptions.Item>
                    <Descriptions.Item label="型号名称">{model.modelName ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="状态">{model.status}</Descriptions.Item>
                    <Descriptions.Item label="运费信息">{model.freightInfo ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="保修信息">{model.warrantyInfo ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="创建时间">{model.createdAt ?? '-'}</Descriptions.Item>
                    <Descriptions.Item label="更新时间">{model.updatedAt ?? '-'}</Descriptions.Item>
                  </Descriptions>
                ),
              },
              {
                key: 'bom',
                label: '零部件清单 (BOM)',
                children: (
                  <ProTable<BomVersionRow>
                    actionRef={bomTableRef}
                    columns={bomColumns}
                    request={async (params) => {
                      const page = (params.current ?? 1) - 1;
                      const size = params.pageSize ?? 10;
                      const { data } = await request.get<SpringPage<BomVersionRow>>('/v1/masterdata/model-part-lists', {
                        params: {
                          modelId,
                          page,
                          size,
                          sort: 'id,desc',
                        },
                      });
                      return toProTableResult(data);
                    }}
                    rowKey="id"
                    search={false}
                    pagination={{ pageSize: 10 }}
                    headerTitle="版本列表"
                    toolBarRender={() => [
                      <ModalForm<{
                        version: string;
                        effectiveDate?: string;
                        items: { partNo?: string; name?: string; quantity?: number }[];
                        createdBy?: string;
                      }>
                        key="new-version"
                        title="新建 BOM 版本"
                        trigger={<Button type="primary">新建版本</Button>}
                        onFinish={async (values) => {
                          try {
                            const items = (values.items ?? []).filter(
                              (i) => (i.partNo ?? '').trim() || (i.name ?? '').trim()
                            );
                            if (items.length === 0) {
                              message.warning('请至少添加一行 BOM 项');
                              return false;
                            }
                            const payload = {
                              modelId,
                              version: values.version?.trim() ?? '',
                              effectiveDate: values.effectiveDate ?? null,
                              items: items.map((i) => ({
                                partNo: (i.partNo ?? '').trim(),
                                name: (i.name ?? '').trim(),
                                quantity: i.quantity ?? 1,
                                remark: '',
                              })),
                              createdBy: values.createdBy ?? 'user',
                            };
                            const { data } = await request.post<CreateDraftResponse>(
                              '/v1/masterdata/model-part-lists/draft',
                              payload
                            );
                            message.success('草稿已创建');
                            bomTableRef.current?.reload();
                            if (data.pendingConflictsCount > 0) {
                              message.warning('检测到数据冲突，请处理！');
                              setConflictBomId(data.bomId);
                              setConflictDrawerOpen(true);
                            }
                            return true;
                          } catch (e) {
                            showError(e);
                            return false;
                          }
                        }}
                      >
                        <ProFormText name="version" label="版本号" rules={[{ required: true }]} />
                        <ProFormDatePicker name="effectiveDate" label="生效日期" />
                        <ProFormList
                          name="items"
                          label="清单明细"
                          creatorButtonProps={{ creatorButtonText: '添加一行' }}
                          min={1}
                        >
                          <ProFormGroup>
                            <ProFormText name="partNo" label="图号" rules={[{ required: true }]} />
                            <ProFormText name="name" label="名称" rules={[{ required: true }]} />
                            <ProFormDigit name="quantity" label="用量" min={1} initialValue={1} />
                          </ProFormGroup>
                        </ProFormList>
                        <ProFormText name="createdBy" label="创建人" initialValue="user" />
                      </ModalForm>,
                    ]}
                  />
                ),
              },
            ]}
          />
        </Card>
      )}

      <ConflictDrawer
        bomId={conflictBomId}
        open={conflictDrawerOpen}
        onClose={() => {
          setConflictDrawerOpen(false);
          setConflictBomId(null);
        }}
        onResolved={() => bomTableRef.current?.reload()}
        onPublishNow={async (bomId) => {
          try {
            await request.post(`/v1/masterdata/model-part-lists/${bomId}/publish`);
            message.success('已发布');
            setConflictDrawerOpen(false);
            setConflictBomId(null);
            bomTableRef.current?.reload();
          } catch (e) {
            showError(e);
          }
        }}
      />

      <Drawer
        title="清单明细"
        open={detailsDrawerOpen}
        onClose={() => setDetailsDrawerOpen(false)}
        width={640}
      >
        {detailsBom && (
          <>
            <p>
              版本：{detailsBom.version} | 状态：{statusTag(detailsBom.status)}
            </p>
            <Table<BomItemRow>
              dataSource={detailsItems}
              rowKey={(_, i) => String(i)}
              size="small"
              columns={[
                { title: '图号', dataIndex: 'partNo', width: 120 },
                { title: '名称', dataIndex: 'name', ellipsis: true },
                { title: '用量', dataIndex: 'quantity', width: 90 },
                { title: '备注', dataIndex: 'remark', ellipsis: true },
              ]}
              pagination={false}
            />
          </>
        )}
      </Drawer>
    </div>
  );
}
