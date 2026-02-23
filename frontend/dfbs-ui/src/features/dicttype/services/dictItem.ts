import request from '@/shared/utils/request';

/** Single dictionary item (matches backend DictItemDto). */
export interface DictItem {
  id: number;
  typeId: number;
  itemValue: string;
  itemLabel: string;
  sortOrder: number;
  enabled: boolean;
  note: string | null;
  parentId: number | null;
  createdAt?: string;
  updatedAt: string;
}

/** List response. */
export interface DictItemListResponse {
  items: DictItem[];
  total: number;
}

/** Create request body. */
export interface CreateDictItemRequest {
  itemValue: string;
  itemLabel: string;
  sortOrder?: number;
  enabled?: boolean;
  note?: string | null;
  parentId?: number | null;
}

/** Update request body (itemValue immutable). */
export interface UpdateDictItemRequest {
  itemLabel?: string;
  sortOrder?: number;
  enabled?: boolean;
  note?: string | null;
  parentId?: number | null;
}

const TYPES_BASE = '/v1/admin/dictionary-types';
const ITEMS_BASE = '/v1/admin/dictionary-items';

export async function listItems(
  typeId: number,
  params: {
    q?: string;
    enabled?: boolean;
    parentId?: number | null;
    /** When true, return only root items (parentId null). Used when 父级=全部. */
    rootsOnly?: boolean;
    page?: number;
    pageSize?: number;
  }
): Promise<DictItemListResponse> {
  const { parentId, rootsOnly, ...rest } = params;
  const query: Record<string, unknown> = { ...rest };
  if (rootsOnly === true) {
    query.rootsOnly = true;
  } else if (parentId != null) {
    query.parentId = parentId;
  }
  const { data } = await request.get<DictItemListResponse>(`${TYPES_BASE}/${typeId}/items`, {
    params: query,
  });
  return data ?? { items: [], total: 0 };
}

export async function createItem(typeId: number, body: CreateDictItemRequest): Promise<DictItem> {
  const { data } = await request.post<DictItem>(`${TYPES_BASE}/${typeId}/items`, body);
  if (!data) throw new Error('No data returned');
  return data;
}

export async function updateItem(itemId: number, body: UpdateDictItemRequest): Promise<DictItem> {
  const { data } = await request.put<DictItem>(`${ITEMS_BASE}/${itemId}`, body);
  if (!data) throw new Error('No data returned');
  return data;
}

export async function enableItem(itemId: number): Promise<DictItem> {
  const { data } = await request.patch<DictItem>(`${ITEMS_BASE}/${itemId}/enable`);
  if (!data) throw new Error('No data returned');
  return data;
}

export async function disableItem(itemId: number): Promise<DictItem> {
  const { data } = await request.patch<DictItem>(`${ITEMS_BASE}/${itemId}/disable`);
  if (!data) throw new Error('No data returned');
  return data;
}

export async function deleteDictItem(itemId: number): Promise<void> {
  await request.delete(`${ITEMS_BASE}/${itemId}`);
}

/** Reorder items within same type and same parent. */
export async function reorderItems(
  typeId: number,
  parentId: number | null,
  orderedItemIds: number[]
): Promise<DictItemListResponse> {
  const { data } = await request.patch<DictItemListResponse>(
    `${TYPES_BASE}/${typeId}/items/reorder`,
    { parentId, orderedItemIds }
  );
  return data ?? { items: [], total: 0 };
}
