import type { ReactNode } from 'react';

/**
 * Role-to-UI gating matrix v1 baseline.
 *
 * Source of truth for which UI areas are gated and how they align to the alignment pack.
 * Reuse codes/names from:
 *   - docs/product/BUSINESS_MAP_v0.1.md (business module codes M01..M10; system = M_SYS)
 *   - docs/product/MODULE_ROUTE_ANCHORS_v0.1.md (route anchors)
 *   - docs/product/PROCESS_MAP_v0.1.md (process nodes; use "Support/none" where no valid node)
 *   - docs/product/OBJECT_MAP_v0.1.md (object scope)
 *
 * RULES (do not add gated UI points without following these):
 * 1. No new gated UI point may be added without anchors (business module, route, process, object scope).
 * 2. System/admin pages that lack a valid process node in PROCESS_MAP v0.1 must use temporary
 *    marker "Support/none" for process node(s) in v1. Document where "Support/none" is applied.
 *    See SUPPORT_NONE_ENTRY_IDS for the list of entries using this marker.
 * 3. Object scope for system/admin pages: OBJECT_MAP v0.1 does not list system objects; use
 *    descriptive names (e.g. 字典, 账号, 权限) and note anchor-gap if needed.
 */

/** Temporary process marker when no PROCESS_MAP v0.1 node applies (e.g. system-maintenance / admin pages). */
export const PROCESS_MARKER_SUPPORT_NONE = 'Support/none';

export interface RoleToUiGatingEntry {
  /** Stable unique id for this gated point. */
  id: string;
  /** UI area name / gated point label (human-readable). */
  uiAreaName: string;
  /** Business module code(s) from BUSINESS_MAP_v0.1 (e.g. M01, M04, M_SYS). */
  businessModuleCodes: string[];
  /** Route anchor(s) from MODULE_ROUTE_ANCHORS_v0.1 (e.g. /shipments, /work-orders). Use "*" only for global shell. */
  routeAnchors: string[];
  /** Process node(s) from PROCESS_MAP_v0.1. Use PROCESS_MARKER_SUPPORT_NONE for system/admin pages with no mapped process. */
  processNodes: string[];
  /** Object scope from OBJECT_MAP_v0.1 (e.g. 发货交付单, 工单). Empty for global/shell-only. */
  objectScope: string[];
  /** Allowed simulated role set for visibility (role names as in simulator dropdown). Empty = not driven by simulator in v1. */
  allowedSimulatedRoleSet: string[];
  /** Optional note: temporary markers, anchor caveats, or permission key references. */
  note?: string;
}

/**
 * v1 baseline: Role-to-UI gating matrix.
 * Only currently obvious gated UI areas from repo facts; no guessing beyond alignment pack.
 */
export const ROLE_TO_UI_GATING_MATRIX: RoleToUiGatingEntry[] = [
  {
    id: 'role-simulator-topbar',
    uiAreaName: 'Role simulator (top bar: dropdown + badge + disclaimer)',
    businessModuleCodes: ['M_SYS'],
    routeAnchors: ['*'],
    processNodes: [PROCESS_MARKER_SUPPORT_NONE],
    objectScope: ['UI session state', 'simulated role view state'],
    allowedSimulatedRoleSet: ['Super Admin', 'Admin'],
    note: 'Global shell; visible only when user is admin/super-admin (server-confirmed). Not a permission; UI-only simulation.',
  },
  {
    id: 'shipments-page',
    uiAreaName: 'Shipments list and detail',
    businessModuleCodes: ['M04'],
    routeAnchors: ['/shipments'],
    processNodes: ['主线_交付'],
    objectScope: ['发货交付单'],
    allowedSimulatedRoleSet: ['Super Admin', 'Admin', 'Operator'],
    note: 'Viewer excluded in v1 so simulator produces visible left-nav change. Actual backend permission shipment.shipments:VIEW unchanged.',
  },
  {
    id: 'work-orders-page',
    uiAreaName: 'Work orders list and detail',
    businessModuleCodes: ['M05'],
    routeAnchors: ['/work-orders', '/work-orders/:id'],
    processNodes: ['主线_售后工单'],
    objectScope: ['工单'],
    allowedSimulatedRoleSet: ['Super Admin', 'Admin', 'Operator'],
    note: 'Viewer excluded in v1 so simulator produces visible left-nav change. Actual backend permission work_order:VIEW unchanged.',
  },
  {
    id: 'platform-orgs-page',
    uiAreaName: 'Platform orgs (platform management)',
    businessModuleCodes: ['M08'],
    routeAnchors: ['/platform/orgs'],
    processNodes: ['支线_入网准备'],
    objectScope: ['平台信息', '平台账号/平台侧记录'],
    allowedSimulatedRoleSet: ['Super Admin', 'Admin', 'Operator'],
    note: 'Viewer excluded in v1 for visible simulator change. Actual backend permission platform_application.orgs:VIEW unchanged.',
  },
  {
    id: 'platform-applications-page',
    uiAreaName: 'Platform applications',
    businessModuleCodes: ['M08'],
    routeAnchors: ['/platform/applications', '/platform/apply'],
    processNodes: ['支线_入网准备'],
    objectScope: ['平台信息', 'SIM卡', '入网准备记录'],
    allowedSimulatedRoleSet: ['Super Admin', 'Admin'],
    note: 'Operator and Viewer excluded in v1 so simulator produces visible left-nav change. Actual backend permission unchanged.',
  },
  {
    id: 'admin-data-dictionary',
    uiAreaName: 'Data dictionary (admin)',
    businessModuleCodes: ['M_SYS'],
    routeAnchors: ['/admin/data-dictionary'],
    processNodes: [PROCESS_MARKER_SUPPORT_NONE],
    objectScope: ['字典'],
    allowedSimulatedRoleSet: ['Super Admin'],
    note: 'Super-admin only. Process node uses Support/none (no PROCESS_MAP v0.1 system-maintenance node).',
  },
  {
    id: 'admin-dictionary-types',
    uiAreaName: 'Dictionary types and items',
    businessModuleCodes: ['M_SYS'],
    routeAnchors: ['/admin/dictionary-types', '/admin/dictionary-types/:typeId/items', '/admin/dictionary-types/:typeId/transitions'],
    processNodes: [PROCESS_MARKER_SUPPORT_NONE],
    objectScope: ['字典'],
    allowedSimulatedRoleSet: ['Super Admin'],
    note: 'Super-admin only. Support/none for process.',
  },
  {
    id: 'admin-org-tree',
    uiAreaName: 'Org tree and structure',
    businessModuleCodes: ['M_SYS'],
    routeAnchors: ['/admin/org-tree', '/admin/org-levels', '/admin/org-change-logs'],
    processNodes: [PROCESS_MARKER_SUPPORT_NONE],
    objectScope: ['组织架构'],
    allowedSimulatedRoleSet: ['Super Admin'],
    note: 'Super-admin only. Support/none for process.',
  },
  {
    id: 'admin-account-permissions',
    uiAreaName: 'Account and permissions',
    businessModuleCodes: ['M_SYS'],
    routeAnchors: ['/admin/account-permissions'],
    processNodes: [PROCESS_MARKER_SUPPORT_NONE],
    objectScope: ['账号', '权限'],
    allowedSimulatedRoleSet: ['Super Admin', 'Admin'],
    note: 'Admin or Super-admin. Support/none for process.',
  },
  {
    id: 'admin-roles-permissions',
    uiAreaName: 'Roles and permissions (perm catalog)',
    businessModuleCodes: ['M_SYS'],
    routeAnchors: ['/admin/roles-permissions'],
    processNodes: [PROCESS_MARKER_SUPPORT_NONE],
    objectScope: ['角色', '权限'],
    allowedSimulatedRoleSet: ['Super Admin'],
    note: 'Perm allowlist (Perm super-admin). Support/none for process.',
  },
  {
    id: 'admin-test-data-cleaner',
    uiAreaName: 'Test data cleaner (top bar action)',
    businessModuleCodes: ['M_SYS'],
    routeAnchors: ['*'],
    processNodes: [PROCESS_MARKER_SUPPORT_NONE],
    objectScope: [],
    allowedSimulatedRoleSet: ['Super Admin'],
    note: 'Super-admin only; top bar action. Support/none for process.',
  },
];

/** All entry ids for validation and consumption. */
export const ROLE_TO_UI_GATING_ENTRY_IDS: string[] = ROLE_TO_UI_GATING_MATRIX.map((e) => e.id);

/** Find matrix entry by id. */
export function getRoleToUiGatingEntry(id: string): RoleToUiGatingEntry | undefined {
  return ROLE_TO_UI_GATING_MATRIX.find((e) => e.id === id);
}

/**
 * Whether the Shipment page workflow action area is allowed for the given simulated role (UI-only).
 * Used for drawer "可执行操作" buttons: when false, buttons are disabled with tooltip "该角色不可操作".
 * When simulatedRole is null or '__none__', returns true (baseline; no simulator override).
 * Rule source: matrix entry id 'shipments-page' allowedSimulatedRoleSet.
 */
export function isShipmentWorkflowActionAllowedForSimulatedRole(simulatedRole: string | null): boolean {
  if (!simulatedRole || simulatedRole === '__none__') return true;
  const entry = getRoleToUiGatingEntry('shipments-page');
  if (!entry) return true;
  return entry.allowedSimulatedRoleSet.includes(simulatedRole);
}

/**
 * Whether Work Order page key actions are allowed for the given simulated role (UI-only).
 * When false, list/detail actions (新建工单, 受理, 派单, 驳回, 接单) are disabled with tooltip "该角色不可操作".
 * Rule source: matrix entry id 'work-orders-page' allowedSimulatedRoleSet.
 */
export function isWorkOrderActionAllowedForSimulatedRole(simulatedRole: string | null): boolean {
  if (!simulatedRole || simulatedRole === '__none__') return true;
  const entry = getRoleToUiGatingEntry('work-orders-page');
  if (!entry) return true;
  return entry.allowedSimulatedRoleSet.includes(simulatedRole);
}

/**
 * Whether Platform Org (平台管理) page key actions are allowed for the given simulated role (UI-only).
 * When false, page/row actions (销售申请, 服务申请, 营企申请, 新建机构, 编辑, 删除) are disabled with tooltip "该角色不可操作".
 * Rule source: matrix entry id 'platform-orgs-page' allowedSimulatedRoleSet.
 */
export function isPlatformOrgActionAllowedForSimulatedRole(simulatedRole: string | null): boolean {
  if (!simulatedRole || simulatedRole === '__none__') return true;
  const entry = getRoleToUiGatingEntry('platform-orgs-page');
  if (!entry) return true;
  return entry.allowedSimulatedRoleSet.includes(simulatedRole);
}

/**
 * Whether Platform Application (申请管理) page key actions are allowed for the given simulated role (UI-only).
 * When false, actions (通过, 驳回, 提交至管理员, 关闭申请) are disabled with tooltip "该角色不可操作".
 * Rule source: matrix entry id 'platform-applications-page' allowedSimulatedRoleSet.
 */
export function isPlatformApplicationActionAllowedForSimulatedRole(simulatedRole: string | null): boolean {
  if (!simulatedRole || simulatedRole === '__none__') return true;
  const entry = getRoleToUiGatingEntry('platform-applications-page');
  if (!entry) return true;
  return entry.allowedSimulatedRoleSet.includes(simulatedRole);
}

/** Entries that use process marker Support/none (anchor-gap documentation). */
export const SUPPORT_NONE_ENTRY_IDS: string[] = ROLE_TO_UI_GATING_MATRIX.filter((e) =>
  e.processNodes.includes(PROCESS_MARKER_SUPPORT_NONE)
).map((e) => e.id);

/**
 * Whether a route path is visible for the given simulated role (UI-only gating).
 * When simulatedRole is null or '__none__', returns true (no simulator override; baseline menu).
 * When simulatedRole is set: returns true if no matrix entry covers this path (fallback: show),
 * or if at least one covering entry has this role in allowedSimulatedRoleSet.
 */
function routeAnchorMatchesPath(anchor: string, path: string): boolean {
  if (path === anchor) return true;
  if (!anchor.includes(':')) return path.startsWith(anchor + '/');
  const prefix = anchor.split(':')[0];
  return path.startsWith(prefix + '/') || path === prefix;
}

export function isRouteVisibleForSimulatedRole(path: string, simulatedRole: string | null): boolean {
  if (!simulatedRole || simulatedRole === '__none__') return true;
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  const covering = ROLE_TO_UI_GATING_MATRIX.filter((e) =>
    e.routeAnchors.some((anchor) => routeAnchorMatchesPath(anchor, normalizedPath))
  );
  if (covering.length === 0) return true;
  return covering.some((e) => e.allowedSimulatedRoleSet.includes(simulatedRole));
}

/** Menu route item shape used by layout (path optional for groups). */
export interface MenuRouteItem {
  path?: string;
  name?: string;
  key?: string;
  icon?: ReactNode;
  routes?: MenuRouteItem[];
}

/**
 * Filters menu route tree by simulated role. When simulatedRole is null/__none__, returns routes unchanged.
 * Uses show/hide: items not allowed for the role are removed (v1 consistent behavior).
 * Parent groups (e.g. 售后服务, 物流管理, 平台&网卡管理) are removed when they have zero visible children
 * so the real left nav does not show an empty group.
 */
export function filterMenuBySimulatedRole(routes: MenuRouteItem[], simulatedRole: string | null): MenuRouteItem[] {
  if (!routes || routes.length === 0) return routes;
  return routes
    .map((r) => {
      if (r.routes && r.routes.length > 0) {
        const filtered = filterMenuBySimulatedRole(r.routes, simulatedRole);
        if (filtered.length === 0) return null;
        return { ...r, routes: filtered };
      }
      if (r.path != null) return isRouteVisibleForSimulatedRole(r.path, simulatedRole) ? r : null;
      return r;
    })
    .filter((x): x is MenuRouteItem => x != null);
}
