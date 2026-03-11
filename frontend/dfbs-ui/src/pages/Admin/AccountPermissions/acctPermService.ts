/**
 * Admin-only Account & Permissions API (/api/v1/admin/account-permissions).
 * Used by 账号与权限 page, Accounts tab.
 */
import request from '@/shared/utils/request';

export interface UserSummary {
  id: number;
  username: string;
  nickname?: string;
  enabled?: boolean;
  primaryBusinessRole?: string | null;
  orgPersonId?: number | null;
}

/** One row from GET /account-list (full account table for Accounts tab). */
export interface AccountListItem {
  userId: number;
  username: string;
  nickname?: string | null;
  enabled?: boolean | null;
  orgPersonId?: number | null;
  position?: string | null;
  department?: string | null;
  roleTemplateId?: number | null;
  roleTemplateLabel?: string | null;
  primaryBusinessRole?: string | null;
}

export interface AccountOverrideResponse {
  userId: number;
  roleTemplateId: number | null;
  roleTemplateKey: string | null;
  addKeys: string[];
  removeKeys: string[];
  effectiveKeys: string[];
}

export interface RoleResponse {
  id: number;
  roleKey: string;
  label: string;
  enabled?: boolean;
  description?: string;
}

const BASE = '/v1/admin/account-permissions';

export interface PersonOptionForBinding {
  personId: number;
  name: string;
  orgUnitLabel?: string;
  title?: string;
  phone?: string;
  email?: string;
}

export interface AccountSummaryResponse {
  id: number;
  username: string;
  nickname?: string;
  enabled?: boolean;
  orgPersonId?: number;
  primaryBusinessRole?: string | null;
}

export function getPeopleOptions(query: string): Promise<PersonOptionForBinding[]> {
  return request
    .get<PersonOptionForBinding[]>(`${BASE}/people`, { params: { query: query || '' } })
    .then((res) => res.data ?? []);
}

export function createAccount(body: {
  orgPersonId: number;
  username: string;
  nickname?: string;
  roleTemplateId?: number | null;
  primaryBusinessRole: string;
}): Promise<AccountSummaryResponse> {
  return request
    .post<AccountSummaryResponse>(`${BASE}/accounts`, {
      orgPersonId: body.orgPersonId,
      username: body.username.trim(),
      nickname: body.nickname?.trim() || undefined,
      roleTemplateId: body.roleTemplateId ?? undefined,
      primaryBusinessRole: body.primaryBusinessRole?.trim() || undefined,
    })
    .then((res) => res.data);
}

export function updateAccount(
  userId: number,
  body: { nickname?: string; primaryBusinessRole?: string | null; username?: string | null; orgPersonId?: number | null },
): Promise<AccountSummaryResponse> {
  return request
    .put<AccountSummaryResponse>(`${BASE}/accounts/${userId}`, body)
    .then((res) => res.data);
}

export function setAccountEnabled(userId: number, enabled: boolean): Promise<void> {
  return request.put(`${BASE}/accounts/${userId}/enabled`, { enabled }).then(() => undefined);
}

export function resetPassword(userId: number, newPassword?: string): Promise<void> {
  return request
    .post(`${BASE}/accounts/${userId}/reset-password`, { newPassword: newPassword ?? undefined })
    .then(() => undefined);
}

export function searchUsers(query: string): Promise<UserSummary[]> {
  return request
    .get<UserSummary[]>(`${BASE}/users`, { params: { query: query || '' } })
    .then((res) => res.data ?? []);
}

/** Account list for Accounts tab: empty query = first N (default 50); with query = filtered. limit 1–200. */
export function getAccountList(query = '', limit = 50): Promise<AccountListItem[]> {
  const params: { query?: string; limit?: number } = { limit: Math.min(200, Math.max(1, limit)) };
  if (query.trim() !== '') params.query = query.trim();
  return request
    .get<AccountListItem[]>(`${BASE}/account-list`, { params })
    .then((res) => res.data ?? []);
}

export function getUser(id: number): Promise<UserSummary> {
  return request.get<UserSummary>(`${BASE}/users/${id}`).then((res) => res.data);
}

export function getAccountOverride(userId: number): Promise<AccountOverrideResponse> {
  return request
    .get<AccountOverrideResponse>(`${BASE}/accounts/${userId}/override`)
    .then((res) => res.data);
}

export function saveAccountOverride(
  userId: number,
  body: { roleTemplateId: number | null; addKeys: string[]; removeKeys: string[] },
): Promise<AccountOverrideResponse> {
  return request
    .put<AccountOverrideResponse>(`${BASE}/accounts/${userId}/override`, body)
    .then((res) => res.data);
}

export function getRoles(enabledOnly = true): Promise<RoleResponse[]> {
  return request
    .get<RoleResponse[]>(`${BASE}/roles`, { params: { enabledOnly } })
    .then((res) => res.data ?? []);
}

export function getRolePermissions(roleId: number): Promise<string[]> {
  return request
    .get<{ permissionKeys: string[] }>(`${BASE}/roles/${roleId}/permissions`)
    .then((res) => res.data?.permissionKeys ?? []);
}

/** Create role. Omit roleKey (or pass empty) to let backend auto-generate. */
export function createRole(body: {
  label: string;
  description?: string;
  enabled?: boolean;
  roleKey?: string;
}): Promise<RoleResponse> {
  const payload: Record<string, unknown> = {
    label: body.label?.trim() || '',
    enabled: body.enabled !== false,
  };
  if (body.description != null && body.description !== '') payload.description = body.description.trim();
  if (body.roleKey != null && body.roleKey.trim() !== '') payload.roleKey = body.roleKey.trim();
  return request.post<RoleResponse>(`${BASE}/roles`, payload).then((res) => res.data);
}

export function cloneRole(roleId: number): Promise<RoleResponse> {
  return request.post<RoleResponse>(`${BASE}/roles/${roleId}/clone`).then((res) => res.data);
}

export function updateRole(id: number, label: string, enabled: boolean): Promise<RoleResponse> {
  return request.put<RoleResponse>(`${BASE}/roles/${id}`, { label, enabled }).then((res) => res.data);
}

export function deleteRole(id: number): Promise<void> {
  return request.delete(`${BASE}/roles/${id}`).then(() => undefined);
}

/** Atomic save: label + enabled + permissionKeys (replace-style). */
export function saveRoleTemplate(
  roleId: number,
  label: string,
  enabled: boolean,
  permissionKeys: string[],
  description?: string,
): Promise<RoleResponse> {
  const body: { label: string; enabled: boolean; permissionKeys: string[]; description?: string } = {
    label,
    enabled,
    permissionKeys,
  };
  if (description !== undefined) body.description = description;
  return request.put<RoleResponse>(`${BASE}/roles/${roleId}/template`, body).then((res) => res.data);
}

// ---------- Default password (admin-managed; no plaintext in response) ----------

export interface DefaultPasswordStatus {
  configured: boolean;
  updatedAt?: string;
  updatedByUserId?: number | null;
}

export function getDefaultPasswordStatus(): Promise<DefaultPasswordStatus> {
  return request.get<DefaultPasswordStatus>(`${BASE}/auth/default-password/status`).then((res) => res.data);
}

export function setDefaultPassword(defaultPassword: string): Promise<DefaultPasswordStatus> {
  return request
    .put<DefaultPasswordStatus>(`${BASE}/auth/default-password`, { defaultPassword })
    .then((res) => res.data);
}
