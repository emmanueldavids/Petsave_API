package com.petsave.petsave.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Configuration
@EnableAsync
@EnableCaching
@RequiredArgsConstructor
public class ProductionConfig {
    
    @Component
    public static class ApiHealthIndicator implements HealthIndicator {
        @Override
        public Health health() {
            return Health.up()
                    .withDetail("status", "API is running")
                    .withDetail("timestamp", System.currentTimeMillis())
                    .build();
        }
    }
}
