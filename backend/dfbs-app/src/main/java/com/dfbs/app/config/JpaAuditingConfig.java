package com.dfbs.app.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.util.Optional;

@Configuration
@EnableJpaAuditing
@EnableConfigurationProperties({CompanyInfoProperties.class, PermAllowlistProperties.class, AuthProperties.class})
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("system");
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
