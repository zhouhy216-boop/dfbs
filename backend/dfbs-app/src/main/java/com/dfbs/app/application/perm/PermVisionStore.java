package com.dfbs.app.application.perm;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ephemeral vision state for test-only Role-Vision. Keyed by admin userId; TTL 30 min.
 * No DB writes. Thread-safe.
 */
@Component
public class PermVisionStore {

    private static final long TTL_MS = 30 * 60 * 1000L;

    public enum Mode {
        OFF,
        USER
    }

    public static final class VisionEntry {
        private final Mode mode;
        private final Long targetUserId;
        private final long expiresAt;

        public VisionEntry(Mode mode, Long targetUserId, long expiresAt) {
            this.mode = mode;
            this.targetUserId = targetUserId;
            this.expiresAt = expiresAt;
        }

        public Mode getMode() { return mode; }
        public Long getTargetUserId() { return targetUserId; }
        public boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    }

    private final Map<Long, VisionEntry> byAdmin = new ConcurrentHashMap<>();

    public void setVision(Long adminUserId, Mode mode, Long targetUserId) {
        if (adminUserId == null) return;
        if (mode == Mode.OFF) {
            byAdmin.remove(adminUserId);
            return;
        }
        long expiresAt = System.currentTimeMillis() + TTL_MS;
        byAdmin.put(adminUserId, new VisionEntry(mode, targetUserId, expiresAt));
    }

    public VisionEntry getVision(Long adminUserId) {
        if (adminUserId == null) return null;
        VisionEntry e = byAdmin.get(adminUserId);
        if (e == null || e.isExpired()) {
            if (e != null) byAdmin.remove(adminUserId);
            return null;
        }
        return e;
    }
}
