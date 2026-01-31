/**
 * Adapt Spring Boot paginated response to ProTable format.
 * Spring: { content: T[], totalElements: number, number?: number, size?: number }
 * ProTable: { data: T[], total: number, success: boolean }
 */
export interface SpringPage<T> {
  content?: T[];
  totalElements?: number;
  number?: number;
  size?: number;
}

export function toProTableResult<T>(res: SpringPage<T> | null | undefined): {
  data: T[];
  total: number;
  success: true;
} {
  if (!res) {
    return { data: [], total: 0, success: true };
  }
  const content = Array.isArray(res.content) ? res.content : [];
  const total = typeof res.totalElements === 'number' ? res.totalElements : 0;
  return { data: content, total, success: true };
}
