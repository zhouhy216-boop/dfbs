package com.dfbs.app.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
public class TestDataCleanerRedisConfig {

    @Bean
    public RedisFlusher redisFlusher(RedisConnectionFactory connectionFactory) {
        return () -> {
            var conn = connectionFactory.getConnection();
            try {
                conn.flushDb();
            } finally {
                conn.close();
            }
        };
    }
}
