package com.firefly.experience.profile.infra.factories;

import com.firefly.domain.common.contracts.sdk.api.ContractsApi;
import com.firefly.domain.common.contracts.sdk.invoker.ApiClient;
import com.firefly.experience.profile.infra.properties.ContractsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that wires SDK client beans for the contracts domain services.
 * <p>
 * Provides:
 * <ul>
 *   <li>{@link ContractsApi} — from {@code domain-common-contracts-sdk},
 *       for reading contract summaries by party</li>
 * </ul>
 * Base path is configured via
 * {@code api-configuration.domain-platform.common-contracts.base-path}.
 */
@Component
@RequiredArgsConstructor
public class ContractsClientFactory {

    private final ContractsProperties properties;

    private ApiClient buildApiClient() {
        ApiClient client = new ApiClient();
        client.setBasePath(properties.getBasePath());
        return client;
    }

    /**
     * Provides the {@link ContractsApi} bean for fetching contract summaries
     * associated with a party.
     *
     * @return configured {@link ContractsApi} instance
     */
    @Bean
    public ContractsApi contractsApi() {
        return new ContractsApi(buildApiClient());
    }
}
