import { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import request from '@/utils/request';
import { useAuthStore } from '@/stores/useAuthStore';

export default function Login() {
  const navigate = useNavigate();
  const login = useAuthStore((s) => s.login);
  const [loading, setLoading] = useState(false);

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      const { data } = await request.post<{ token?: string; accessToken?: string; user?: { id?: number; username?: string; roles?: string[] } }>(
        '/auth/login',
        values
      );
      const token = data?.token ?? data?.accessToken ?? 'mock-token-mvp';
      const user = data?.user ?? { username: values.username, roles: ['USER'] };
      login(token, { id: user.id, username: user.username, roles: user.roles });
      message.success('登录成功');
      navigate('/dashboard', { replace: true });
    } catch (e: unknown) {
      const err = e as { response?: { status: number } };
      if (err.response?.status === 404) {
        message.info('后端未配置登录接口，使用演示模式');
        login('demo-token', { username: values.username || 'demo', roles: ['USER'] });
        navigate('/dashboard', { replace: true });
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f0f2f5' }}>
      <Card title="DFBS 登录" style={{ width: 360 }}>
        <Form onFinish={onFinish} layout="vertical">
          <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
            <Input placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item name="password" label="密码" rules={[{ required: true }]}>
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}
