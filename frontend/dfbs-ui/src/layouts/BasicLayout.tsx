import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { ProLayout } from '@ant-design/pro-components';
import { useAuthStore } from '@/shared/stores/useAuthStore';
import {
  DashboardOutlined,
  UserOutlined,
  FileTextOutlined,
  DollarOutlined,
  ToolOutlined,
  DatabaseOutlined,
  FileOutlined,
  ApartmentOutlined,
  MobileOutlined,
  CreditCardOutlined,
  UnorderedListOutlined,
  CloudOutlined,
  CloudUploadOutlined,
  BankOutlined,
  InteractionOutlined,
  WarningOutlined,
  TruckOutlined,
  AuditOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { getStoredToken } from '@/shared/utils/request';
import { useIsSuperAdmin } from '@/shared/components/SuperAdminGuard';
import { useIsPermSuperAdmin } from '@/shared/components/PermSuperAdminGuard';
import { TestDataCleanerModal } from '@/shared/components/TestDataCleaner/Modal';

/** Static menu config: guaranteed array to avoid "spread non-iterable" in ProLayout. */
const MENU_ROUTES_BASE = [
  { path: '/dashboard', name: 'Dashboard', icon: <DashboardOutlined /> },
  { path: '/quotes', name: '报价单', icon: <FileTextOutlined /> },
  {
    path: '/logistics',
    name: '物流管理',
    icon: <TruckOutlined />,
    key: 'logistics-group',
    routes: [
      { path: '/shipments', name: '发货列表' },
      { path: '/after-sales', name: '运输异常', icon: <WarningOutlined /> },
    ],
  },
  {
    path: '/after-sales-service',
    name: '售后服务',
    icon: <ToolOutlined />,
    key: 'after-sales-service-group',
    routes: [
      { path: '/work-orders', name: '工单管理' },
    ],
  },
  { path: '/finance', name: '财务', icon: <DollarOutlined /> },
  { path: '/warehouse/inventory', name: '库存管理', icon: <BankOutlined /> },
  { path: '/warehouse/replenish', name: '补货审批', icon: <InteractionOutlined /> },
  { path: '/import-center', name: '数据导入', icon: <CloudUploadOutlined /> },
  {
    path: '/master-data',
    name: '主数据',
    icon: <DatabaseOutlined />,
    routes: [
      { path: '/customers', name: '客户', icon: <UserOutlined /> },
      { path: '/master-data/contracts', name: '合同', icon: <FileOutlined /> },
      { path: '/master-data/machines', name: '机器', icon: <MobileOutlined /> },
      { path: '/master-data/machine-models', name: '机器型号', icon: <ApartmentOutlined /> },
      { path: '/master-data/model-part-lists', name: '型号BOM', icon: <UnorderedListOutlined /> },
      { path: '/master-data/spare-parts', name: '零部件', icon: <UnorderedListOutlined /> },
      { path: '/master-data/sim-cards', name: 'SIM卡', icon: <CreditCardOutlined /> },
    ],
  },
  {
    path: '/platform',
    name: '平台&网卡管理',
    icon: <CloudOutlined />,
    key: 'platform-group',
    routes: [
      { path: '/platform/orgs', name: '平台管理' },
      { path: '/platform/sim-applications', name: 'SIM管理' },
      { path: '/platform/applications', name: '申请管理' },
    ],
  },
  {
    path: '/admin',
    name: '系统',
    icon: <AuditOutlined />,
    key: 'admin-group',
    routes: [
      { path: '/admin/confirmation-center', name: '数据确认中心' },
      { path: '/system/platform-config', name: '平台配置', icon: <SettingOutlined /> },
    ],
  },
];

/** Super Admin only: 层级配置、组织架构、变更记录、字典类型（人员视图已并入组织架构右侧） */
const ORG_STRUCTURE_MENU = [
  { path: '/admin/org-levels', name: '层级配置' },
  { path: '/admin/org-tree', name: '组织架构' },
  { path: '/admin/org-change-logs', name: '变更记录' },
  { path: '/admin/dictionary-types', name: '字典类型' },
  { path: '/admin/dictionary-snapshot-demo', name: '历史显示示例' },
];

/** PERM allowlist only: 角色与权限 */
const PERM_MENU = [{ path: '/admin/roles-permissions', name: '角色与权限' }];

function buildMenuRoutes(isSuperAdmin: boolean, permAllowed: boolean) {
  const adminExtras = [
    ...(permAllowed ? PERM_MENU : []),
    ...(isSuperAdmin ? ORG_STRUCTURE_MENU : []),
  ];
  if (adminExtras.length === 0) return MENU_ROUTES_BASE;
  return MENU_ROUTES_BASE.map((r) => {
    if (r.key === 'admin-group' && r.routes) {
      return { ...r, routes: [...r.routes, ...adminExtras] };
    }
    return r;
  });
}

export function AuthGuard({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  const navigate = useNavigate();
  const token = useAuthStore((s) => s.token);
  const hydrateFromStorage = useAuthStore((s) => s.hydrateFromStorage);
  const stored = getStoredToken();
  const isLogin = location.pathname === '/login';
  const isPublicRepair = location.pathname === '/public/repair';
  const isAuthenticated = Boolean(token || stored);

  useEffect(() => {
    hydrateFromStorage();
  }, [hydrateFromStorage]);

  useEffect(() => {
    if (isLogin || isPublicRepair) return;
    if (!isAuthenticated) {
      navigate('/login', { replace: true });
    }
  }, [isAuthenticated, isLogin, isPublicRepair, navigate]);

  if (isLogin || isPublicRepair) return <>{children}</>;
  if (!isAuthenticated) return null;
  return <>{children}</>;
}

export default function BasicLayout() {
  const navigate = useNavigate();
  const logout = useAuthStore((s) => s.logout);
  const userInfo = useAuthStore((s) => s.userInfo);
  const isSuperAdmin = useIsSuperAdmin();
  const { allowed: permAllowed } = useIsPermSuperAdmin();
  const [testDataCleanerOpen, setTestDataCleanerOpen] = useState(false);
  const displayName = userInfo?.username ?? userInfo?.name ?? 'User';
  const menuRoutes = useMemo(() => buildMenuRoutes(isSuperAdmin, permAllowed), [isSuperAdmin, permAllowed]);

  return (
    <div style={{ height: '100vh', minHeight: '100vh' }}>
      <ProLayout
        title="DFBS"
        layout="mix"
        route={{ routes: menuRoutes }}
        menuDataRender={() => menuRoutes}
        menuItemRender={(item, dom) => (
          <a onClick={() => navigate(item.path ?? '/dashboard')}>{dom}</a>
        )}
        avatarProps={{
          title: displayName,
        }}
        actionsRender={() => [
          ...(isSuperAdmin
            ? [
                <a
                  key="test-data-cleaner"
                  onClick={() => setTestDataCleanerOpen(true)}
                  style={{ marginRight: 8 }}
                >
                  测试数据清理器
                </a>,
              ]
            : []),
          <a
            key="logout"
            onClick={() => {
              logout();
              navigate('/login');
            }}
          >
            退出
          </a>,
        ]}
      >
        <Outlet />
      </ProLayout>
      <TestDataCleanerModal
        open={testDataCleanerOpen}
        onClose={() => setTestDataCleanerOpen(false)}
      />
    </div>
  );
}
