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

import io.fireflysoftware.domain.common.contracts.sdk.api.ContractsApi;
import io.fireflysoftware.experience.profile.core.ports.ContractHoldingsPort;
import io.fireflysoftware.experience.profile.core.queries.ContractSummaryDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reads holdings from the contracts domain SDK. Product name and code are filled
 * by that domain from the product core. This journey does not call the product core.
 */
@Component
public class HttpContractHoldingsAdapter implements ContractHoldingsPort {

    private final ContractsApi contracts;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public HttpContractHoldingsAdapter(ContractsApi contracts) {
        this.contracts = contracts;
    }

    @Override
    public Flux<ContractSummaryDTO> list(UUID partyId) {
        return contracts.listByParty(partyId, null).flatMapMany(this::rows);
    }

    private Flux<ContractSummaryDTO> rows(Object body) {
        if (body == null) {
            return Flux.empty();
        }
        Object content = body;
        if (body instanceof Map<?, ?> map && map.get("content") instanceof Collection<?>) {
            content = map.get("content");
        }
        if (!(content instanceof Collection<?> rows)) {
            return Flux.empty();
        }
        List<ContractSummaryDTO> summaries = rows.stream()
                .filter(Map.class::isInstance)
                .map(row -> summary((Map<?, ?>) row))
                .toList();
        return Flux.fromIterable(summaries);
    }

    private ContractSummaryDTO summary(Map<?, ?> raw) {
        Map<String, Object> row = mapper.convertValue(raw, mapper.getTypeFactory()
                .constructMapType(Map.class, String.class, Object.class));
        return ContractSummaryDTO.builder()
                .contractId(uuid(row.get("contractId")))
                .type(text(row.get("contractNumber")))
                .status(text(row.get("contractStatus")))
                .signedAt(signedAt(row.get("signedAt")))
                .productId(uuid(row.get("productId")))
                .productName(text(row.get("productName")))
                .productCode(text(row.get("productCode")))
                .build();
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

    private static LocalDateTime signedAt(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        if (value instanceof LocalDateTime date) {
            return date;
        }
        return LocalDateTime.parse(value.toString());
    }
}
