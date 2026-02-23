import request from '@/shared/utils/request';

export interface DictSnapshotDemoRecord {
  id: number;
  typeCode: string;
  itemValue: string;
  itemLabelSnapshot: string;
  note: string | null;
  createdAt: string;
}

export interface DictSnapshotDemoCreateRequest {
  typeCode: string;
  itemValue: string;
  note?: string | null;
}

const BASE = '/v1/admin/dictionary-snapshot-demo';

export async function createSnapshotDemoRecord(
  body: DictSnapshotDemoCreateRequest
): Promise<DictSnapshotDemoRecord> {
  const { data } = await request.post<DictSnapshotDemoRecord>(`${BASE}/records`, body);
  if (!data) throw new Error('No data returned');
  return data;
}

/** Spring Page shape */
export interface DictSnapshotDemoPage {
  content: DictSnapshotDemoRecord[];
  totalElements: number;
}

export async function listSnapshotDemoRecords(params: {
  page?: number;
  size?: number;
}): Promise<DictSnapshotDemoPage> {
  const { data } = await request.get<DictSnapshotDemoPage>(`${BASE}/records`, {
    params: { page: params.page ?? 0, size: params.size ?? 50 },
  });
  if (!data) return { content: [], totalElements: 0 };
  return data;
}
