package com.dfbs.app.application.quote;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Mutable clock for tests. Use setInstant() to simulate date changes (e.g. next month).
 */
public class TestClock extends Clock {

    private final ZoneId zone;
    private volatile Instant instant;

    public TestClock(ZoneId zone, Instant instant) {
        this.zone = zone;
        this.instant = instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Override
    public Instant instant() {
        return instant;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new TestClock(zone, instant);
    }
}

