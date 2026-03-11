import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import { ProLayout } from '@ant-design/pro-components';
import { Button, Input, Modal, Table } from 'antd';
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
import request, {
  getStoredToken,
  getStoredUserId,
  getStoredUserInfo,
  setStoredUserId,
  getStoredOriginalUserId,
  setStoredOriginalUserId,
  getStoredOriginalUserInfo,
  setStoredOriginalUserInfo,
  clearOriginalUser,
} from '@/shared/utils/request';
import { clearPermAllowedCache } from '@/shared/permAllowedCache';
import { clearEffectiveKeysCache } from '@/shared/effectiveKeysCache';
import { getAccountList, type AccountListItem } from '@/pages/Admin/AccountPermissions/acctPermService';
import { useIsSuperAdmin } from '@/shared/components/SuperAdminGuard';
import { useIsPermSuperAdmin } from '@/shared/components/PermSuperAdminGuard';
import { useIsAdminOrSuperAdmin } from '@/shared/components/AdminOrSuperAdminGuard';
import { useEffectivePermissions } from '@/shared/hooks/useEffectivePermissions';
import { SIMULATOR_BUSINESS_ROLES_ZH, filterMenuBySimulatedRole, type MenuRouteItem } from '@/shared/config/roleToUiGatingMatrix';
import { useSimulatedRoleStore } from '@/shared/stores/useSimulatedRoleStore';

/** Re-export for consumers that need the storage key. */
export { SIMULATED_ROLE_STORAGE_KEY } from '@/shared/stores/useSimulatedRoleStore';

import { TestDataCleanerModal } from '@/shared/components/TestDataCleaner/Modal';

const PERM_ORGS_VIEW = 'platform_application.orgs:VIEW';
const PERM_APPS_VIEW = 'platform_application.applications:VIEW';
const PERM_WORK_ORDER_VIEW = 'work_order:VIEW';

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
  const [accountSwitchModalOpen, setAccountSwitchModalOpen] = useState(false);
  const [accountSwitchList, setAccountSwitchList] = useState<AccountListItem[]>([]);
  const [accountSwitchLoading, setAccountSwitchLoading] = useState(false);
  const [accountSwitchQuery, setAccountSwitchQuery] = useState('');
  const [selectedSwitchTarget, setSelectedSwitchTarget] = useState<AccountListItem | null>(null);
  const [restoreLoading, setRestoreLoading] = useState(false);
  const [switchApplying, setSwitchApplying] = useState(false);
  const setUserInfoFromMe = useAuthStore((s) => s.setUserInfoFromMe);
  const simulatedRole = useSimulatedRoleStore((s) => s.simulatedRole);
  const setSimulatedRole = useSimulatedRoleStore((s) => s.setSimulatedRole);
  useEffect(() => {
    if (simulatedRole && !SIMULATOR_BUSINESS_ROLES_ZH.includes(simulatedRole)) {
      setSimulatedRole(null);
    }
  }, [simulatedRole, setSimulatedRole]);
  const displayName = userInfo?.username ?? 'User';
  const menuRoutes = useMemo(() => {
    const base = buildMenuRoutes(isSuperAdmin, permAllowed, isAdminOrSuperAdmin, hasPermission);
    return filterMenuBySimulatedRole(base as MenuRouteItem[], simulatedRole);
  }, [isSuperAdmin, permAllowed, isAdminOrSuperAdmin, hasPermission, simulatedRole]);
  const isSwitched = Boolean(getStoredOriginalUserId());
  const originalUsername = getStoredOriginalUserInfo()?.username ?? '—';

  useEffect(() => {
    if (!accountSwitchModalOpen) return;
    setAccountSwitchLoading(true);
    getAccountList(accountSwitchQuery.trim(), 50)
      .then(setAccountSwitchList)
      .catch(() => setAccountSwitchList([]))
      .finally(() => setAccountSwitchLoading(false));
  }, [accountSwitchModalOpen, accountSwitchQuery]);

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
          ...(isSuperAdmin || isSwitched
            ? [
                <span
                  key="real-switch-state"
                  style={{
                    marginRight: 8,
                    padding: '2px 8px',
                    background: isSwitched ? '#e6f7ff' : 'transparent',
                    color: isSwitched ? '#0050b3' : '#8c8c8c',
                    border: isSwitched ? '1px solid #91d5ff' : '1px solid #d9d9d9',
                    borderRadius: 4,
                    fontSize: 12,
                  }}
                >
                  {isSwitched ? `测试切换：已切换至 ${displayName}（原：${originalUsername}）` : `当前账号：${displayName}`}
                </span>,
                isSwitched ? (
                  <>
                    <Button
                      key="open-switch-modal"
                      type="default"
                      size="small"
                      style={{ marginRight: 8 }}
                      onClick={() => {
                        setAccountSwitchModalOpen(true);
                        setSelectedSwitchTarget(null);
                        setAccountSwitchQuery('');
                        setAccountSwitchLoading(true);
                        getAccountList('', 50).then(setAccountSwitchList).catch(() => setAccountSwitchList([])).finally(() => setAccountSwitchLoading(false));
                      }}
                    >
                      切换至其他账号
                    </Button>
                    <Button
                      key="restore-account"
                      type="default"
                      size="small"
                      loading={restoreLoading}
                      onClick={() => {
                        const origId = getStoredOriginalUserId();
                        if (!origId) return;
                        setRestoreLoading(true);
                        setStoredUserId(origId);
                        request.get<{ id?: number; username?: string; nickname?: string; roles?: string[] }>('/auth/me')
                          .then((res) => {
                            if (res.data && typeof res.data === 'object') {
                              setUserInfoFromMe({ id: res.data.id, username: res.data.username, roles: res.data.roles });
                            }
                            clearOriginalUser();
                            clearPermAllowedCache();
                            clearEffectiveKeysCache();
                          })
                          .catch(() => {})
                          .finally(() => setRestoreLoading(false));
                      }}
                    >
                      恢复原账号
                    </Button>
                  </>
                ) : (
                  <Button
                    key="open-switch-modal"
                    type="default"
                    size="small"
                    onClick={() => {
                      setAccountSwitchModalOpen(true);
                      setSelectedSwitchTarget(null);
                      setAccountSwitchQuery('');
                      setAccountSwitchLoading(true);
                      getAccountList('', 50).then(setAccountSwitchList).catch(() => setAccountSwitchList([])).finally(() => setAccountSwitchLoading(false));
                    }}
                  >
                    切换账号
                  </Button>
                ),
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
        title="测试账号切换 — 选择目标账号"
        open={accountSwitchModalOpen}
        onCancel={() => setAccountSwitchModalOpen(false)}
        footer={[
          <Button key="cancel" onClick={() => setAccountSwitchModalOpen(false)}>取消</Button>,
          <Button
            key="ok"
            type="primary"
            disabled={!selectedSwitchTarget || selectedSwitchTarget.userId === userInfo?.id}
            loading={switchApplying}
            onClick={() => {
              if (!selectedSwitchTarget || !userInfo?.id) return;
              if (selectedSwitchTarget.userId === userInfo.id) return;
              setSwitchApplying(true);
              const alreadySwitched = Boolean(getStoredOriginalUserId());
              if (!alreadySwitched) {
                setStoredOriginalUserId(getStoredUserId() ?? userInfo.id);
                setStoredOriginalUserInfo(getStoredUserInfo());
              }
              setStoredUserId(selectedSwitchTarget.userId);
              request.get<{ id?: number; username?: string; nickname?: string; roles?: string[] }>('/auth/me')
                .then((res) => {
                  if (res.data && typeof res.data === 'object') {
                    setUserInfoFromMe({ id: res.data.id, username: res.data.username, roles: res.data.roles });
                  }
                  setAccountSwitchModalOpen(false);
                  clearPermAllowedCache();
                  clearEffectiveKeysCache();
                })
                .catch(() => {})
                .finally(() => setSwitchApplying(false));
            }}
          >
            确认切换
          </Button>,
        ]}
        width={640}
        destroyOnClose
      >
        <p style={{ marginBottom: 12, fontSize: 12, color: '#666' }}>
          切换后，页面与接口将按目标账号身份生效；仅用于测试阶段验证。恢复原账号请使用顶栏「恢复原账号」。
        </p>
        <Input.Search
          placeholder="输入用户名或昵称搜索"
          value={accountSwitchQuery}
          onChange={(e) => setAccountSwitchQuery(e.target.value)}
          onSearch={(q) => setAccountSwitchQuery(q ?? accountSwitchQuery)}
          allowClear
          style={{ width: '100%', marginBottom: 12 }}
        />
        <Table<AccountListItem>
          size="small"
          dataSource={accountSwitchList}
          rowKey="userId"
          loading={accountSwitchLoading}
          pagination={false}
          scroll={{ y: 320 }}
          rowClassName={(r) => (selectedSwitchTarget?.userId === r.userId ? 'ant-table-row-selected' : '')}
          onRow={(record) => ({
            onClick: () => setSelectedSwitchTarget(record),
            style: { cursor: 'pointer' },
          })}
          columns={[
            { title: 'ID', dataIndex: 'userId', width: 72, render: (v: number) => v },
            { title: '用户名', dataIndex: 'username', ellipsis: true },
            { title: '昵称', dataIndex: 'nickname', ellipsis: true, render: (v: string | null | undefined) => v ?? '—' },
            { title: '状态', dataIndex: 'enabled', width: 72, render: (v: boolean | undefined) => (v !== false ? '启用' : '停用') },
          ]}
        />
      </Modal>
    </div>
  );
}
