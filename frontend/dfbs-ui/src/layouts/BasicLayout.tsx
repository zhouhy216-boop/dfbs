import { useEffect } from 'react';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { ProLayout } from '@ant-design/pro-components';
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
  CloudUploadOutlined,
  BankOutlined,
  InteractionOutlined,
  WarningOutlined,
  TruckOutlined,
} from '@ant-design/icons';
import { getStoredToken } from '@/utils/request';
import { useAuthStore } from '@/stores/useAuthStore';

/** Static menu config: guaranteed array to avoid "spread non-iterable" in ProLayout. */
const MENU_ROUTES = [
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
];

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
  const displayName = userInfo?.username ?? userInfo?.name ?? 'User';

  console.log('BasicLayout rendering', { userInfo });

  return (
    <div style={{ height: '100vh', minHeight: '100vh' }}>
      <ProLayout
        title="DFBS"
        layout="mix"
        route={{ routes: MENU_ROUTES }}
        menuDataRender={() => MENU_ROUTES}
        menuItemRender={(item, dom) => (
          <a onClick={() => navigate(item.path ?? '/dashboard')}>{dom}</a>
        )}
        avatarProps={{
          title: displayName,
        }}
        actionsRender={() => [
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
    </div>
  );
}
