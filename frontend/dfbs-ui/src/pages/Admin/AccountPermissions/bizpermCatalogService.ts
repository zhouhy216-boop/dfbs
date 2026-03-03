/**
 * Business Module Catalog API (/api/v1/admin/bizperm/catalog). Allowlist super-admin only.
 */
import request from '@/shared/utils/request';

const BASE = '/v1/admin/bizperm/catalog';

export interface OpPoint {
  /** Present for persisted op points; null for computed (universe-only) unclassified. */
  id: number | null;
  permissionKey: string;
  cnName: string | null;
  sortOrder: number;
  handledOnly: boolean;
}

export interface CatalogNode {
  id: number;
  cnName: string;
  sortOrder: number;
  children: CatalogNode[];
  ops: OpPoint[];
}

export interface CatalogResponse {
  tree: CatalogNode[];
  unclassified: OpPoint[];
}

export function getCatalog(): Promise<CatalogResponse> {
  return request.get<CatalogResponse>(BASE).then((res) => res.data);
}

/** Admin-readable catalog (same shape). Use for per-account assignment when non-allowlist or unified. */
export function getCatalogRead(): Promise<CatalogResponse> {
  return request.get<CatalogResponse>(`${BASE}/read`).then((res) => res.data);
}

export function createNode(body: { cnName: string; parentId?: number | null; sortOrder?: number | null }): Promise<{ id: number; cnName: string; parentId: number | null; sortOrder: number }> {
  return request.post(BASE + '/nodes', body).then((res) => res.data);
}

export function updateNode(id: number, body: { cnName?: string; parentId?: number | null; sortOrder?: number | null }): Promise<unknown> {
  return request.put(BASE + `/nodes/${id}`, body).then((res) => res.data);
}

export function deleteNode(id: number): Promise<void> {
  return request.delete(BASE + `/nodes/${id}`).then(() => undefined);
}

export function reorderChildren(nodeId: number, orderedIds: number[]): Promise<void> {
  return request.put(BASE + `/nodes/${nodeId}/reorder-children`, { orderedIds }).then(() => undefined);
}

export function upsertOpPoint(body: {
  permissionKey: string;
  cnName?: string | null;
  sortOrder?: number | null;
  handledOnly?: boolean | null;
  nodeId?: number | null;
}): Promise<{ id: number; permissionKey: string; cnName?: string | null; sortOrder: number; handledOnly: boolean; nodeId?: number | null }> {
  return request.post(BASE + '/op-points', body).then((res) => res.data);
}

export function updateOpPoint(id: number, body: { cnName?: string; sortOrder?: number; handledOnly?: boolean; nodeId?: number | null }): Promise<unknown> {
  return request.put(BASE + `/op-points/${id}`, body).then((res) => res.data);
}

export function claimOpPoints(body: { nodeId: number; permissionKeys: string[] }): Promise<void> {
  return request.put(BASE + '/op-points/claim', body).then(() => undefined);
}

export function updateHandledOnly(id: number, handledOnly: boolean): Promise<unknown> {
  return request.put(BASE + `/op-points/${id}/handled-only`, { handledOnly }).then((res) => res.data);
}

// --- Per-account op scope (ALL / HANDLED_ONLY). BIZPERM-260302-001-04 ---
const SCOPE_BASE = '/v1/admin/bizperm/scope';

export interface ScopesResponse {
  userId: number;
  scopes: Record<string, string>;
}

export function getAccountScopes(userId: number): Promise<ScopesResponse> {
  return request.get<ScopesResponse>(`${SCOPE_BASE}/accounts/${userId}/scopes`).then((res) => res.data);
}

export function setAccountScopes(
  userId: number,
  updates: Array<{ permissionKey: string; scope: string }>,
): Promise<ScopesResponse> {
  return request.put<ScopesResponse>(`${SCOPE_BASE}/accounts/${userId}/scopes`, { updates }).then((res) => res.data);
}

// --- Catalog Import/Export (BIZPERM-260302-001-05) ---

export interface ImportValidationError {
  sheet: string;
  row: number;
  col: string;
  message: string;
  machineCode?: string;
}

export interface ImportPreviewSummary {
  nodeRows: number;
  opRows: number;
  errorCount: number;
}

export interface ImportPreviewResponse {
  valid: boolean;
  summary: ImportPreviewSummary;
  errors: ImportValidationError[];
}

export interface ImportApplySummary {
  nodesUpdated: number;
  opsCreated: number;
  opsUpdated: number;
}

export interface ImportApplyResponse {
  summary: ImportApplySummary;
}

/** 导出目录为 XLSX，触发浏览器下载。文件名由服务端 Content-Disposition 或默认中文名。 */
export async function exportCatalog(): Promise<void> {
  const res = await request.get<Blob>(`${BASE}/export`, {
    responseType: 'blob',
  });
  const disposition = (res.headers as Record<string, string>)['content-disposition'] ?? '';
  let filename = `业务模块目录_${new Date().toISOString().slice(0, 10)}.xlsx`;
  if (disposition) {
    const utf8Match = disposition.match(/filename\*=UTF-8''(.+?)(?:;|$)/);
    if (utf8Match) filename = decodeURIComponent(utf8Match[1].trim());
    else {
      const m = disposition.match(/filename="?([^";]+)"?/);
      if (m) filename = m[1].trim();
    }
  }
  const blob = res.data instanceof Blob ? res.data : new Blob([res.data]);
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = filename;
  a.click();
  URL.revokeObjectURL(a.href);
}

export function importPreview(file: File): Promise<ImportPreviewResponse> {
  const form = new FormData();
  form.append('file', file);
  return request
    .post<ImportPreviewResponse>(`${BASE}/import/preview`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((res) => res.data);
}

export function importApply(file: File): Promise<ImportApplyResponse> {
  const form = new FormData();
  form.append('file', file);
  return request
    .post<ImportApplyResponse>(`${BASE}/import/apply`, form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((res) => res.data);
}
