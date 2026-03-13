package com.firefly.experience.profile.web.openapi;

import org.fireflyframework.web.openapi.EnableOpenApiGen;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Lightweight application used during the Maven integration-test phase to start the service
 * on port 18080 so that springdoc-openapi-maven-plugin can fetch the OpenAPI spec.
 * <p>
 * This class lives in {@code src/test/java} and is only active during the
 * {@code generate-openapi} Maven profile triggered from the parent {@code firefly-parent}.
 */
@EnableOpenApiGen
@ComponentScan(basePackages = "com.firefly.experience.profile.web.controllers")
public class OpenApiGenApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenApiGenApplication.class, args);
    }
}
