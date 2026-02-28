/**
 * PERM API (allowlist-gated). Used by 角色与权限 page only.
 */
import request from '@/shared/utils/request';

export interface ActionItem {
  key: string;
  label: string;
}

export interface ModuleNode {
  key: string;
  label: string;
  actions: string[];
  children: ModuleNode[];
  /** From API (002-04.c): id/parentId/enabled for editing all modules. */
  id?: number;
  parentId?: number | null;
  enabled?: boolean;
}

export interface PermissionTreeResponse {
  keyFormat: string;
  actions: ActionItem[];
  modules: ModuleNode[];
}

export interface RoleResponse {
  id: number;
  roleKey: string;
  label: string;
  enabled?: boolean;
}

export interface RolePermissionsResponse {
  permissionKeys: string[];
}

const ROLES_BASE = '/v1/admin/perm/roles';

export function fetchPermissionTree(): Promise<PermissionTreeResponse> {
  return request.get<PermissionTreeResponse>('/v1/admin/perm/permission-tree').then((res) => res.data);
}

// --- Module management (allowlist-only) ---

const MODULES_BASE = '/v1/admin/perm/modules';

export interface ModuleResponse {
  id: number;
  moduleKey: string;
  label: string;
  parentId: number | null;
  enabled?: boolean;
}

export function createModule(
  moduleKey: string,
  label: string,
  parentId: number | null,
  enabled?: boolean,
): Promise<ModuleResponse> {
  const body: { moduleKey: string; label: string; parentId: number | null; enabled?: boolean } = {
    moduleKey: moduleKey.trim(),
    label: label?.trim() || moduleKey.trim(),
    parentId,
  };
  if (enabled !== undefined) body.enabled = enabled;
  return request.post<ModuleResponse>(MODULES_BASE, body).then((res) => res.data);
}

export function updateModule(
  id: number,
  label: string,
  parentId: number | null,
  enabled?: boolean,
): Promise<ModuleResponse> {
  const body: { label: string; parentId: number | null; enabled?: boolean } = { label, parentId };
  if (enabled !== undefined) body.enabled = enabled;
  return request.put<ModuleResponse>(`${MODULES_BASE}/${id}`, body).then((res) => res.data);
}

export function deleteModule(id: number): Promise<void> {
  return request.delete(`${MODULES_BASE}/${id}`).then(() => undefined);
}

export function setModuleActions(id: number, actionKeys: string[]): Promise<void> {
  return request.put(`${MODULES_BASE}/${id}/actions`, { actionKeys: actionKeys ?? [] }).then(() => undefined);
}

export function fetchRoles(enabledOnly = true): Promise<RoleResponse[]> {
  return request
    .get<RoleResponse[]>(ROLES_BASE, { params: { enabledOnly } })
    .then((res) => res.data);
}

export function createRole(roleKey: string, label: string): Promise<RoleResponse> {
  return request.post<RoleResponse>(ROLES_BASE, { roleKey, label }).then((res) => res.data);
}

export function updateRole(id: number, label: string): Promise<RoleResponse> {
  return request.put<RoleResponse>(`${ROLES_BASE}/${id}`, { label }).then((res) => res.data);
}

export function deleteRole(id: number): Promise<void> {
  return request.delete(`${ROLES_BASE}/${id}`).then(() => undefined);
}

export function fetchRolePermissions(roleId: number): Promise<string[]> {
  return request
    .get<RolePermissionsResponse>(`${ROLES_BASE}/${roleId}/permissions`)
    .then((res) => res.data?.permissionKeys ?? []);
}

export function setRolePermissions(roleId: number, permissionKeys: string[]): Promise<void> {
  return request.put(`${ROLES_BASE}/${roleId}/permissions`, { permissionKeys }).then(() => undefined);
}

/** Atomic save: label + enabled + permissionKeys (replace-style). */
export function saveRoleTemplate(
  roleId: number,
  label: string,
  enabled: boolean,
  permissionKeys: string[],
): Promise<RoleResponse> {
  return request
    .put<RoleResponse>(`${ROLES_BASE}/${roleId}/template`, { label, enabled, permissionKeys })
    .then((res) => res.data);
}

// --- Per-account override (Step-04) ---

export interface UserSummary {
  id: number;
  username: string;
  nickname?: string;
}

export interface AccountOverrideResponse {
  userId: number;
  roleTemplateId: number | null;
  roleTemplateKey: string | null;
  addKeys: string[];
  removeKeys: string[];
  effectiveKeys: string[];
}

export function searchUsers(query: string): Promise<UserSummary[]> {
  return request
    .get<UserSummary[]>('/v1/admin/perm/users', { params: { query: query || '' } })
    .then((res) => res.data ?? []);
}

export function getAccountOverride(userId: number): Promise<AccountOverrideResponse> {
  return request
    .get<AccountOverrideResponse>(`/v1/admin/perm/accounts/${userId}/override`)
    .then((res) => res.data);
}

export function saveAccountOverride(
  userId: number,
  body: { roleTemplateId: number | null; addKeys: string[]; removeKeys: string[] },
): Promise<AccountOverrideResponse> {
  return request
    .put<AccountOverrideResponse>(`/v1/admin/perm/accounts/${userId}/override`, body)
    .then((res) => res.data);
}

// --- Test-only Role-Vision (404 when test utilities disabled) ---

export interface VisionResponse {
  mode: string;
  userId?: number | null;
}

export function getTestVision(): Promise<VisionResponse> {
  return request.get<VisionResponse>('/v1/admin/perm/test/vision').then((res) => res.data);
}

export function setTestVision(body: { mode: string; userId?: number }): Promise<VisionResponse> {
  return request.post<VisionResponse>('/v1/admin/perm/test/vision', body).then((res) => res.data);
}

export function getTestEffectiveKeys(): Promise<string[]> {
  return request
    .get<{ effectiveKeys?: string[] }>('/v1/admin/perm/test/me/effective-keys')
    .then((res) => res.data?.effectiveKeys ?? []);
}

export interface KitAccountSummary {
  username: string;
  nickname: string;
  userId: number;
  effectiveKeyCount: number;
  effectiveKeySample: string[];
}

export function getTestAccounts(): Promise<KitAccountSummary[]> {
  return request.get<KitAccountSummary[]>('/v1/admin/perm/test/accounts').then((res) => res.data ?? []);
}

export function resetTestAccounts(): Promise<KitAccountSummary[]> {
  return request.post<KitAccountSummary[]>('/v1/admin/perm/test/accounts/reset').then((res) => res.data ?? []);
}
