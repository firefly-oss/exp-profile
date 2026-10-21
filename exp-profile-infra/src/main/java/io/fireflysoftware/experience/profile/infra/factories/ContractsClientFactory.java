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


package io.fireflysoftware.experience.profile.infra.factories;

import io.fireflysoftware.domain.common.contracts.sdk.api.ContractsApi;
import io.fireflysoftware.domain.common.contracts.sdk.invoker.ApiClient;
import io.fireflysoftware.experience.profile.infra.properties.ContractsProperties;
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
