package com.project.uptime_status.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/** Worker exposes only health and Prometheus metrics; everything else is denied. */
@Configuration
@Profile("worker")
public class WorkerSecurityConfig {

    @Bean
    public SecurityFilterChain workerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/actuator/health/**", "/actuator/prometheus").permitAll()
                .anyRequest().denyAll());
        return http.build();
    }
}
