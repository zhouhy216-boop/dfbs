/**
 * 默认密码管理：展示状态（已配置/否、最近修改时间、修改人），修改按钮打开弹窗，不展示/不存储明文。
 * Admin-only（页面已门控）。
 */
import { useCallback, useEffect, useState } from 'react';
import { Button, Card, message, Modal, Space, Typography } from 'antd';
import { Input } from 'antd';
import {
  getDefaultPasswordStatus,
  setDefaultPassword,
  type DefaultPasswordStatus,
} from './acctPermService';

const MIN_LENGTH = 6;

function formatDateTime(iso?: string): string {
  if (!iso) return '—';
  try {
    const d = new Date(iso);
    return d.toLocaleString('zh-CN', { dateStyle: 'short', timeStyle: 'short' });
  } catch {
    return iso;
  }
}

function getErrorMessage(err: { response?: { data?: { message?: string }; status?: number } }): string {
  if (err.response?.status === 403) return '无权限';
  return err.response?.data?.message ?? '操作失败，请重试';
}

export default function DefaultPasswordSection() {
  const [status, setStatus] = useState<DefaultPasswordStatus | null>(null);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadStatus = useCallback(() => {
    setLoading(true);
    getDefaultPasswordStatus()
      .then(setStatus)
      .catch((err) => {
        if (err.response?.status === 403) {
          message.error('无权限');
        } else {
          message.error(getErrorMessage(err));
        }
        setStatus({ configured: false });
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadStatus();
  }, [loadStatus]);

  const handleOpenModal = () => {
    setNewPassword('');
    setConfirmPassword('');
    setModalOpen(true);
  };

  const handleSubmit = () => {
    if (newPassword.length < MIN_LENGTH) {
      message.warning(`默认密码至少 ${MIN_LENGTH} 位`);
      return;
    }
    if (newPassword !== confirmPassword) {
      message.warning('两次输入不一致');
      return;
    }
    setSubmitting(true);
    setDefaultPassword(newPassword)
      .then(() => {
        message.success('已保存');
        setModalOpen(false);
        loadStatus();
      })
      .catch((err) => message.error(getErrorMessage(err)))
      .finally(() => setSubmitting(false));
  };

  return (
    <>
      <Card size="small" title="默认密码" style={{ marginBottom: 16 }}>
        {loading ? (
          <Typography.Text type="secondary">加载中…</Typography.Text>
        ) : (
          <>
            <Space direction="vertical" size="small" style={{ width: '100%' }}>
              <div>
                <Typography.Text type="secondary">已配置：</Typography.Text>{' '}
                {status?.configured ? '是' : '否（使用配置文件中的默认值）'}
              </div>
              {status?.configured && (
                <>
                  <div>
                    <Typography.Text type="secondary">最近修改时间：</Typography.Text>{' '}
                    {formatDateTime(status?.updatedAt)}
                  </div>
                  {status?.updatedByUserId != null && (
                    <div>
                      <Typography.Text type="secondary">修改人：</Typography.Text> 用户 ID {status.updatedByUserId}
                    </div>
                  )}
                </>
              )}
              <Typography.Text type="secondary" style={{ fontSize: 12, display: 'block', marginTop: 8 }}>
                此默认密码将用于【新建账号】与【重置密码（留空）】以及【legacy 登录】；保存后生效。
              </Typography.Text>
              <Button type="primary" size="small" onClick={handleOpenModal}>
                修改默认密码
              </Button>
            </Space>
          </>
        )}
      </Card>

      <Modal
        title="修改默认密码"
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        onOk={handleSubmit}
        confirmLoading={submitting}
        okText="保存"
        cancelText="取消"
        destroyOnClose
      >
        <Space direction="vertical" style={{ width: '100%' }} size="middle">
          <div>
            <div style={{ marginBottom: 4 }}>新默认密码（至少 {MIN_LENGTH} 位）</div>
            <Input.Password
              placeholder="请输入新默认密码"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              autoComplete="new-password"
            />
          </div>
          <div>
            <div style={{ marginBottom: 4 }}>确认新默认密码</div>
            <Input.Password
              placeholder="请再次输入"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              autoComplete="new-password"
            />
          </div>
        </Space>
      </Modal>
    </>
  );
}
