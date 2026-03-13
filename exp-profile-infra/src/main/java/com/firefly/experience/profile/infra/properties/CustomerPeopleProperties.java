package com.firefly.experience.profile.infra.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the domain-customer-people downstream service.
 * <p>
 * Bound automatically via {@code @ConfigurationPropertiesScan} on the application class.
 * Configure the base path in {@code application.yaml} under
 * {@code api-configuration.domain-platform.customer-people.base-path}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "api-configuration.domain-platform.customer-people")
public class CustomerPeopleProperties {

    /**
     * Base URL of the domain-customer-people service.
     * Example: {@code http://localhost:8081}
     */
    private String basePath;
}
