/**
 * PERM read-only API (allowlist-gated). Used by 角色与权限 page only.
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

export function fetchPermissionTree(): Promise<PermissionTreeResponse> {
  return request.get<PermissionTreeResponse>('/v1/admin/perm/permission-tree').then((res) => res.data);
}
