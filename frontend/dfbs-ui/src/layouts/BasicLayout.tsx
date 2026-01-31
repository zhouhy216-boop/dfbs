import { useEffect } from 'react';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { ProLayout } from '@ant-design/pro-components';
import {
  DashboardOutlined,
  UserOutlined,
  FileTextOutlined,
  CarOutlined,
  DollarOutlined,
  ToolOutlined,
  DatabaseOutlined,
  FileOutlined,
  ApartmentOutlined,
  MobileOutlined,
  CreditCardOutlined,
  UnorderedListOutlined,
} from '@ant-design/icons';
import { getStoredToken } from '@/utils/request';
import { useAuthStore } from '@/stores/useAuthStore';

/** Static menu config: guaranteed array to avoid "spread non-iterable" in ProLayout. */
const MENU_ROUTES = [
  { path: '/dashboard', name: 'Dashboard', icon: <DashboardOutlined /> },
  { path: '/quotes', name: '报价单', icon: <FileTextOutlined /> },
  { path: '/shipments', name: '发货', icon: <CarOutlined /> },
  { path: '/after-sales', name: '售后', icon: <ToolOutlined /> },
  { path: '/finance', name: '财务', icon: <DollarOutlined /> },
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
  const isAuthenticated = Boolean(token || stored);

  useEffect(() => {
    hydrateFromStorage();
  }, [hydrateFromStorage]);

  useEffect(() => {
    if (isLogin) return;
    if (!isAuthenticated) {
      navigate('/login', { replace: true });
    }
  }, [isAuthenticated, isLogin, navigate]);

  if (isLogin) return <>{children}</>;
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
