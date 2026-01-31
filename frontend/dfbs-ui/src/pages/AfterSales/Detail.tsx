import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Descriptions, Button, Steps, message, Upload } from 'antd';
import type { UploadFile } from 'antd';
import request from '@/utils/request';
import dayjs from 'dayjs';

interface AfterSalesDetail {
  id: number;
  type: string;
  status: string;
  sourceShipmentId: number;
  machineNo: string;
  reason?: string;
  attachments?: string;
  relatedNewShipmentId?: number;
  createdAt?: string;
  updatedAt?: string;
}

const STATUS_STEPS = ['DRAFT', 'SUBMITTED', 'RECEIVED', 'PROCESSING', 'SENT_BACK', 'COMPLETED'];

export default function AfterSalesDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [detail, setDetail] = useState<AfterSalesDetail | null>(null);
  const [loading, setLoading] = useState(false);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (id) fetchDetail(Number(id));
  }, [id]);

  async function fetchDetail(idNum: number) {
    setLoading(true);
    try {
      const { data } = await request.get<AfterSalesDetail>(`/v1/after-sales/${idNum}`);
      setDetail(data);
      if (data.attachments) {
        try {
          const urls = JSON.parse(data.attachments) as string[];
          setFileList(urls.map((url, i) => ({ uid: String(i), name: url.split('/').pop() ?? `file-${i}`, status: 'done' as const, url })));
        } catch {
          setFileList([]);
        }
      }
    } catch {
      setDetail(null);
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit() {
    if (!id || !detail) return;
    if (detail.status !== 'DRAFT') {
      message.warning('只有草稿可提交');
      return;
    }
    if (fileList.length === 0) {
      message.error('Please upload at least one attachment');
      return;
    }
    setSubmitting(true);
    try {
      const urls = fileList.map((f) => (f.response as { url?: string })?.url ?? f.url ?? `mock://${f.name ?? f.uid}`);
      const attachments = JSON.stringify(urls);
      await request.put(`/v1/after-sales/${id}`, {
        type: detail.type,
        machineNo: detail.machineNo,
        reason: detail.reason ?? null,
        attachments,
      });
      await request.post(`/v1/after-sales/${id}/submit`);
      message.success('已提交');
      await fetchDetail(Number(id));
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      message.error(err?.response?.data?.message ?? '提交失败');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleAction(action: 'receive' | 'process' | 'send-back' | 'complete') {
    if (!id) return;
    setSubmitting(true);
    try {
      if (action === 'send-back') {
        await request.post(`/v1/after-sales/${id}/send-back`);
      } else {
        await request.post(`/v1/after-sales/${id}/${action}`);
      }
      message.success('操作成功');
      await fetchDetail(Number(id));
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } };
      message.error(err?.response?.data?.message ?? '操作失败');
    } finally {
      setSubmitting(false);
    }
  }

  const currentStepIndex = detail ? STATUS_STEPS.indexOf(detail.status) : 0;

  if (loading || !detail) {
    return <div style={{ padding: 24 }}>{loading ? '加载中...' : '未找到'}</div>;
  }

  return (
    <div style={{ padding: 24 }}>
      <Card title={`售后单 #${detail.id}`}>
        <Steps current={currentStepIndex >= 0 ? currentStepIndex : 0} size="small" style={{ marginBottom: 24 }}>
          {STATUS_STEPS.map((s) => (
            <Steps.Step key={s} title={s} />
          ))}
        </Steps>

        <Descriptions column={1} bordered size="small">
          <Descriptions.Item label="类型">{detail.type === 'EXCHANGE' ? '换货' : '维修'}</Descriptions.Item>
          <Descriptions.Item label="状态">{detail.status}</Descriptions.Item>
          <Descriptions.Item label="来源发货单ID">{detail.sourceShipmentId}</Descriptions.Item>
          <Descriptions.Item label="机器编号">{detail.machineNo}</Descriptions.Item>
          <Descriptions.Item label="原因">{detail.reason ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="关联新发货单ID">{detail.relatedNewShipmentId ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{detail.createdAt ? dayjs(detail.createdAt).format('YYYY-MM-DD HH:mm') : '-'}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{detail.updatedAt ? dayjs(detail.updatedAt).format('YYYY-MM-DD HH:mm') : '-'}</Descriptions.Item>
        </Descriptions>

        {detail.status === 'DRAFT' && (
          <div style={{ marginTop: 24 }}>
            <strong>附件（提交前至少上传一个）</strong>
            <Upload
              fileList={fileList}
              onChange={({ fileList: fl }) => setFileList(fl)}
              beforeUpload={() => false}
              multiple
            >
              <Button style={{ marginTop: 8 }}>选择文件</Button>
            </Upload>
            <p style={{ color: '#999', fontSize: 12 }}>MVP: 选择文件后点击提交即可（后端需有至少一个附件）</p>
          </div>
        )}

        <div style={{ marginTop: 24, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          {detail.status === 'DRAFT' && (
            <Button type="primary" loading={submitting} onClick={handleSubmit}>
              提交 (Submit)
            </Button>
          )}
          {detail.status === 'SUBMITTED' && (
            <Button type="primary" loading={submitting} onClick={() => handleAction('receive')}>
              已收货 (Receive)
            </Button>
          )}
          {detail.status === 'RECEIVED' && (
            <Button type="primary" loading={submitting} onClick={() => handleAction('process')}>
              处理中 (Process)
            </Button>
          )}
          {detail.status === 'PROCESSING' && (
            <Button type="primary" loading={submitting} onClick={() => handleAction('send-back')}>
              已寄回 (Send Back)
            </Button>
          )}
          {detail.status === 'SENT_BACK' && (
            <>
              <Button type="primary" loading={submitting} onClick={() => handleAction('complete')}>
                完成 (Complete)
              </Button>
              <Button onClick={() => navigate('/shipments')}>
                创建退货发货单 (Create Return Shipment)
              </Button>
            </>
          )}
          <Button onClick={() => navigate('/after-sales')}>返回列表</Button>
        </div>
      </Card>
    </div>
  );
}
