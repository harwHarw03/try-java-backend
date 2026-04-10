package com.airscope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AirScope - IoT Environmental Data Backend
 *
 * Entry point of the Spring Boot application.
 * @SpringBootApplication enables:
 *   - @Configuration (bean definitions)
 *   - @EnableAutoConfiguration (Spring Boot magic)
 *   - @ComponentScan (finds all our classes)
 */
@SpringBootApplication
@EnableScheduling
public class AirScopeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirScopeApplication.class, args);
    }
}
