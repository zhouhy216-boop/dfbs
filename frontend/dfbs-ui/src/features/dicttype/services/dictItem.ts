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
    page?: number;
    pageSize?: number;
  }
): Promise<DictItemListResponse> {
  const { data } = await request.get<DictItemListResponse>(`${TYPES_BASE}/${typeId}/items`, {
    params: { ...params, parentId: params.parentId ?? undefined },
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
