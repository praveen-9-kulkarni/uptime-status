package com.project.uptime_status.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DateTimeConfig {

    @Bean
    public Clock clock() {

        return Clock.systemUTC();
    }
}
