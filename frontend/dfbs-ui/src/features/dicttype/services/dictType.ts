import request from '@/shared/utils/request';

/** Single dictionary type item (matches backend DictTypeItemDto). */
export interface DictTypeItem {
  id: number;
  typeCode: string;
  typeName: string;
  description: string | null;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

/** List response. */
export interface DictTypeListResponse {
  items: DictTypeItem[];
  total: number;
}

/** Create request body. */
export interface CreateDictTypeRequest {
  typeCode: string;
  typeName: string;
  description?: string | null;
  enabled?: boolean;
}

/** Update request body (typeCode immutable). */
export interface UpdateDictTypeRequest {
  typeName?: string;
  description?: string | null;
  enabled?: boolean;
}

const BASE = '/v1/admin/dictionary-types';

export async function listDictTypes(params: {
  page?: number;
  pageSize?: number;
  q?: string;
  enabled?: boolean;
}): Promise<DictTypeListResponse> {
  const { data } = await request.get<DictTypeListResponse>(BASE, { params });
  return data ?? { items: [], total: 0 };
}

export async function createDictType(body: CreateDictTypeRequest): Promise<DictTypeItem> {
  const { data } = await request.post<DictTypeItem>(BASE, body);
  if (!data) throw new Error('No data returned');
  return data;
}

export async function updateDictType(id: number, body: UpdateDictTypeRequest): Promise<DictTypeItem> {
  const { data } = await request.put<DictTypeItem>(`${BASE}/${id}`, body);
  if (!data) throw new Error('No data returned');
  return data;
}

export async function enableDictType(id: number): Promise<DictTypeItem> {
  const { data } = await request.patch<DictTypeItem>(`${BASE}/${id}/enable`);
  if (!data) throw new Error('No data returned');
  return data;
}

export async function disableDictType(id: number): Promise<DictTypeItem> {
  const { data } = await request.patch<DictTypeItem>(`${BASE}/${id}/disable`);
  if (!data) throw new Error('No data returned');
  return data;
}
