package com.project.uptime_status;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UptimeStatusApplication {

	public static void main(String[] args) {
		SpringApplication.run(UptimeStatusApplication.class, args);
	}

}
