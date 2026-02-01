import { useState } from 'react';
import {
  Card,
  Segmented,
  Upload,
  Alert,
  Table,
  Radio,
  Button,
  Space,
  Statistic,
  Row,
  Col,
  message,
} from 'antd';
import type { UploadFile, UploadProps } from 'antd';
import { ProCard } from '@ant-design/pro-components';
import type { ColumnsType } from 'antd/es/table';
import { InboxOutlined, CheckCircleOutlined, CloseCircleOutlined, WarningOutlined } from '@ant-design/icons';
import request from '@/utils/request';

const { Dragger } = Upload;

type ImportType =
  | 'customers'
  | 'contracts'
  | 'machines'
  | 'models'
  | 'spare-parts'
  | 'sim-cards'
  | 'model-part-lists';

interface ImportFailureDto {
  rowNum: number;
  uniqueKey: string;
  reason: string;
}

interface ImportConflictDto {
  rowNum: number;
  uniqueKey: string;
  originalData: string;
  importData: string;
}

interface ImportResultDto {
  successCount: number;
  failureCount: number;
  conflictCount: number;
  failures: ImportFailureDto[];
  conflicts: ImportConflictDto[];
}

/** Fallback: translate known English backend messages to Chinese for display */
function localizeBackendMessage(msg: string | undefined): string {
  if (!msg || typeof msg !== 'string') return msg ?? '操作失败';
  const map: Record<string, string> = {
    'Import failed:': '导入失败：',
    'Name is required': '名称必填',
    'Code is required': '编码必填',
    'Part No is required': '图号必填',
    'Serial No is required': '序列号必填',
    'Reference not found': '引用对象不存在',
    'Duplicate key': '唯一键重复',
    'Must resolve conflicts first': '必须先解决冲突',
    'Create draft failed': '创建草稿失败',
  };
  for (const [en, zh] of Object.entries(map)) {
    if (msg.includes(en)) return msg.replace(en, zh);
  }
  return msg;
}

const IMPORT_TYPE_OPTIONS: { value: ImportType; label: string; disabled?: boolean }[] = [
  { value: 'customers', label: '客户' },
  { value: 'contracts', label: '合同' },
  { value: 'machines', label: '机器' },
  { value: 'models', label: '机器型号' },
  { value: 'spare-parts', label: '零部件' },
  { value: 'sim-cards', label: 'SIM卡' },
  { value: 'model-part-lists', label: '型号BOM' },
];

function renderJsonField(json: string): React.ReactNode {
  if (!json) return '-';
  try {
    const obj = typeof json === 'string' ? JSON.parse(json) : json;
    const entries = Object.entries(obj).filter(([, v]) => v != null && v !== '');
    if (entries.length === 0) return '-';
    return (
      <div style={{ fontSize: 12 }}>
        {entries.map(([k, v]) => (
          <div key={k}>
            <strong>{k}:</strong> {String(v)}
          </div>
        ))}
      </div>
    );
  } catch {
    return <span title={json}>{String(json).slice(0, 80)}…</span>;
  }
}

export default function ImportCenter() {
  const [importType, setImportType] = useState<ImportType>('customers');
  const [result, setResult] = useState<ImportResultDto | null>(null);
  const [uploading, setUploading] = useState(false);
  const [conflictActions, setConflictActions] = useState<Record<number, 'SKIP' | 'UPDATE' | 'REUSE'>>({});
  const [resolving, setResolving] = useState(false);

  const uploadProps: UploadProps = {
    name: 'file',
    multiple: false,
    accept: '.xlsx,.xls',
    maxCount: 1,
    showUploadList: true,
    disabled: uploading,
    customRequest: async ({ file, onSuccess, onError }) => {
      const f = file as File;
      setUploading(true);
      setResult(null);
      setConflictActions({});
      const formData = new FormData();
      formData.append('file', f);
      try {
        const { data } = await request.post<ImportResultDto>(
          `/v1/imports/${importType}`,
          formData,
          { headers: { 'Content-Type': undefined } as Record<string, string> }
        );
        setResult(data);
        if (data.conflicts?.length) {
          const initial: Record<number, 'SKIP' | 'UPDATE' | 'REUSE'> = {};
          data.conflicts.forEach((c) => (initial[c.rowNum] = 'SKIP'));
          setConflictActions(initial);
        }
        onSuccess?.(data);
        message.success('导入完成');
      } catch (e: unknown) {
        const err = e as { message?: string; response?: { data?: { message?: string } } };
        const msg = err.response?.data?.message ?? err.message ?? '上传失败';
        message.error(localizeBackendMessage(msg));
        onError?.(err as Error);
      } finally {
        setUploading(false);
      }
    },
  };

  async function handleApplyResolution() {
    if (!result?.conflicts?.length) return;
    setResolving(true);
    try {
      const actions = result.conflicts.map((c) => ({
        action: conflictActions[c.rowNum] ?? 'SKIP',
        rowKey: String(c.rowNum),
      }));
      await request.post(`/v1/imports/${importType}/resolve`, actions);
      message.success('冲突已成功处理');
      setResult((prev) =>
        prev
          ? {
              ...prev,
              conflicts: [],
              conflictCount: 0,
            }
          : null
      );
      setConflictActions({});
    } catch (e: unknown) {
      const msg = (e as { message?: string })?.message ?? '提交失败';
      message.error(localizeBackendMessage(msg));
    } finally {
      setResolving(false);
    }
  }

  const failureColumns: ColumnsType<ImportFailureDto> = [
    { title: '行号', dataIndex: 'rowNum', width: 80 },
    { title: '唯一标识', dataIndex: 'uniqueKey', width: 120, ellipsis: true },
    { title: '失败原因', dataIndex: 'reason', ellipsis: true },
  ];

  const conflictColumns: ColumnsType<ImportConflictDto & { action?: string }> = [
    { title: '行号', dataIndex: 'rowNum', width: 80 },
    {
      title: '原系统数据',
      dataIndex: 'originalData',
      render: (v: string) => renderJsonField(v),
      ellipsis: false,
    },
    {
      title: '新导入数据',
      dataIndex: 'importData',
      render: (v: string) => renderJsonField(v),
      ellipsis: false,
    },
    {
      title: '处理方式',
      key: 'action',
      width: 260,
      render: (_, row) => (
        <Radio.Group
          size="small"
          value={conflictActions[row.rowNum] ?? 'SKIP'}
          onChange={(e) =>
            setConflictActions((s) => ({ ...s, [row.rowNum]: e.target.value as 'SKIP' | 'UPDATE' | 'REUSE' }))
          }
          options={[
            { label: '跳过 (不导入)', value: 'SKIP' },
            { label: '覆盖更新', value: 'UPDATE' },
            { label: '仅复用 (不更新)', value: 'REUSE' },
          ]}
        />
      ),
    },
  ];

  const hasResult = result != null;
  const hasFailures = (result?.failureCount ?? 0) > 0;
  const hasConflicts = (result?.conflictCount ?? 0) > 0;

  return (
    <div style={{ padding: 24 }}>
      <Card title="数据导入" style={{ marginBottom: 24 }}>
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <div>
            <div style={{ marginBottom: 8 }}>选择导入类型</div>
            <Segmented
              options={IMPORT_TYPE_OPTIONS}
              value={importType}
              onChange={(v) => setImportType(v as ImportType)}
            />
          </div>

          <div>
            <div style={{ marginBottom: 8, display: 'flex', alignItems: 'center', gap: 16 }}>
              <span>上传 Excel</span>
              <a
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  message.info('模板下载功能即将开放');
                }}
              >
                下载导入模板
              </a>
            </div>
            <Dragger {...uploadProps}>
              <p className="ant-upload-drag-icon">
                <InboxOutlined style={{ fontSize: 48, color: '#1890ff' }} />
              </p>
              <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
              <p className="ant-upload-hint">支持 Excel 文件上传，请确保格式正确。</p>
            </Dragger>
          </div>
        </Space>
      </Card>

      {hasResult && (
        <ProCard title="导入结果" style={{ marginBottom: 24 }}>
          <Row gutter={24} style={{ marginBottom: 24 }}>
            <Col span={8}>
              <Alert
                type="success"
                showIcon
                icon={<CheckCircleOutlined />}
                message={
                  <Statistic title="导入成功" value={result.successCount} valueStyle={{ color: '#52c41a' }} />
                }
              />
            </Col>
            <Col span={8}>
              <Alert
                type="error"
                showIcon
                icon={<CloseCircleOutlined />}
                message={
                  <Statistic title="导入失败" value={result.failureCount} valueStyle={{ color: '#ff4d4f' }} />
                }
              />
            </Col>
            <Col span={8}>
              <Alert
                type="warning"
                showIcon
                icon={<WarningOutlined />}
                message={
                  <Statistic title="发现冲突" value={result.conflictCount} valueStyle={{ color: '#faad14' }} />
                }
              />
            </Col>
          </Row>

          {hasFailures && (
            <div style={{ marginBottom: 24 }}>
              <div style={{ marginBottom: 8, fontWeight: 500 }}>失败明细</div>
              <Table
                dataSource={result.failures}
                columns={failureColumns}
                rowKey="rowNum"
                size="small"
                pagination={false}
              />
            </div>
          )}

          {hasConflicts && (
            <div>
              <div style={{ marginBottom: 8, fontWeight: 500 }}>冲突处理</div>
              <Table
                dataSource={result.conflicts}
                columns={conflictColumns}
                rowKey={(r) => `${r.rowNum}-${r.uniqueKey}`}
                size="small"
                pagination={false}
                style={{ marginBottom: 16 }}
              />
              <Button type="primary" loading={resolving} onClick={handleApplyResolution}>
                应用处理方案
              </Button>
            </div>
          )}
        </ProCard>
      )}
    </div>
  );
}
