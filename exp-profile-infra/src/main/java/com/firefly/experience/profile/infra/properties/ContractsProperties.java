package com.firefly.experience.profile.infra.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the domain-common-contracts downstream service.
 * <p>
 * Bound automatically via {@code @ConfigurationPropertiesScan} on the application class.
 * Configure the base path in {@code application.yaml} under
 * {@code api-configuration.domain-platform.common-contracts.base-path}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "api-configuration.domain-platform.common-contracts")
public class ContractsProperties {

    /**
     * Base URL of the domain-common-contracts service.
     * Example: {@code http://localhost:8090}
     */
    private String basePath;
}
