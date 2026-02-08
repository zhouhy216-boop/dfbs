import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';

interface PlaceholderApplicationProps {
  title: string;
  subTitle?: string;
  /** Route to go back to applications hub. */
  backPath?: string;
}

/**
 * Placeholder for application types not yet implemented (Reuse, Verification, SIM Activation).
 * Stable UI, no crash; clearly named for future implementation.
 */
export default function PlaceholderApplication({ title, subTitle = '功能开发中，敬请期待。', backPath = '/platform/applications' }: PlaceholderApplicationProps) {
  const navigate = useNavigate();
  return (
    <div style={{ padding: 48 }}>
      <Result
        status="info"
        title={title}
        subTitle={subTitle}
        extra={
          <Button type="primary" onClick={() => navigate(backPath)}>
            返回申请管理
          </Button>
        }
      />
    </div>
  );
}
