package com.dfbs.app.modules.quote;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class QuoteNumberingTestConfig {

    @Bean
    @Primary
    public TestClock testClock() {
        return new TestClock(ZoneId.of("UTC"), Instant.parse("2026-01-15T10:00:00Z"));
    }
}
