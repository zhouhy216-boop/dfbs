import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { ProLayout } from '@ant-design/pro-components';
import { Button, Dropdown, Modal, Table } from 'antd';
import type { MenuProps } from 'antd';
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
import request, { getStoredToken } from '@/shared/utils/request';
import { useIsSuperAdmin } from '@/shared/components/SuperAdminGuard';
import { useIsPermSuperAdmin } from '@/shared/components/PermSuperAdminGuard';
import { useIsAdminOrSuperAdmin } from '@/shared/components/AdminOrSuperAdminGuard';
import { useEffectivePermissions } from '@/shared/hooks/useEffectivePermissions';
import {
  ROLE_TO_UI_GATING_MATRIX,
  SUPPORT_NONE_ENTRY_IDS,
  filterMenuBySimulatedRole,
  type MenuRouteItem,
  type RoleToUiGatingEntry,
} from '@/shared/config/roleToUiGatingMatrix';
import { useSimulatedRoleStore } from '@/shared/stores/useSimulatedRoleStore';

/** Re-export for consumers that need the storage key. */
export { SIMULATED_ROLE_STORAGE_KEY } from '@/shared/stores/useSimulatedRoleStore';

/** Temporary UI list for role simulator shell; not wired to backend roles. */
const SIMULATOR_ROLE_OPTIONS: { label: string; key: string }[] = [
  { label: 'Super Admin', key: 'Super Admin' },
  { label: 'Admin', key: 'Admin' },
  { label: 'Operator', key: 'Operator' },
  { label: 'Viewer', key: 'Viewer' },
  { label: 'None', key: '__none__' },
];

const PERM_ORGS_VIEW = 'platform_application.orgs:VIEW';
const PERM_APPS_VIEW = 'platform_application.applications:VIEW';
const PERM_WORK_ORDER_VIEW = 'work_order:VIEW';
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

/** Super Admin only: 数据字典、层级配置、组织架构、变更记录（保留数据字典，移除重复入口字典类型） */
const ORG_STRUCTURE_MENU = [
  { path: '/admin/data-dictionary', name: '数据字典' },
  { path: '/admin/org-levels', name: '层级配置' },
  { path: '/admin/org-tree', name: '组织架构' },
  { path: '/admin/org-change-logs', name: '变更记录' },
  { path: '/admin/dictionary-snapshot-demo', name: '历史显示示例' },
];

/** Admin or Super-admin: 账号与权限（保留；角色与权限已从左侧菜单移除，避免重复） */
const ACCOUNT_PERMISSIONS_MENU = [{ path: '/admin/account-permissions', name: '账号与权限' }];

function buildMenuRoutes(
  isSuperAdmin: boolean,
  _permAllowed: boolean,
  isAdminOrSuperAdmin: boolean,
  hasPermission: (key: string) => boolean
) {
  const adminExtras = [
    ...(isAdminOrSuperAdmin ? ACCOUNT_PERMISSIONS_MENU : []),
    ...(isSuperAdmin ? ORG_STRUCTURE_MENU : []),
  ];
  return MENU_ROUTES_BASE.map((r) => {
    if (r.key === 'platform-group' && r.routes) {
      const platformRoutes = (r.routes as { path: string; name: string }[]).filter((item) => {
        if (item.path === '/platform/orgs') return hasPermission(PERM_ORGS_VIEW) || isAdminOrSuperAdmin;
        if (item.path === '/platform/applications') return hasPermission(PERM_APPS_VIEW) || isAdminOrSuperAdmin;
        return true;
      });
      return { ...r, routes: platformRoutes };
    }
    if (r.key === 'after-sales-service-group' && r.routes) {
      const workOrderRoutes = (r.routes as { path: string; name: string }[]).filter((item) => {
        if (item.path === '/work-orders') return hasPermission(PERM_WORK_ORDER_VIEW) || isAdminOrSuperAdmin;
        return true;
      });
      return { ...r, routes: workOrderRoutes };
    }
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
  const setUserInfoFromMe = useAuthStore((s) => s.setUserInfoFromMe);
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

  // Refresh userInfo from server so role-dependent UI (e.g. simulator) uses server-authoritative roles after load/restart, not stale localStorage.
  useEffect(() => {
    if (!isAuthenticated || isLogin || isPublicRepair) return;
    request
      .get<{ id?: number; username?: string; nickname?: string; roles?: string[] }>('/auth/me')
      .then((res) => {
        if (res.data && typeof res.data === 'object') {
          setUserInfoFromMe({ id: res.data.id, username: res.data.username, roles: res.data.roles });
        }
      })
      .catch(() => {});
  }, [isAuthenticated, isLogin, isPublicRepair, setUserInfoFromMe]);

  if (isLogin || isPublicRepair) return <>{children}</>;
  if (!isAuthenticated) return null;
  return <>{children}</>;
}

export default function BasicLayout() {
  const navigate = useNavigate();
  const logout = useAuthStore((s) => s.logout);
  const userInfo = useAuthStore((s) => s.userInfo);
  const userInfoRefreshedFromServer = useAuthStore((s) => s.userInfoRefreshedFromServer);
  const isSuperAdmin = useIsSuperAdmin();
  const { allowed: permAllowed } = useIsPermSuperAdmin();
  const isAdminOrSuperAdmin = useIsAdminOrSuperAdmin();
  const { has: hasPermission } = useEffectivePermissions();
  const [testDataCleanerOpen, setTestDataCleanerOpen] = useState(false);
  const [matrixReviewOpen, setMatrixReviewOpen] = useState(false);
  const simulatedRole = useSimulatedRoleStore((s) => s.simulatedRole);
  const setSimulatedRole = useSimulatedRoleStore((s) => s.setSimulatedRole);
  const displayName = userInfo?.username ?? 'User';
  const menuRoutes = useMemo(() => {
    const base = buildMenuRoutes(isSuperAdmin, permAllowed, isAdminOrSuperAdmin, hasPermission);
    return filterMenuBySimulatedRole(base as MenuRouteItem[], simulatedRole);
  }, [isSuperAdmin, permAllowed, isAdminOrSuperAdmin, hasPermission, simulatedRole]);
  const showSimulator =
    Boolean(userInfoRefreshedFromServer && userInfo && Array.isArray(userInfo.roles) &&
      (userInfo.roles.includes('ADMIN') || userInfo.roles.includes('SUPER_ADMIN')));

  const onSimulatedRoleSelect: MenuProps['onClick'] = ({ key }) => {
    const value = key === '__none__' ? null : key;
    setSimulatedRole(value);
  };

  const simulatorMenuItems: MenuProps['items'] = SIMULATOR_ROLE_OPTIONS.map((opt) => ({
    key: opt.key,
    label: opt.label,
  }));

  return (
    <div style={{ height: '100vh', minHeight: '100vh' }}>
      <ProLayout
        title="DFBS"
        layout="mix"
        route={{ routes: menuRoutes }}
        menuDataRender={() => menuRoutes as any}
        menuItemRender={(item, dom) => (
          <a onClick={() => navigate(item.path ?? '/dashboard')}>{dom}</a>
        )}
        avatarProps={{
          title: displayName,
        }}
        actionsRender={() => [
          ...(showSimulator
            ? [
                ...(simulatedRole
                  ? [
                      <span
                        key="sim-badge"
                        style={{
                          marginRight: 12,
                          padding: '2px 8px',
                          background: '#faad14',
                          color: '#000',
                          borderRadius: 4,
                          fontSize: 12,
                          fontWeight: 500,
                        }}
                      >
                        SIMULATING: {simulatedRole}
                      </span>,
                    ]
                  : []),
                <span
                  key="sim-disclaimer"
                  style={{ marginRight: 8, fontSize: 11, color: '#8c8c8c' }}
                  title="UI-only simulation; does not change real permissions or security."
                >
                  仅界面模拟，不改变实际权限
                </span>,
                <Dropdown
                  key="simulated-role"
                  menu={{ items: simulatorMenuItems, onClick: onSimulatedRoleSelect }}
                  trigger={['click']}
                >
                  <a onClick={(e) => e.preventDefault()} style={{ marginRight: 8 }}>
                    Simulated Role
                  </a>
                </Dropdown>,
                <a
                  key="matrix-review"
                  onClick={() => setMatrixReviewOpen(true)}
                  style={{ marginRight: 8 }}
                  title="Read-only review of Role-to-UI gating matrix"
                >
                  角色-界面矩阵（查看）
                </a>,
              ]
            : []),
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
      <Modal
        title="Role-to-UI gating matrix（只读查看）"
        open={matrixReviewOpen}
        onCancel={() => setMatrixReviewOpen(false)}
        footer={<Button type="primary" onClick={() => setMatrixReviewOpen(false)}>关闭</Button>}
        width={1000}
        destroyOnClose
      >
        <div style={{ marginBottom: 12 }}>
          <strong>总条目数：</strong>{ROLE_TO_UI_GATING_MATRIX.length}
          <span style={{ marginLeft: 16 }}>
            <strong>使用 Support/none 的条目：</strong>{SUPPORT_NONE_ENTRY_IDS.length} 个 — {SUPPORT_NONE_ENTRY_IDS.join(', ')}
          </span>
        </div>
        <Table<RoleToUiGatingEntry>
          size="small"
          pagination={false}
          scroll={{ x: 900 }}
          dataSource={ROLE_TO_UI_GATING_MATRIX}
          rowKey="id"
          columns={[
            { title: 'id', dataIndex: 'id', width: 180, ellipsis: true },
            { title: 'UI 区域', dataIndex: 'uiAreaName', width: 220, ellipsis: true },
            {
              title: '业务模块',
              dataIndex: 'businessModuleCodes',
              width: 100,
              render: (v: string[]) => (Array.isArray(v) ? v.join(', ') : ''),
            },
            {
              title: '路由锚点',
              dataIndex: 'routeAnchors',
              width: 140,
              render: (v: string[]) => (Array.isArray(v) ? v.join(', ') : ''),
            },
            {
              title: '流程节点',
              dataIndex: 'processNodes',
              width: 120,
              render: (v: string[]) => (Array.isArray(v) ? v.join(', ') : ''),
            },
            {
              title: '对象范围',
              dataIndex: 'objectScope',
              width: 140,
              render: (v: string[]) => (Array.isArray(v) ? v.join(', ') : '—'),
            },
            {
              title: '可见角色集',
              dataIndex: 'allowedSimulatedRoleSet',
              width: 160,
              render: (v: string[]) => (Array.isArray(v) ? v.join(', ') : ''),
            },
          ]}
        />
      </Modal>
    </div>
  );
}
