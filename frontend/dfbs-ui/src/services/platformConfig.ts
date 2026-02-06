import request from '@/utils/request';

/** Matches backend PlatformConfigResponse. */
export interface PlatformConfigItem {
  id: number;
  platformName: string;
  platformCode: string;
  isActive: boolean;
  ruleUniqueEmail: boolean;
  ruleUniquePhone: boolean;
  ruleUniqueOrgName: boolean;
  codeValidatorType: 'NONE' | 'UPPERCASE' | 'CHINESE' | 'MIXED';
  createdAt?: string;
  updatedAt?: string;
}

/** Create/update request body. */
export interface PlatformConfigRequest {
  platformName: string;
  platformCode: string;
  isActive?: boolean;
  ruleUniqueEmail?: boolean;
  ruleUniquePhone?: boolean;
  ruleUniqueOrgName?: boolean;
  codeValidatorType?: 'NONE' | 'UPPERCASE' | 'CHINESE' | 'MIXED';
}

const BASE = '/v1/platform-configs';

export async function getPlatformConfigs(): Promise<PlatformConfigItem[]> {
  const { data } = await request.get<PlatformConfigItem[]>(BASE);
  return data ?? [];
}

export async function addPlatformConfig(body: PlatformConfigRequest): Promise<PlatformConfigItem> {
  const { data } = await request.post<PlatformConfigItem>(BASE, body);
  if (!data) throw new Error('No data returned');
  return data;
}

export async function updatePlatformConfig(id: number, body: PlatformConfigRequest): Promise<PlatformConfigItem> {
  const { data } = await request.put<PlatformConfigItem>(`${BASE}/${id}`, body);
  if (!data) throw new Error('No data returned');
  return data;
}

export async function togglePlatformActive(id: number): Promise<PlatformConfigItem> {
  const { data } = await request.patch<PlatformConfigItem>(`${BASE}/${id}/toggle`);
  if (!data) throw new Error('No data returned');
  return data;
}
