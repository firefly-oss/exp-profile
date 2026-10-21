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


package io.fireflysoftware.experience.profile.core.services.impl;

import io.fireflysoftware.experience.profile.core.commands.AddAddressCommand;
import io.fireflysoftware.experience.profile.core.commands.AddIdentityDocumentCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdateAddressCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdateConsentCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdateContactDataCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdatePersonalDataCommand;
import io.fireflysoftware.experience.profile.core.commands.UploadDocumentCommand;
import io.fireflysoftware.experience.profile.core.ports.AccessAction;
import io.fireflysoftware.experience.profile.core.ports.AccessGuard;
import io.fireflysoftware.experience.profile.core.ports.IdentityPort;
import io.fireflysoftware.experience.profile.core.ports.AccessPort;
import io.fireflysoftware.experience.profile.core.ports.AccessRequest;
import io.fireflysoftware.experience.profile.core.ports.ContractHoldingsPort;
import io.fireflysoftware.experience.profile.core.ports.PartyProfilePort;
import io.fireflysoftware.experience.profile.core.queries.AddressDTO;
import io.fireflysoftware.experience.profile.core.queries.ConsentDTO;
import io.fireflysoftware.experience.profile.core.queries.ContractSummaryDTO;
import io.fireflysoftware.experience.profile.core.queries.DocumentDTO;
import io.fireflysoftware.experience.profile.core.queries.IdentityDocumentDTO;
import io.fireflysoftware.experience.profile.core.queries.ProfileDTO;
import io.fireflysoftware.experience.profile.core.services.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Customer profile journey. Every read or update checks {@link AccessPort} before the
 * domain port. The domain adapter calls domain-customer-people and domain-common-contracts.
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final IdentityPort identityPort;
    private final AccessPort accessPort;
    private final PartyProfilePort partyProfile;
    private final ContractHoldingsPort holdings;

    @Override
    public Mono<ProfileDTO> getProfile(UUID partyId) {
        return guard(partyId, AccessAction.READ, () -> partyProfile.getProfile(partyId));
    }

    @Override
    public Mono<Void> updatePersonalData(UUID partyId, UpdatePersonalDataCommand command) {
        return guard(partyId, AccessAction.UPDATE, () -> partyProfile.updatePersonalData(partyId, command));
    }

    @Override
    public Mono<Void> updateContactData(UUID partyId, UpdateContactDataCommand command) {
        return guard(partyId, AccessAction.UPDATE, () -> partyProfile.updateContactData(partyId, command));
    }

    @Override
    public Flux<AddressDTO> getAddresses(UUID partyId) {
        return guardMany(partyId, AccessAction.READ, () -> partyProfile.getAddresses(partyId));
    }

    @Override
    public Mono<AddressDTO> addAddress(UUID partyId, AddAddressCommand command) {
        return guard(partyId, AccessAction.UPDATE, () -> partyProfile.addAddress(partyId, command));
    }

    @Override
    public Mono<AddressDTO> updateAddress(UUID partyId, UUID addressId, UpdateAddressCommand command) {
        return guard(partyId, AccessAction.UPDATE, () -> partyProfile.updateAddress(partyId, addressId, command));
    }

    @Override
    public Mono<Void> deleteAddress(UUID partyId, UUID addressId) {
        return guard(partyId, AccessAction.UPDATE, () -> partyProfile.deleteAddress(partyId, addressId));
    }

    @Override
    public Flux<DocumentDTO> getDocuments(UUID partyId) {
        return guardMany(partyId, AccessAction.READ, () -> partyProfile.getDocuments(partyId));
    }

    @Override
    public Mono<DocumentDTO> uploadDocument(UUID partyId, UploadDocumentCommand command) {
        return guard(partyId, AccessAction.UPDATE, () -> partyProfile.uploadDocument(partyId, command));
    }

    @Override
    public Mono<byte[]> downloadDocument(UUID partyId, UUID documentId) {
        return guard(partyId, AccessAction.READ, () -> partyProfile.downloadDocument(partyId, documentId));
    }

    @Override
    public Flux<ConsentDTO> getConsents(UUID partyId, String productCode) {
        return guardMany(partyId, AccessAction.READ, () -> partyProfile.getConsents(partyId, productCode));
    }

    @Override
    public Mono<Void> updateConsent(UUID partyId, UUID consentId, UpdateConsentCommand command) {
        return guard(partyId, AccessAction.UPDATE, () -> partyProfile.updateConsent(partyId, consentId, command));
    }

    @Override
    public Flux<IdentityDocumentDTO> getIdentityDocuments(UUID partyId) {
        return guardMany(partyId, AccessAction.READ, () -> partyProfile.getIdentityDocuments(partyId));
    }

    @Override
    public Mono<IdentityDocumentDTO> addIdentityDocument(UUID partyId, AddIdentityDocumentCommand command) {
        return guard(partyId, AccessAction.UPDATE, () -> partyProfile.addIdentityDocument(partyId, command));
    }

    @Override
    public Mono<Void> deleteIdentityDocument(UUID partyId, UUID documentId) {
        return guard(partyId, AccessAction.UPDATE, () -> partyProfile.deleteIdentityDocument(partyId, documentId));
    }

    @Override
    public Flux<ContractSummaryDTO> getContracts(UUID partyId) {
        return guardMany(partyId, AccessAction.READ, () -> holdings.list(partyId));
    }

    private <T> Mono<T> guard(UUID partyId, AccessAction action, java.util.function.Supplier<Mono<T>> call) {
        return AccessGuard.require(identityPort, accessPort, IdentityPort.SAMPLE_USERNAME,
                AccessRequest.of(partyId, action, "PARTY", partyId), call);
    }

    private <T> Flux<T> guardMany(UUID partyId, AccessAction action, java.util.function.Supplier<Flux<T>> call) {
        return AccessGuard.requireMany(identityPort, accessPort, IdentityPort.SAMPLE_USERNAME,
                AccessRequest.of(partyId, action, "PARTY", partyId), call);
    }
}
