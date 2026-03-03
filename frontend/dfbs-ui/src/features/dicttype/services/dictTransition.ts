import request from '@/shared/utils/request';

const ADMIN_BASE = '/v1/admin/dictionary-types';
const READ_BASE = '/v1/dictionaries';

/** One transition edge (admin list response). */
export interface TransitionEdgeDto {
  id: number;
  fromValue: string;
  fromLabel: string;
  toValue: string;
  toLabel: string;
  enabled: boolean;
}

/** Admin list response. */
export interface TransitionListResponse {
  transitions: TransitionEdgeDto[];
}

/** One edge for upsert request. */
export interface TransitionEdgeRequest {
  fromValue: string;
  toValue: string;
  enabled?: boolean;
}

/** Upsert request body. */
export interface UpsertTransitionsRequest {
  transitions: TransitionEdgeRequest[];
}

export async function listTransitionsAdmin(typeId: number): Promise<TransitionListResponse> {
  const { data } = await request.get<TransitionListResponse>(`${ADMIN_BASE}/${typeId}/transitions`);
  return data ?? { transitions: [] };
}

export async function upsertTransitionsAdmin(
  typeId: number,
  body: UpsertTransitionsRequest
): Promise<TransitionListResponse> {
  const { data } = await request.post<TransitionListResponse>(`${ADMIN_BASE}/${typeId}/transitions`, body);
  return data ?? { transitions: [] };
}

/** Read contract (business): one edge, no internal id. */
export interface TransitionEdgeReadDto {
  fromValue: string;
  toValue: string;
  enabled: boolean;
  fromLabel?: string;
  toLabel?: string;
}

export interface TransitionsReadResponse {
  typeCode: string;
  transitions: TransitionEdgeReadDto[];
}

/** Public read API for preview. */
export async function getTransitionsRead(
  typeCode: string,
  includeDisabled = false
): Promise<TransitionsReadResponse> {
  const { data } = await request.get<TransitionsReadResponse>(
    `${READ_BASE}/${encodeURIComponent(typeCode)}/transitions`,
    { params: { includeDisabled } }
  );
  if (!data) return { typeCode, transitions: [] };
  return data;
}
