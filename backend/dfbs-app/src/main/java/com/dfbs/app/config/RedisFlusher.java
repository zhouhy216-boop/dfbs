package com.dfbs.app.config;

/** Optional: flushes current Redis DB. Used by Test Data Cleaner after cleanup. */
public interface RedisFlusher {
    void flushDb();
}
