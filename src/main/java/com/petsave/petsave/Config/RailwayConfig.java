package com.petsave.petsave.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Profile("railway")
public class RailwayConfig {

    @Bean
    public HealthIndicator railwayHealthIndicator() {
        return () -> Health.up()
                .withDetail("status", "API is running on Railway")
                .withDetail("timestamp", System.currentTimeMillis())
                .withDetail("profile", "railway")
                .build();
    }
}
