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
import io.fireflysoftware.domain.people.sdk.api.ConsentCatalogApi;
import io.fireflysoftware.domain.people.sdk.api.CustomersApi;
import io.fireflysoftware.domain.people.sdk.model.ConsentCatalogResponse;
import io.fireflysoftware.domain.people.sdk.model.RegisterAddressCommand;
import io.fireflysoftware.domain.people.sdk.model.RegisterEmailCommand;
import io.fireflysoftware.domain.people.sdk.model.RegisterIdentityDocumentCommand;
import io.fireflysoftware.domain.people.sdk.model.RegisterPhoneCommand;
import io.fireflysoftware.domain.people.sdk.model.UpdateCustomerCommand;
import io.fireflysoftware.domain.people.sdk.api.PartyContactQueriesApi;
import io.fireflysoftware.domain.people.sdk.model.PaginationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fireflysoftware.experience.profile.core.commands.AddAddressCommand;
import io.fireflysoftware.experience.profile.core.commands.AddIdentityDocumentCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdateAddressCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdateConsentCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdateContactDataCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdatePersonalDataCommand;
import io.fireflysoftware.experience.profile.core.commands.UploadDocumentCommand;
import io.fireflysoftware.experience.profile.core.queries.AddressDTO;
import io.fireflysoftware.experience.profile.core.queries.ConsentDTO;
import io.fireflysoftware.experience.profile.core.queries.ContractSummaryDTO;
import io.fireflysoftware.experience.profile.core.queries.DocumentDTO;
import io.fireflysoftware.experience.profile.core.queries.IdentityDocumentDTO;
import io.fireflysoftware.experience.profile.core.queries.ProfileDTO;
import io.fireflysoftware.experience.profile.core.ports.PartyProfilePort;
import io.fireflysoftware.experience.profile.core.util.IdempotencyKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.web.error.exceptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

/**
 * Domain adapter for {@link PartyProfilePort}. Every method calls domain-customer-people
 * or domain-common-contracts. The journey checks access before this adapter runs.
 * <p>
 * Composes downstream domain SDK calls to aggregate and mutate profile data.
 * No persistent state is maintained between requests.
 * <p>
 * Capabilities backed by {@code domain-customer-people-sdk}:
 * <ul>
 *   <li>Read personal data via {@code CustomersApi.getCustomerInfo()}</li>
 *   <li>Update personal data via {@code CustomersApi.updateCustomer()}</li>
 *   <li>Manage addresses via {@code CustomersApi.addCustomerAddress()},
 *       {@code updateCustomerAddress()}, {@code removeCustomerAddress()}</li>
 *   <li>Manage contact data via {@code CustomersApi.addCustomerEmail()},
 *       {@code addCustomerPhone()}</li>
 *   <li>Manage identity documents via {@code CustomersApi.addTaxId()},
 *       {@code removeTaxId()}</li>
 *   <li>Upsert consent records via {@code CustomersApi.updateCustomerConsent()}</li>
 * </ul>
 * Capabilities backed by {@code domain-common-contracts-sdk}:
 * <ul>
 *   <li>Contract summaries via {@code ContractsApi.listByParty()}</li>
 * </ul>
 * <p>
 * Query methods use {@code PartyContactQueriesApi} and {@code ContractsApi.listByParty}.
 */
@Slf4j
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "firefly.experience.profile.ports.party", havingValue = "domain", matchIfMissing = true)
@Component
@RequiredArgsConstructor
public class DomainPartyProfileAdapter implements PartyProfilePort {

    private final CustomersApi customersApi;
    private final ConsentCatalogApi consentCatalogApi;
    private final ContractsApi contractsApi;
    private final PartyContactQueriesApi partyContactQueriesApi;
    private final ObjectMapper objectMapper;

    // ── Profile ────────────────────────────────────────────────────────────────

    @Override
    public Mono<ProfileDTO> getProfile(UUID partyId) {
        log.debug("Fetching profile for partyId={}", partyId);

        return customersApi.getCustomerInfo(partyId, null)
                .flatMap(person -> Mono.zip(
                        getAddresses(partyId).collectList(),
                        getIdentityDocuments(partyId).collectList()
                ).map(lists -> ProfileDTO.builder()
                        .partyId(partyId)
                        .firstName(person.getGivenName())
                        .lastName(person.getFamilyName())
                        .dateOfBirth(person.getDateOfBirth())
                        .addresses(lists.getT1())
                        .identityDocuments(lists.getT2())
                        .build()));
    }

    // ── Personal & Contact Data ────────────────────────────────────────────────

    @Override
    public Mono<Void> updatePersonalData(UUID partyId, UpdatePersonalDataCommand command) {
        log.debug("Updating personal data for partyId={}", partyId);

        UpdateCustomerCommand sdkCommand = new UpdateCustomerCommand();
        sdkCommand.setPartyId(partyId);
        if (command.getFirstName() != null) {
            sdkCommand.setGivenName(command.getFirstName());
        }
        if (command.getLastName() != null) {
            sdkCommand.setFamilyName(command.getLastName());
        }
        if (command.getDateOfBirth() != null) {
            sdkCommand.setDateOfBirth(command.getDateOfBirth());
        }
        return customersApi.updateCustomer(sdkCommand, UUID.randomUUID().toString()).then();
    }

    @Override
    public Mono<Void> updateContactData(UUID partyId, UpdateContactDataCommand command) {
        log.debug("Updating contact data for partyId={}", partyId);

        Mono<Void> emailUpdate = Mono.empty();
        Mono<Void> phoneUpdate = Mono.empty();

        if (command.getEmail() != null) {
            RegisterEmailCommand emailCmd = new RegisterEmailCommand();
            emailCmd.setPartyId(partyId);
            emailCmd.setEmail(command.getEmail());
            emailCmd.setIsPrimary(true);
            emailUpdate = customersApi.addCustomerEmail(partyId, emailCmd, UUID.randomUUID().toString()).then();
        }

        if (command.getPhone() != null) {
            RegisterPhoneCommand phoneCmd = new RegisterPhoneCommand();
            phoneCmd.setPartyId(partyId);
            phoneCmd.setPhoneNumber(command.getPhone());
            phoneCmd.setIsPrimary(true);
            phoneUpdate = customersApi.addCustomerPhone(partyId, phoneCmd, UUID.randomUUID().toString()).then();
        }

        return Mono.when(emailUpdate, phoneUpdate);
    }

    // ── Addresses ─────────────────────────────────────────────────────────────

    @Override
    public Flux<AddressDTO> getAddresses(UUID partyId) {
        log.debug("Fetching addresses for partyId={}", partyId);
        return partyContactQueriesApi.getPartyAddresses(partyId, UUID.randomUUID().toString())
                .flatMapMany(this::content)
                .map(item -> {
                    var map = objectMapper.convertValue(item, java.util.Map.class);
                    return AddressDTO.builder()
                            .addressId(uuid(map.get("addressId")))
                            .type(text(map.get("addressKind")))
                            .street(text(map.get("line1")))
                            .city(text(map.get("city")))
                            .postalCode(text(map.get("postalCode")))
                            .country(text(map.get("country")))
                            .build();
                });
    }

    @Override
    public Mono<AddressDTO> addAddress(UUID partyId, AddAddressCommand command) {
        log.debug("Adding address for partyId={}", partyId);

        RegisterAddressCommand sdkCommand = new RegisterAddressCommand();
        sdkCommand.setPartyId(partyId);
        sdkCommand.setLine1(command.getStreet());
        sdkCommand.setCity(command.getCity());
        sdkCommand.setPostalCode(command.getPostalCode());
        sdkCommand.setAddressKind(toAddressKind(command.getType()));

        return customersApi.addCustomerAddress(partyId, sdkCommand, UUID.randomUUID().toString())
                .thenReturn(AddressDTO.builder()
                        .addressId(UUID.randomUUID())
                        .type(command.getType())
                        .street(command.getStreet())
                        .city(command.getCity())
                        .postalCode(command.getPostalCode())
                        .country(command.getCountry())
                        .build());
    }

    @Override
    public Mono<AddressDTO> updateAddress(UUID partyId, UUID addressId, UpdateAddressCommand command) {
        log.debug("Updating addressId={} for partyId={}", addressId, partyId);

        io.fireflysoftware.domain.people.sdk.model.UpdateAddressCommand sdkCommand =
                new io.fireflysoftware.domain.people.sdk.model.UpdateAddressCommand();
        sdkCommand.setAddressId(addressId);
        sdkCommand.setPartyId(partyId);
        if (command.getStreet() != null) {
            sdkCommand.setLine1(command.getStreet());
        }
        if (command.getCity() != null) {
            sdkCommand.setCity(command.getCity());
        }
        if (command.getPostalCode() != null) {
            sdkCommand.setPostalCode(command.getPostalCode());
        }

        return customersApi.updateCustomerAddress(partyId, addressId, sdkCommand, UUID.randomUUID().toString())
                .thenReturn(AddressDTO.builder()
                        .addressId(addressId)
                        .street(command.getStreet())
                        .city(command.getCity())
                        .postalCode(command.getPostalCode())
                        .country(command.getCountry())
                        .build());
    }

    @Override
    public Mono<Void> deleteAddress(UUID partyId, UUID addressId) {
        log.debug("Deleting addressId={} for partyId={}", addressId, partyId);
        return customersApi.removeCustomerAddress(partyId, addressId, UUID.randomUUID().toString()).then();
    }

    // ── Documents ─────────────────────────────────────────────────────────────

    @Override
    public Flux<DocumentDTO> getDocuments(UUID partyId) {
        log.debug("Fetching documents for partyId={}", partyId);
        return partyContactQueriesApi.getPartyIdentityDocuments(partyId, UUID.randomUUID().toString())
                .flatMapMany(this::content)
                .map(item -> {
                    var map = objectMapper.convertValue(item, java.util.Map.class);
                    return DocumentDTO.builder()
                            .documentId(uuid(map.get("identityDocumentId"), map.get("documentId")))
                            .name(text(map.get("documentNumber")))
                            .type(text(map.get("documentType")))
                            .build();
                });
    }

    @Override
    public Mono<DocumentDTO> uploadDocument(UUID partyId, UploadDocumentCommand command) {
        log.debug("Uploading document for partyId={}", partyId);
        RegisterIdentityDocumentCommand sdkCommand = new RegisterIdentityDocumentCommand();
        sdkCommand.setPartyId(partyId);
        sdkCommand.setDocumentNumber(command.getName());
        return customersApi.addTaxId(partyId, sdkCommand, UUID.randomUUID().toString())
                .map(response -> {
                    var map = objectMapper.convertValue(response, java.util.Map.class);
                    return DocumentDTO.builder()
                            .documentId(uuid(map.get("identityDocumentId"), map.get("documentId")))
                            .name(command.getName())
                            .type(command.getType())
                            .size(command.getContent() == null ? 0L : (long) command.getContent().length)
                            .build();
                });
    }

    @Override
    public Mono<byte[]> downloadDocument(UUID partyId, UUID documentId) {
        log.debug("Downloading documentId={} for partyId={}", documentId, partyId);
        return partyContactQueriesApi.getPartyIdentityDocuments(partyId, UUID.randomUUID().toString())
                .flatMapMany(this::content)
                .filter(item -> documentId.equals(uuid(objectMapper.convertValue(item, java.util.Map.class).get("identityDocumentId"),
                        objectMapper.convertValue(item, java.util.Map.class).get("documentId"))))
                .next()
                .map(item -> objectMapper.convertValue(item, java.util.Map.class))
                .map(map -> text(map.get("documentNumber")).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // ── Consents ───────────────────────────────────────────────────────────────

    @Override
    public Flux<ConsentDTO> getConsents(UUID partyId, String productCode) {
        log.debug("Fetching consents for partyId={} productCode={}", partyId, productCode);
        // The catalogue endpoint returns every active consent template the
        // channel may need to render. Per-party state (status, updatedAt) is
        // not yet queryable from the domain SDK, so we surface every entry as
        // REJECTED — that's the expected default until the user records a
        // choice via PUT /consents/{id}.
        return consentCatalogApi.getConsentCatalog(productCode, UUID.randomUUID().toString())
                .map(DomainPartyProfileAdapter::toConsentDTO);
    }

    private static ConsentDTO toConsentDTO(ConsentCatalogResponse dto) {
        return ConsentDTO.builder()
                .consentId(dto.getConsentId())
                .type(dto.getConsentType())
                // PENDING distinguishes "user hasn't decided yet" from REJECTED
                // (active opt-out). Once per-party state is queryable from the
                // domain SDK this will be replaced with ACCEPTED/REJECTED based
                // on the recorded choice.
                .status("PENDING")
                .updatedAt(null)
                .required(Boolean.TRUE.equals(dto.getRequired()))
                .label(dto.getDescription())
                .order(dto.getOrder())
                .build();
    }

    @Override
    public Mono<Void> updateConsent(UUID partyId, UUID consentId, UpdateConsentCommand command) {
        return Mono.defer(() -> {
            log.info("Updating consentId={} for partyId={} applicationId={}",
                    consentId, partyId, command.getApplicationId());

            boolean granted = mapStatusToGranted(command.getStatus());

            // Build the domain-level UpdateConsentCommand (PUT /api/v1/customers/{partyId}/consents/{consentId})
            // so the optional applicationId soft link is propagated to the customer-people
            // domain. The domain service is the only owner of the persisted core consent
            // record — the experience tier never speaks to core directly.
            io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand sdkPayload =
                    new io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand();
            sdkPayload.setPartyId(partyId);
            sdkPayload.setConsentId(consentId);
            sdkPayload.setGranted(granted);
            if (command.getApplicationId() != null) {
                sdkPayload.setApplicationId(command.getApplicationId());
            }

            // PUT semantics: the same (partyId, consentId) pair represents the
            // same consent record, so deriving the key deterministically from
            // those two ids guarantees that retries of the same upsert collapse
            // to the same downstream idempotency entry rather than producing
            // duplicate consent rows.
            String idempotencyKey = IdempotencyKeys.of(
                    "exp-profile", "update-consent",
                    partyId.toString(), consentId.toString());

            return dispatchConsentUpsert(partyId, consentId, sdkPayload, idempotencyKey);
        });
    }

    /**
     * Dispatches the upsert of a consent record to the customer-people domain via
     * {@link CustomersApi#updateCustomerConsent(UUID, UUID, io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand, String)}.
     * <p>
     * Extracted as a separate method to allow tests to capture the prepared
     * {@link io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand} via
     * {@code ArgumentCaptor} without replaying the full reactive SDK chain.
     *
     * @param partyId         the owning party UUID
     * @param consentId       the consent record UUID being upserted
     * @param sdkPayload      the prepared SDK payload (already populated with applicationId)
     * @param idempotencyKey  unique key for idempotent retries
     * @return reactive completion signal
     */
    public Mono<Void> dispatchConsentUpsert(UUID partyId,
                                            UUID consentId,
                                            io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand sdkPayload,
                                            String idempotencyKey) {
        return customersApi.updateCustomerConsent(partyId, consentId, sdkPayload, idempotencyKey).then();
    }

    /**
     * Maps the inbound consent status string to the boolean {@code granted} flag
     * expected by the downstream SDK payload.
     *
     * @param status the inbound status — must be one of GRANTED, ACCEPTED, REVOKED, REJECTED
     *               (case-insensitive)
     * @return {@code true} for GRANTED/ACCEPTED, {@code false} for REVOKED/REJECTED
     * @throws BusinessException when {@code status} is not one of the accepted values
     */
    private boolean mapStatusToGranted(String status) {
        if (status == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_CONSENT_STATUS",
                    "status is required. Allowed values: GRANTED, ACCEPTED, REVOKED, REJECTED.");
        }
        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "GRANTED", "ACCEPTED" -> true;
            case "REVOKED", "REJECTED" -> false;
            default -> throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_CONSENT_STATUS",
                    "status '" + status + "' is not supported. "
                            + "Allowed values: GRANTED, ACCEPTED, REVOKED, REJECTED.");
        };
    }

    // ── Identity Documents ─────────────────────────────────────────────────────

    @Override
    public Flux<IdentityDocumentDTO> getIdentityDocuments(UUID partyId) {
        log.debug("Fetching identity documents for partyId={}", partyId);
        return partyContactQueriesApi.getPartyIdentityDocuments(partyId, UUID.randomUUID().toString())
                .flatMapMany(this::content)
                .map(item -> {
                    var map = objectMapper.convertValue(item, java.util.Map.class);
                    return IdentityDocumentDTO.builder()
                            .documentId(uuid(map.get("identityDocumentId"), map.get("documentId")))
                            .type(text(map.get("documentType")))
                            .number(text(map.get("documentNumber")))
                            .verified(Boolean.TRUE.equals(map.get("verified")))
                            .build();
                });
    }

    @Override
    public Mono<IdentityDocumentDTO> addIdentityDocument(UUID partyId, AddIdentityDocumentCommand command) {
        log.debug("Adding identity document for partyId={}", partyId);

        RegisterIdentityDocumentCommand sdkCommand = new RegisterIdentityDocumentCommand();
        sdkCommand.setPartyId(partyId);
        sdkCommand.setDocumentNumber(command.getNumber());
        if (command.getExpiryDate() != null) {
            sdkCommand.setExpiryDate(command.getExpiryDate().atStartOfDay());
        }

        return customersApi.addTaxId(partyId, sdkCommand, UUID.randomUUID().toString())
                .map(response -> {
                    var map = objectMapper.convertValue(response, java.util.Map.class);
                    UUID documentId = uuid(map.get("identityDocumentId"), map.get("documentId"));
                    return IdentityDocumentDTO.builder()
                            .documentId(documentId)
                            .type(command.getType())
                            .number(command.getNumber())
                            .expiryDate(command.getExpiryDate())
                            .verified(false)
                            .build();
                });
    }

    @Override
    public Mono<Void> deleteIdentityDocument(UUID partyId, UUID documentId) {
        log.debug("Deleting identity documentId={} for partyId={}", documentId, partyId);
        return customersApi.removeTaxId(partyId, documentId, UUID.randomUUID().toString()).then();
    }

    // ── Contracts ─────────────────────────────────────────────────────────────

    @Override
    public Flux<ContractSummaryDTO> getContracts(UUID partyId) {
        log.debug("Fetching contracts for partyId={}", partyId);
        return contractsApi.listByParty(partyId, UUID.randomUUID().toString())
                .flatMapMany(body -> {
                    var map = objectMapper.convertValue(body, java.util.Map.class);
                    Object content = map.get("content");
                    if (!(content instanceof java.util.List<?> list)) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(list);
                })
                .map(item -> {
                    var map = objectMapper.convertValue(item, java.util.Map.class);
                    return ContractSummaryDTO.builder()
                            .contractId(uuid(map.get("contractId")))
                            .type(text(map.get("contractNumber")))
                            .status(text(map.get("contractStatus")))
                            .build();
                });
    }

    // ── Mapping Helpers ────────────────────────────────────────────────────────

    private RegisterAddressCommand.AddressKindEnum toAddressKind(String type) {
        try {
            return RegisterAddressCommand.AddressKindEnum.fromValue(type);
        } catch (IllegalArgumentException e) {
            return RegisterAddressCommand.AddressKindEnum.HOME;
        }
    }

    private Flux<Object> content(PaginationResponse page) {
        if (page == null || page.getContent() == null) {
            return Flux.empty();
        }
        return Flux.fromIterable(page.getContent());
    }

    private static UUID uuid(Object... values) {
        for (Object value : values) {
            if (value instanceof UUID id) {
                return id;
            }
            if (value instanceof String text && !text.isBlank()) {
                try {
                    return UUID.fromString(text);
                } catch (IllegalArgumentException ignored) {
                    return UUID.nameUUIDFromBytes(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            }
        }
        return null;
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }
}
