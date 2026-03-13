package com.firefly.experience.profile.infra.factories;

import com.firefly.core.contract.sdk.api.ContractsApi;
import com.firefly.experience.profile.infra.properties.ContractsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that wires SDK client beans for the contracts domain services.
 * <p>
 * Provides:
 * <ul>
 *   <li>{@link ContractsApi} — from {@code core-common-contract-mgmt-sdk},
 *       for reading contract summaries by party</li>
 * </ul>
 * Base path is configured via
 * {@code api-configuration.domain-platform.common-contracts.base-path}.
 *
 * <p>ARCH-EXCEPTION: Uses {@code core-common-contract-mgmt-sdk} (core tier) directly because
 * {@code domain-common-contracts-sdk} only exposes SCA challenge/verify operations via
 * {@code ScaOperationsApi}; no domain SDK exists for loan contract CRUD endpoints.
 */
@Component
@RequiredArgsConstructor
public class ContractsClientFactory {

    private final ContractsProperties properties;

    private com.firefly.core.contract.sdk.invoker.ApiClient buildCoreApiClient() {
        com.firefly.core.contract.sdk.invoker.ApiClient client =
                new com.firefly.core.contract.sdk.invoker.ApiClient();
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
        return new ContractsApi(buildCoreApiClient());
    }
}
