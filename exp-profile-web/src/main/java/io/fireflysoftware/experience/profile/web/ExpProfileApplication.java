/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.profile.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Spring Boot application entry point for the Experience Profile service.
 * <p>
 * Provides REST APIs for customer profile management, aggregating data from
 * domain-tier services (customer-people, KYC/KYB, etc.) for frontend consumption.
 */
@SpringBootApplication(
        scanBasePackages = {
                "io.fireflysoftware.experience.profile",
                "org.fireflyframework.web",
                "org.fireflyframework.common.application"
        }
)
@EnableWebFlux
@ConfigurationPropertiesScan(basePackages = "io.fireflysoftware.experience.profile")
@OpenAPIDefinition(
        info = @Info(
                title = "${spring.application.name}",
                version = "${spring.application.version}",
                description = "Experience layer API for customer profile management",
                contact = @Contact(
                        name = "${spring.application.team.name}",
                        email = "${spring.application.team.email}"
                )
        ),
        servers = {
                @Server(
                        url = "https://openplatform.fireflysoftware.io/exp-profile",
                        description = "Development Environment"
                ),
                @Server(
                        url = "/",
                        description = "Local Development Environment"
                )
        }
)
public class ExpProfileApplication {

    /**
     * Application entry point. Bootstraps the Spring Boot context for the Experience Profile service.
     *
     * @param args command-line arguments passed to the Spring application
     */
    public static void main(String[] args) {
        SpringApplication.run(ExpProfileApplication.class, args);
    }
}
