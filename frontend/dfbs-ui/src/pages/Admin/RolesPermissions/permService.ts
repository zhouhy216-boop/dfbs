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
