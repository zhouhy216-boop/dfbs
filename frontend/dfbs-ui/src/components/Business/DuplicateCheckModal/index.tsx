import { Modal, Table } from 'antd';
import type { ReactNode } from 'react';

export interface DuplicateMatchItem {
  orgCodeShort?: string;
  customerName?: string;
  email?: string;
  phone?: string;
  matchReason?: string;
}

export interface DuplicateCheckModalProps {
  visible: boolean;
  matches: DuplicateMatchItem[];
  currentInput?: { customerName?: string; email?: string; phone?: string } | null;
  title?: string;
  /** Custom footer buttons; receive close callback. */
  renderFooter: (close: () => void) => ReactNode;
  onCancel: () => void;
}

const norm = (s: string | undefined) => (s ?? '').trim().toLowerCase();
const redStyle = { color: '#ff4d4f', fontWeight: 600 as const };

/**
 * Reusable duplicate warning modal: shows "检测到以下信息已存在..." with a table.
 * Red highlighting for cells that match currentInput. Custom footer via renderFooter(close).
 */
export default function DuplicateCheckModal({
  visible,
  matches,
  currentInput,
  title = '重复提醒',
  renderFooter,
  onCancel,
}: DuplicateCheckModalProps) {
  const close = () => onCancel();

  const cur = currentInput
    ? {
        customerName: norm(currentInput.customerName),
        email: norm(currentInput.email),
        phone: norm(currentInput.phone),
      }
    : null;

  const columns = [
    {
      title: '机构代码',
      dataIndex: 'orgCodeShort',
      key: 'orgCodeShort',
      width: 120,
      render: (val: string) => (val ?? '—'),
    },
    {
      title: '客户',
      dataIndex: 'customerName',
      key: 'customerName',
      width: 140,
      ellipsis: true,
      render: (val: string) => (
        <span style={cur && norm(val) === cur.customerName ? redStyle : undefined}>{val ?? '—'}</span>
      ),
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: 160,
      ellipsis: true,
      render: (val: string) => (
        <span style={cur && norm(val) === cur.email ? redStyle : undefined}>{val ?? '—'}</span>
      ),
    },
    {
      title: '电话',
      dataIndex: 'phone',
      key: 'phone',
      width: 120,
      render: (val: string) => (
        <span style={cur && norm(val) === cur.phone ? redStyle : undefined}>{val ?? '—'}</span>
      ),
    },
  ];

  return (
    <Modal
      title={title}
      open={visible}
      onCancel={onCancel}
      footer={renderFooter(close)}
      width={640}
      destroyOnClose
    >
      <p style={{ marginBottom: 12, color: '#333' }}>
        检测到以下信息已存在，请确认是否仍要提交或选择其他操作：
      </p>
      <Table
        dataSource={matches}
        rowKey={(r, i) => `${r.orgCodeShort ?? ''}-${i}`}
        pagination={false}
        size="small"
        columns={columns}
      />
    </Modal>
  );
}
