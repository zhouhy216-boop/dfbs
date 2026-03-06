import { create } from 'zustand';

/** UI-only role simulator: localStorage key. Not used for authorization. */
export const SIMULATED_ROLE_STORAGE_KEY = 'dfbs_ui_simulated_role';

function getStoredSimulatedRole(): string | null {
  try {
    const v = localStorage.getItem(SIMULATED_ROLE_STORAGE_KEY);
    return v && v !== '__none__' ? v : null;
  } catch {
    return null;
  }
}

interface SimulatedRoleState {
  simulatedRole: string | null;
  setSimulatedRole: (role: string | null) => void;
}

export const useSimulatedRoleStore = create<SimulatedRoleState>((set) => ({
  simulatedRole: getStoredSimulatedRole(),
  setSimulatedRole: (role) => {
    try {
      if (role == null) {
        localStorage.removeItem(SIMULATED_ROLE_STORAGE_KEY);
      } else {
        localStorage.setItem(SIMULATED_ROLE_STORAGE_KEY, role);
      }
    } catch (_) {}
    set({ simulatedRole: role });
  },
}));
