/**
 * 账号与权限 — primary admin entry (admin or super-admin).
 * Tabs: 账号 | 角色模板 | 权限管理. 权限管理 tab visible only when perm allowlist (useIsPermSuperAdmin).
 * Top: 默认密码设置（admin-only，不展示明文）.
 */
import { Tabs } from 'antd';
import { PageContainer } from '@ant-design/pro-components';
import { useIsPermSuperAdmin } from '@/shared/components/PermSuperAdminGuard';
import AccountsTab from './AccountsTab';
import DefaultPasswordSection from './DefaultPasswordSection';
import RoleTemplatesTab from './RoleTemplatesTab';
import PermissionTreeTab from './PermissionTreeTab';

export default function AccountPermissionsPage() {
  const { allowed: permAllowed } = useIsPermSuperAdmin();

  const tabItems = [
    { key: 'accounts', label: '账号', children: <AccountsTab /> },
    { key: 'templates', label: '角色模板', children: <RoleTemplatesTab /> },
    ...(permAllowed ? [{ key: 'tree', label: '权限管理', children: <PermissionTreeTab /> }] : []),
  ];

  return (
    <PageContainer title="账号与权限">
      <DefaultPasswordSection />
      <Tabs items={tabItems} />
    </PageContainer>
  );
}
