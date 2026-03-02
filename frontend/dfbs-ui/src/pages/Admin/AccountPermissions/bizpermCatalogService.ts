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
