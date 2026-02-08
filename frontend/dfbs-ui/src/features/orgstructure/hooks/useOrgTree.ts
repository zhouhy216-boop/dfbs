import { useCallback, useEffect, useState } from 'react';
import { getOrgTree, type OrgTreeNode } from '@/features/orgstructure/services/orgStructure';

/**
 * Reusable hook: fetch org tree (optionally include disabled nodes).
 * For use in forms and selectors; requires Super Admin for write, read can be behind same gate in v1.
 */
export function useOrgTree(includeDisabled = false) {
  const [tree, setTree] = useState<OrgTreeNode[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<unknown>(null);

  const refetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getOrgTree(includeDisabled);
      setTree(data);
    } catch (e) {
      setError(e);
      setTree([]);
    } finally {
      setLoading(false);
    }
  }, [includeDisabled]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { tree, loading, error, refetch };
}
