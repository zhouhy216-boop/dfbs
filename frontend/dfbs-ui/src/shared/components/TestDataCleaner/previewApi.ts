import request from '@/shared/utils/request';

const PREVIEW_BASE = '/v1/admin/test-data-cleaner';

export interface PreviewItemDto {
  moduleId: string;
  count: number;
}

export interface TestDataCleanerPreviewResponse {
  items: PreviewItemDto[];
  totalCount: number;
  requiresResetConfirm: boolean;
  requiresResetReasons: string[];
  invalidModuleIds?: string[];
}

export interface TestDataCleanerPreviewRequest {
  moduleIds: string[];
  includeAttachments?: boolean;
}

export async function fetchPreview(body: TestDataCleanerPreviewRequest): Promise<TestDataCleanerPreviewResponse> {
  const { data } = await request.post<TestDataCleanerPreviewResponse>(`${PREVIEW_BASE}/preview`, body);
  if (!data) throw new Error('预览接口未返回数据');
  return data;
}
