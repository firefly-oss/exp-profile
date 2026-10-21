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


package io.fireflysoftware.experience.profile.core.services;

import io.fireflysoftware.experience.profile.core.ports.AccessDecision;
import io.fireflysoftware.experience.profile.core.ports.AccessDeniedException;
import io.fireflysoftware.experience.profile.core.ports.AccessPort;
import io.fireflysoftware.experience.profile.core.ports.ContractHoldingsPort;
import io.fireflysoftware.experience.profile.core.ports.IdentityPort;
import io.fireflysoftware.experience.profile.core.ports.PartyProfilePort;
import io.fireflysoftware.experience.profile.core.ports.SubjectRef;
import io.fireflysoftware.experience.profile.core.queries.ContractSummaryDTO;
import io.fireflysoftware.experience.profile.core.services.impl.ProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileHoldingsTest {

    @Mock private IdentityPort identityPort;
    @Mock private AccessPort accessPort;
    @Mock private PartyProfilePort partyProfile;
    @Mock private ContractHoldingsPort holdings;

    @Test
    void deniedActorDoesNotReadContractsOrProducts() {
        when(identityPort.resolve(any())).thenReturn(Mono.just(new SubjectRef(IdentityPort.DENIED_ACTOR, "sandbox")));
        when(accessPort.check(any())).thenReturn(Mono.just(AccessDecision.denied("ACTOR_NOT_ENTITLED")));
        ProfileServiceImpl service = new ProfileServiceImpl(identityPort, accessPort, partyProfile, holdings);

        StepVerifier.create(service.getContracts(UUID.randomUUID()))
                .expectError(AccessDeniedException.class)
                .verify();

        verify(holdings, never()).list(any());
    }

    @Test
    void allowedReadReturnsContractAndProduct() {
        UUID partyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(identityPort.resolve(any())).thenReturn(Mono.just(new SubjectRef(IdentityPort.SAMPLE_ACTOR, "sandbox")));
        when(accessPort.check(any())).thenReturn(Mono.just(AccessDecision.permit("SANDBOX_GRANT")));
        when(holdings.list(partyId)).thenReturn(Flux.just(ContractSummaryDTO.builder()
                .contractId(UUID.randomUUID())
                .productId(productId)
                .productName("Motor cover")
                .productCode("MOTOR")
                .build()));
        ProfileServiceImpl service = new ProfileServiceImpl(identityPort, accessPort, partyProfile, holdings);

        StepVerifier.create(service.getContracts(partyId))
                .expectNextMatches(row -> productId.equals(row.getProductId()) && "Motor cover".equals(row.getProductName()))
                .verifyComplete();
    }
}
