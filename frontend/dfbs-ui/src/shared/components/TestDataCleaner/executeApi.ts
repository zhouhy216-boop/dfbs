import request from '@/shared/utils/request';

const EXECUTE_BASE = '/v1/admin/test-data-cleaner';

export interface TableResultDto {
  table: string;
  deleted: number;
  status: string;
  error?: string | null;
}

export interface ModuleExecuteItemDto {
  moduleId: string;
  tables: TableResultDto[];
  moduleDeletedTotal: number;
  moduleStatus: string;
}

export interface TestDataCleanerExecuteResponse {
  startedAt: string;
  finishedAt: string;
  requiresResetConfirm: boolean;
  requiresResetReasons: string[];
  invalidModuleIds: string[];
  items: ModuleExecuteItemDto[];
  totalDeleted: number;
  status: string;
  redisMessage: string;
}

export interface TestDataCleanerExecuteRequest {
  moduleIds: string[];
  confirmText?: string | null;
  includeAttachments?: boolean;
}

export async function fetchExecute(body: TestDataCleanerExecuteRequest): Promise<TestDataCleanerExecuteResponse> {
  const { data } = await request.post<TestDataCleanerExecuteResponse>(`${EXECUTE_BASE}/execute`, body);
  if (!data) throw new Error('执行接口未返回数据');
  return data;
}
