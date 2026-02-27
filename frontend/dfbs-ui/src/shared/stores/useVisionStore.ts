import { create } from 'zustand';

export interface VisionState {
  mode: 'OFF' | 'USER';
  userId?: number;
}

interface VisionStoreState {
  vision: VisionState | null;
  version: number;
  setVision: (v: VisionState | null) => void;
  clearVision: () => void;
}

export const useVisionStore = create<VisionStoreState>((set) => ({
  vision: null,
  version: 0,
  setVision: (v) => set((s) => ({ vision: v, version: s.version + 1 })),
  clearVision: () => set((s) => ({ vision: null, version: s.version + 1 })),
}));
