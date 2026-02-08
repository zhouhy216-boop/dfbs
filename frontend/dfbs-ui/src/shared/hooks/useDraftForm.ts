import { useCallback, useEffect, useMemo, useState } from 'react';

const STORAGE_PREFIX = 'draft:';

/**
 * Reusable draft-save for forms. Keyed by formKey (recommended: user + route + form type).
 * Uses localStorage. Does not block normal submit; clear draft after successful submit or manually.
 */
export function useDraftForm<T = Record<string, unknown>>(formKey: string) {
  const storageKey = `${STORAGE_PREFIX}${formKey}`;

  const [hasDraft, setHasDraft] = useState(false);

  useEffect(() => {
    try {
      setHasDraft(!!localStorage.getItem(storageKey));
    } catch {
      setHasDraft(false);
    }
  }, [storageKey]);

  const saveDraft = useCallback(
    (values: T) => {
      try {
        localStorage.setItem(storageKey, JSON.stringify(values));
        setHasDraft(true);
      } catch {
        // ignore quota / disabled storage
      }
    },
    [storageKey]
  );

  const loadDraft = useCallback((): T | null => {
    try {
      const raw = localStorage.getItem(storageKey);
      if (!raw) return null;
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  }, [storageKey]);

  const clearDraft = useCallback(() => {
    try {
      localStorage.removeItem(storageKey);
      setHasDraft(false);
    } catch {
      // ignore
    }
  }, [storageKey]);

  return useMemo(
    () => ({ saveDraft, loadDraft, clearDraft, hasDraft }),
    [saveDraft, loadDraft, clearDraft, hasDraft]
  );
}
