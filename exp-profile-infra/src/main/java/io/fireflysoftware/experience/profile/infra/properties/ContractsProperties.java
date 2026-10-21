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


package io.fireflysoftware.experience.profile.infra.properties;

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
