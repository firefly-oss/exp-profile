package com.firefly.experience.profile.core.services.impl;

import com.firefly.domain.people.sdk.api.CustomersApi;
import com.firefly.domain.people.sdk.model.RegisterAddressCommand;
import com.firefly.domain.people.sdk.model.RegisterEmailCommand;
import com.firefly.domain.people.sdk.model.RegisterIdentityDocumentCommand;
import com.firefly.domain.people.sdk.model.RegisterPhoneCommand;
import com.firefly.domain.people.sdk.model.UpdateCustomerCommand;
import com.firefly.domain.common.contracts.sdk.api.ContractsApi;
import com.firefly.experience.profile.core.commands.AddAddressCommand;
import com.firefly.experience.profile.core.commands.AddIdentityDocumentCommand;
import com.firefly.experience.profile.core.commands.UpdateAddressCommand;
import com.firefly.experience.profile.core.commands.UpdateConsentCommand;
import com.firefly.experience.profile.core.commands.UpdateContactDataCommand;
import com.firefly.experience.profile.core.commands.UpdatePersonalDataCommand;
import com.firefly.experience.profile.core.commands.UploadDocumentCommand;
import com.firefly.experience.profile.core.queries.AddressDTO;
import com.firefly.experience.profile.core.queries.ConsentDTO;
import com.firefly.experience.profile.core.queries.ContractSummaryDTO;
import com.firefly.experience.profile.core.queries.DocumentDTO;
import com.firefly.experience.profile.core.queries.IdentityDocumentDTO;
import com.firefly.experience.profile.core.queries.ProfileDTO;
import com.firefly.experience.profile.core.services.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

/**
 * Stateless implementation of {@link ProfileService}.
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
 * </ul>
 * Capabilities backed by {@code domain-common-contracts-sdk}:
 * <ul>
 *   <li>Contract summaries via {@code ContractsApi.listByParty()}</li>
 * </ul>
 * <p>
 * Methods marked with {@code // TODO} require additional SDK query support
 * not yet available in the current domain service SDK.
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final CustomersApi customersApi;
    private final ContractsApi contractsApi;

    // ── Profile ────────────────────────────────────────────────────────────────

    @Override
    public Mono<ProfileDTO> getProfile(UUID partyId) {
        log.debug("Fetching profile for partyId={}", partyId);

        // TODO: Aggregate addresses and identity documents in parallel once
        //       domain-customer-people-sdk exposes query endpoints for those resources.
        return customersApi.getCustomerInfo(partyId, null)
                .map(person -> ProfileDTO.builder()
                        .partyId(partyId)
                        .firstName(person.getGivenName())
                        .lastName(person.getFamilyName1())
                        .dateOfBirth(person.getDateOfBirth())
                        .addresses(Collections.emptyList())
                        .identityDocuments(Collections.emptyList())
                        .build());
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
            sdkCommand.setFamilyName1(command.getLastName());
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
        // TODO: Implement once domain-customer-people-sdk exposes
        //       a query endpoint for listing addresses by partyId.
        return Flux.empty();
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

        com.firefly.domain.people.sdk.model.UpdateAddressCommand sdkCommand =
                new com.firefly.domain.people.sdk.model.UpdateAddressCommand();
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
        // TODO: Implement once an ECM (Electronic Content Management) SDK is available
        //       in the Firefly Banking Platform for document storage and retrieval.
        return Flux.empty();
    }

    @Override
    public Mono<DocumentDTO> uploadDocument(UUID partyId, UploadDocumentCommand command) {
        log.debug("Uploading document for partyId={}", partyId);
        // TODO: Implement once an ECM SDK is available for document storage.
        return Mono.just(DocumentDTO.builder()
                .documentId(UUID.randomUUID())
                .name(command.getName())
                .type(command.getType())
                .size((long) (command.getContent() != null ? command.getContent().length : 0))
                .build());
    }

    @Override
    public Mono<byte[]> downloadDocument(UUID partyId, UUID documentId) {
        log.debug("Downloading documentId={} for partyId={}", documentId, partyId);
        // TODO: Implement once an ECM SDK is available for document retrieval.
        return Mono.just(new byte[0]);
    }

    // ── Consents ───────────────────────────────────────────────────────────────

    @Override
    public Flux<ConsentDTO> getConsents(UUID partyId) {
        log.debug("Fetching consents for partyId={}", partyId);
        // TODO: Implement once domain-customer-people-sdk exposes
        //       a query endpoint for listing consents by partyId.
        return Flux.empty();
    }

    @Override
    public Mono<Void> updateConsent(UUID partyId, UUID consentId, UpdateConsentCommand command) {
        log.debug("Updating consentId={} for partyId={}", consentId, partyId);
        // TODO: Implement once domain-customer-people-sdk exposes
        //       an update endpoint for consent status by consentId.
        return Mono.empty();
    }

    // ── Identity Documents ─────────────────────────────────────────────────────

    @Override
    public Flux<IdentityDocumentDTO> getIdentityDocuments(UUID partyId) {
        log.debug("Fetching identity documents for partyId={}", partyId);
        // TODO: Implement once domain-customer-people-sdk exposes
        //       a query endpoint for listing identity documents by partyId.
        return Flux.empty();
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
                .thenReturn(IdentityDocumentDTO.builder()
                        .documentId(UUID.randomUUID())
                        .type(command.getType())
                        .number(command.getNumber())
                        .expiryDate(command.getExpiryDate())
                        .verified(false)
                        .build());
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
        // TODO: domain-common-contracts-sdk exposes listByParty(partyId, traceId)
        //       which returns Mono<Object>. Implement mapping from the raw response
        //       to ContractSummaryDTO once the response schema is finalised.
        return Flux.empty();
    }

    // ── Mapping Helpers ────────────────────────────────────────────────────────

    private RegisterAddressCommand.AddressKindEnum toAddressKind(String type) {
        try {
            return RegisterAddressCommand.AddressKindEnum.fromValue(type);
        } catch (IllegalArgumentException e) {
            return RegisterAddressCommand.AddressKindEnum.HOME;
        }
    }
}
