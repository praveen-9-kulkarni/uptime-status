package com.project.uptime_status.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("!worker")
public class SecurityConfig {

    public SecurityConfig() {

    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
            auth -> auth.requestMatchers(HttpMethod.GET, "/status/**", "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/status/*/check").permitAll()
                        .requestMatchers("/targets/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .anyRequest()
                        .authenticated()
        ).csrf(csrf -> csrf.ignoringRequestMatchers("/targets/**")
        );
        return http.build();
    }

}
