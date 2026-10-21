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


package io.fireflysoftware.experience.profile.core.adapter;

import io.fireflysoftware.experience.profile.core.ports.ContractHoldingsPort;
import io.fireflysoftware.experience.profile.core.queries.ContractSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reads contracts, then the product named on each contract.
 */
@Component
public class HttpContractHoldingsAdapter implements ContractHoldingsPort {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {
    };

    private final WebClient contracts;
    private final WebClient products;

    @Autowired
    public HttpContractHoldingsAdapter(
            @Value("${api-configuration.domain-platform.common-contracts.base-path:http://localhost:8090}") String contractsBase,
            @Value("${api-configuration.domain-platform.product-catalog.base-path:http://localhost:8087}") String productsBase) {
        this(WebClient.builder().baseUrl(contractsBase).build(),
                WebClient.builder().baseUrl(productsBase).build());
    }

    HttpContractHoldingsAdapter(@Qualifier("holdingsContracts") WebClient contracts,
                                @Qualifier("holdingsProducts") WebClient products) {
        this.contracts = contracts;
        this.products = products;
    }

    @Override
    public Flux<ContractSummaryDTO> list(UUID partyId) {
        return contracts.get().uri("/api/v1/contracts/by-party/{partyId}", partyId)
                .retrieve()
                .bodyToMono(MAP)
                .flatMapMany(body -> {
                    Object content = body.get("content");
                    if (!(content instanceof List<?> rows)) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(rows);
                })
                .concatMap(row -> {
                    if (!(row instanceof Map<?, ?> raw)) {
                        return Mono.empty();
                    }
                    UUID contractId = uuid(raw.get("contractId"));
                    UUID productId = uuid(raw.get("productId"));
                    ContractSummaryDTO summary = ContractSummaryDTO.builder()
                            .contractId(contractId)
                            .type(text(raw.get("contractNumber")))
                            .status(text(raw.get("contractStatus")))
                            .productId(productId)
                            .build();
                    if (productId == null) {
                        return Mono.just(summary);
                    }
                    return products.get().uri("/api/v1/products/{productId}", productId)
                            .retrieve()
                            .bodyToMono(MAP)
                            .map(product -> {
                                summary.setProductName(text(product.get("productName")));
                                summary.setProductCode(text(product.get("productCode")));
                                return summary;
                            })
                            .defaultIfEmpty(summary);
                });
    }

    private static UUID uuid(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.toString());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }
}
