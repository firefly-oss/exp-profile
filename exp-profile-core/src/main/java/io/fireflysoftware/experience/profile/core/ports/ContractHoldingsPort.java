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


package io.fireflysoftware.experience.profile.core.ports;

import io.fireflysoftware.experience.profile.core.queries.ContractSummaryDTO;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Contracts the customer holds, with the product on each contract.
 * The adapter calls domain-common-contracts and domain-product-catalog. Not a core SDK.
 */
public interface ContractHoldingsPort {

    Flux<ContractSummaryDTO> list(UUID partyId);
}
