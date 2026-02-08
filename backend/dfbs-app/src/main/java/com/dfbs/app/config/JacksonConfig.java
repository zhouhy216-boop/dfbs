package com.dfbs.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson config: register Hibernate6Module so that accidental JPA entity / proxy
 * leakage in JSON does not cause 500 (ByteBuddyInterceptor). Prefer returning DTOs
 * only; this is a defense-in-depth safety net.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer hibernateModuleCustomizer() {
        return builder -> {
            Hibernate6Module module = new Hibernate6Module();
            // Do not force lazy loading during serialization (avoids N+1 and session issues)
            module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, false);
            builder.modulesToInstall(module);
        };
    }
}
