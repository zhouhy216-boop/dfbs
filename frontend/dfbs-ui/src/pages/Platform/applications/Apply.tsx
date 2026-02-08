import { useNavigate, useSearchParams } from 'react-router-dom';
import PlatformApplication from '@/pages/Platform/Application';

/**
 * Standalone platform apply form entry from Platform Management page.
 * source=sales | service | enterprise (营企申请 = 申请平台).
 * Does not use the removed "新建申请" card-entry page.
 */
export default function PlatformApply() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const source = params.get('source') || 'sales';
  const isEnterprise = source === 'enterprise';
  const initialSourceType = source === 'service' ? 'SERVICE' : 'FACTORY';

  return (
    <PlatformApplication
      createModalOnly
      open
      initialSourceType={initialSourceType}
      enterpriseDirect={isEnterprise}
      onSuccess={() => navigate('/platform/applications')}
      onCancel={() => navigate('/platform/orgs')}
    />
  );
}
