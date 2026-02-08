import { Card, Button } from 'antd';
import { useNavigate } from 'react-router-dom';

const entries = [
  { key: 'account-open', label: '开户申请', path: '/platform/applications', description: '平台账户开户' },
  { key: 'sim-open', label: '网卡申请', path: '/platform/sim-applications', description: '物联网卡申请' },
  { key: 'reuse', label: '缴费复用', path: '/platform/applications/reuse', description: '功能开发中' },
  { key: 'verification', label: '申请核查', path: '/platform/applications/verification', description: '功能开发中' },
  { key: 'sim-activation', label: '网卡开通', path: '/platform/applications/sim-activation', description: '功能开发中' },
];

/**
 * Unified "Create application" entry: routes to Account Open, SIM Open, Reuse, Verification, SIM Activation.
 */
export default function CreateEntry() {
  const navigate = useNavigate();
  return (
    <div style={{ padding: 24 }}>
      <h2 style={{ marginBottom: 16 }}>新建申请</h2>
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: 16 }}>
        {entries.map((e) => (
          <Card key={e.key} size="small" style={{ width: 200 }}>
            <Card.Meta title={e.label} description={e.description} />
            <Button type="primary" style={{ marginTop: 12 }} onClick={() => navigate(e.path)}>
              进入
            </Button>
          </Card>
        ))}
      </div>
    </div>
  );
}
