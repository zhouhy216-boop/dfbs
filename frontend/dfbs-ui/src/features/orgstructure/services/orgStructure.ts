import request from '@/shared/utils/request';

const BASE = '/v1/org-structure';

// ----- Levels -----
export interface OrgLevelItem {
  id: number;
  orderIndex: number;
  displayName: string;
  isEnabled: boolean;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
}

export async function listLevels(): Promise<OrgLevelItem[]> {
  const { data } = await request.get<OrgLevelItem[]>(`${BASE}/levels`);
  return data ?? [];
}

/** Configurable levels only (excludes system-fixed 公司). For Level Config page. */
export async function listConfigurableLevels(): Promise<OrgLevelItem[]> {
  const { data } = await request.get<OrgLevelItem[]>(`${BASE}/levels/configurable`);
  return data ?? [];
}

export async function listEnabledLevels(): Promise<OrgLevelItem[]> {
  const { data } = await request.get<OrgLevelItem[]>(`${BASE}/levels/enabled`);
  return data ?? [];
}

export async function createLevel(body: { orderIndex?: number; displayName: string }): Promise<OrgLevelItem> {
  const { data } = await request.post<OrgLevelItem>(`${BASE}/levels`, body);
  if (!data) throw new Error('No data');
  return data;
}

export async function updateLevel(id: number, body: { orderIndex?: number; displayName?: string; isEnabled?: boolean }): Promise<OrgLevelItem> {
  const { data } = await request.put<OrgLevelItem>(`${BASE}/levels/${id}`, body);
  if (!data) throw new Error('No data');
  return data;
}

/** Reorder configurable levels (company excluded). Returns new ordered list. */
export async function reorderLevels(orderedIds: number[]): Promise<OrgLevelItem[]> {
  const { data } = await request.put<OrgLevelItem[]>(`${BASE}/levels/reorder`, { orderedIds });
  return data ?? [];
}

export interface CanResetLevelsResult {
  canReset: boolean;
  message?: string | null;
  nodeCount?: number;
  affiliationCount?: number;
}

export async function canResetLevels(): Promise<CanResetLevelsResult> {
  const { data } = await request.get<CanResetLevelsResult>(`${BASE}/levels/can-reset`);
  return data ?? { canReset: false, message: '已被使用，不能重置，请手动改名/排序' };
}

export async function resetLevelsToDefault(): Promise<OrgLevelItem[]> {
  const { data } = await request.post<OrgLevelItem[]>(`${BASE}/levels/reset-to-default`);
  return data ?? [];
}

/** Dev-only: clear org-structure test data and restore default levels. */
export async function resetDevOrgStructure(): Promise<OrgLevelItem[]> {
  const { data } = await request.post<OrgLevelItem[]>(`${BASE}/reset-dev`);
  return data ?? [];
}

export interface ResetAvailabilityResult {
  allowed: boolean;
  reason?: string | null;
  personCount?: number;
  affiliationCount?: number;
  nodeCount?: number;
}

/** Super Admin: probe for reset tooling (blocked when people/affiliations exist). */
export async function getResetAvailability(): Promise<ResetAvailabilityResult> {
  const { data } = await request.get<ResetAvailabilityResult>(`${BASE}/reset-availability`);
  return data ?? { allowed: false, reason: '无法获取' };
}

/** Safe reset: clear org tree + logs + levels, restore defaults. Requires confirmText: "RESET". Blocked if any people/affiliations. */
export async function resetOrgStructureTooling(body: { confirmText: string }): Promise<OrgLevelItem[]> {
  const { data } = await request.post<OrgLevelItem[]>(`${BASE}/reset`, body);
  return data ?? [];
}

/** Reset all org-structure test data (position bindings, enabled, affiliations, people, nodes, change logs, levels); restore default levels + root 公司 node. Requires confirmText: "RESET". Does NOT touch accounts/permissions/templates. */
export interface ResetAllResult {
  positionBindingCleared?: number;
  positionEnabledCleared?: number;
  affiliationCleared?: number;
  personCleared?: number;
  nodeCleared?: number;
  changeLogCleared?: number;
  levelCleared?: number;
  levelsRestored?: number;
  rootNodeCreated?: boolean;
}
export async function resetOrgStructureAll(body: { confirmText: string }): Promise<ResetAllResult> {
  const { data } = await request.post<ResetAllResult>(`${BASE}/reset-all`, body);
  return data ?? {};
}

// ----- Tree -----
export interface OrgTreeNode {
  id: number;
  name: string;
  levelId: number;
  parentId: number | null;
  isEnabled: boolean;
  remark?: string | null;
  children: OrgTreeNode[];
}

export interface OrgNodeItem {
  id: number;
  levelId: number;
  parentId: number | null;
  name: string;
  remark?: string | null;
  isEnabled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ImpactSummary {
  descendantNodeCount: number;
  personCountInSubtree: number;
}

export async function getOrgTree(includeDisabled = false): Promise<OrgTreeNode[]> {
  const { data } = await request.get<OrgTreeNode[]>(`${BASE}/nodes/tree`, { params: { includeDisabled } });
  return data ?? [];
}

export async function getNodeChildren(parentId: number | null, includeDisabled = false): Promise<OrgNodeItem[]> {
  const params: { includeDisabled: boolean; parentId?: number } = { includeDisabled };
  if (parentId != null) params.parentId = parentId;
  const { data } = await request.get<OrgNodeItem[]>(`${BASE}/nodes/children`, { params });
  return data ?? [];
}

export async function getNode(id: number): Promise<OrgNodeItem> {
  const { data } = await request.get<OrgNodeItem>(`${BASE}/nodes/${id}`);
  if (!data) throw new Error('No data');
  return data;
}

export async function getNodeImpact(id: number): Promise<ImpactSummary> {
  const { data } = await request.get<ImpactSummary>(`${BASE}/nodes/${id}/impact`);
  if (!data) throw new Error('No data');
  return data;
}

export async function createNode(body: { levelId: number; parentId?: number | null; name: string; remark?: string; isEnabled?: boolean }): Promise<OrgNodeItem> {
  const { data } = await request.post<OrgNodeItem>(`${BASE}/nodes`, body);
  if (!data) throw new Error('No data');
  return data;
}

export async function updateNode(id: number, body: { name?: string; remark?: string; isEnabled?: boolean }): Promise<OrgNodeItem> {
  const { data } = await request.put<OrgNodeItem>(`${BASE}/nodes/${id}`, body);
  if (!data) throw new Error('No data');
  return data;
}

export async function moveNode(id: number, newParentId: number | null): Promise<OrgNodeItem> {
  const { data } = await request.post<OrgNodeItem>(`${BASE}/nodes/${id}/move`, { newParentId });
  if (!data) throw new Error('No data');
  return data;
}

export async function disableNode(id: number): Promise<OrgNodeItem> {
  const { data } = await request.post<OrgNodeItem>(`${BASE}/nodes/${id}/disable`);
  if (!data) throw new Error('No data');
  return data;
}

export async function enableNode(id: number): Promise<OrgNodeItem> {
  const { data } = await request.post<OrgNodeItem>(`${BASE}/nodes/${id}/enable`);
  if (!data) throw new Error('No data');
  return data;
}

// ----- Job levels -----
export interface JobLevelItem {
  id: number;
  displayName: string;
  orderIndex: number;
}

export async function listJobLevels(): Promise<JobLevelItem[]> {
  const { data } = await request.get<JobLevelItem[]>(`${BASE}/job-levels`);
  return data ?? [];
}

// ----- People -----
export interface OrgPersonItem {
  id: number;
  name: string;
  phone: string;
  email?: string | null;
  remark?: string | null;
  jobLevelId: number;
  isActive: boolean;
  primaryOrgNodeId: number | null;
  secondaryOrgNodeIds: number[];
  jobLevelDisplayName?: string | null;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  updatedBy?: string;
}

export interface PersonOption {
  id: number;
  name: string;
  phone: string;
  email?: string | null;
}

export async function searchPeople(params: { keyword?: string; primaryOrgId?: number; page?: number; size?: number }): Promise<{ content: OrgPersonItem[]; totalElements: number; totalPages: number }> {
  const { data } = await request.get<{ content: OrgPersonItem[]; totalElements: number; totalPages: number }>(`${BASE}/people`, {
    params: { ...params, page: (params.page ?? 1) - 1, size: params.size ?? 20 },
  });
  if (!data) return { content: [], totalElements: 0, totalPages: 0 };
  return data;
}

/** People by org subtree for Org Tree right panel. Omit orgNodeId for root => all people. */
export async function searchPeopleByOrg(params: {
  orgNodeId?: number | null;
  includeDescendants?: boolean;
  includeSecondaries?: boolean;
  activeOnly?: boolean;
  keyword?: string;
  page?: number;
  size?: number;
}): Promise<{ content: OrgPersonItem[]; totalElements: number; totalPages: number; size: number; number: number }> {
  const { data } = await request.get<{ content: OrgPersonItem[]; totalElements: number; totalPages: number; size: number; number: number }>(
    `${BASE}/people/by-org`,
    {
      params: {
        orgNodeId: params.orgNodeId ?? undefined,
        includeDescendants: params.includeDescendants ?? true,
        includeSecondaries: params.includeSecondaries ?? true,
        activeOnly: params.activeOnly ?? true,
        keyword: params.keyword || undefined,
        page: (params.page ?? 1) - 1,
        size: params.size ?? 20,
      },
    }
  );
  if (!data) return { content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 };
  return data;
}

export async function getPersonOptions(keyword?: string): Promise<PersonOption[]> {
  const { data } = await request.get<PersonOption[]>(`${BASE}/people/options`, { params: { keyword } });
  return data ?? [];
}

export async function getPerson(id: number): Promise<OrgPersonItem> {
  const { data } = await request.get<OrgPersonItem>(`${BASE}/people/${id}`);
  if (!data) throw new Error('No data');
  return data;
}

export async function createPerson(body: {
  name: string;
  phone: string;
  email?: string;
  remark?: string;
  jobLevelId: number;
  primaryOrgNodeId: number;
  secondaryOrgNodeIds?: number[];
}): Promise<OrgPersonItem> {
  const { data } = await request.post<OrgPersonItem>(`${BASE}/people`, body);
  if (!data) throw new Error('No data');
  return data;
}

export async function updatePerson(id: number, body: Partial<{
  name: string;
  phone: string;
  email: string;
  remark: string;
  jobLevelId: number;
  primaryOrgNodeId: number;
  secondaryOrgNodeIds: number[];
}>): Promise<OrgPersonItem> {
  const { data } = await request.put<OrgPersonItem>(`${BASE}/people/${id}`, body);
  if (!data) throw new Error('No data');
  return data;
}

export async function disablePerson(id: number): Promise<OrgPersonItem> {
  const { data } = await request.post<OrgPersonItem>(`${BASE}/people/${id}/disable`);
  if (!data) throw new Error('No data');
  return data;
}

export async function enablePerson(id: number): Promise<OrgPersonItem> {
  const { data } = await request.post<OrgPersonItem>(`${BASE}/people/${id}/enable`);
  if (!data) throw new Error('No data');
  return data;
}

// ----- Position catalog & config -----
export interface PositionCatalogItem {
  id: number;
  baseName: string;
  grade: string;
  displayName: string;
  shortName: string | null;
  isEnabled: boolean;
}

export interface PositionBoundPerson {
  id: number;
  name: string;
  phone: string;
  email: string | null;
  primaryOrgNodeId: number | null;
  primaryOrgNamePath: string | null;
  isPartTime: boolean;
}

export interface EnabledPositionWithBindings {
  positionId: number;
  baseName: string;
  grade: string;
  displayName: string;
  shortName: string | null;
  boundPeople: PositionBoundPerson[];
}

export interface PositionsByOrgResponse {
  orgNodeId: number;
  orgNodeName: string;
  enabledPositions: EnabledPositionWithBindings[];
}

export interface PersonPositionAssignment {
  orgNodeId: number;
  orgNodeNamePath: string;
  positionId: number;
  positionDisplayName: string;
  positionShortName: string | null;
  isPartTime: boolean;
}

export async function getPositionCatalog(): Promise<PositionCatalogItem[]> {
  const { data } = await request.get<PositionCatalogItem[]>(`${BASE}/positions/catalog`);
  return data ?? [];
}

export async function getPositionsByOrg(orgNodeId: number): Promise<PositionsByOrgResponse> {
  const { data } = await request.get<PositionsByOrgResponse>(`${BASE}/positions/by-org`, { params: { orgNodeId } });
  if (!data) throw new Error('No data');
  return data;
}

export async function enablePosition(orgNodeId: number, positionId: number): Promise<void> {
  await request.post(`${BASE}/positions/by-org/enable`, { orgNodeId, positionId });
}

export async function disablePosition(orgNodeId: number, positionId: number): Promise<void> {
  await request.post(`${BASE}/positions/by-org/disable`, { orgNodeId, positionId });
}

export async function putPositionBindings(orgNodeId: number, positionId: number, personIds: number[]): Promise<void> {
  await request.put(`${BASE}/positions/by-org/bindings`, { orgNodeId, positionId, personIds });
}

export async function queryPositionBindings(orgNodeId: number, positionId: number): Promise<{ id: number; name: string; phone: string; email: string | null; isPartTime: boolean }[]> {
  const { data } = await request.get(`${BASE}/positions/bindings/query`, { params: { orgNodeId, positionId } });
  return data ?? [];
}

export async function getPersonPositions(personId: number): Promise<PersonPositionAssignment[]> {
  const { data } = await request.get<PersonPositionAssignment[]>(`${BASE}/people/${personId}/positions`);
  return data ?? [];
}

// ----- Change log -----
export interface ChangeLogItem {
  id: number;
  objectType: string;
  objectId: number;
  action: string;
  operatorId?: number | null;
  operatorName?: string | null;
  timestamp: string;
  summaryText?: string | null;
  diffJson?: string | null;
}

export async function listChangeLogs(params: {
  objectType?: string;
  objectId?: number;
  operatorId?: number;
  from?: string;
  to?: string;
  page?: number;
  size?: number;
}): Promise<{ content: ChangeLogItem[]; totalElements: number; totalPages: number }> {
  const { data } = await request.get<{ content: ChangeLogItem[]; totalElements: number; totalPages: number }>(`${BASE}/change-logs`, {
    params: { ...params, page: (params.page ?? 1) - 1, size: params.size ?? 20 },
  });
  if (!data) return { content: [], totalElements: 0, totalPages: 0 };
  return data;
}
