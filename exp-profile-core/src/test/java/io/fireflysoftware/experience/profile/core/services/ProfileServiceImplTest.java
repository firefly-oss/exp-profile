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

import io.fireflysoftware.domain.common.contracts.sdk.api.ContractsApi;
import io.fireflysoftware.domain.people.sdk.api.ConsentCatalogApi;
import io.fireflysoftware.domain.people.sdk.api.CustomersApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fireflysoftware.domain.people.sdk.api.PartyContactQueriesApi;
import io.fireflysoftware.domain.people.sdk.model.NaturalPersonDTO;
import io.fireflysoftware.domain.people.sdk.model.PaginationResponse;
import io.fireflysoftware.experience.profile.core.commands.AddAddressCommand;
import io.fireflysoftware.experience.profile.core.commands.AddIdentityDocumentCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdateAddressCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdateConsentCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdateContactDataCommand;
import io.fireflysoftware.experience.profile.core.commands.UpdatePersonalDataCommand;
import io.fireflysoftware.experience.profile.core.queries.AddressDTO;
import io.fireflysoftware.experience.profile.core.queries.IdentityDocumentDTO;
import io.fireflysoftware.experience.profile.core.queries.ProfileDTO;
import io.fireflysoftware.experience.profile.core.adapter.DomainPartyProfileAdapter;
import io.fireflysoftware.experience.profile.core.ports.PartyProfilePort;
import org.fireflyframework.web.error.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private CustomersApi customersApi;

    @Mock
    private ConsentCatalogApi consentCatalogApi;

    @Mock
    private ContractsApi contractsApi;
    @Mock
    private PartyContactQueriesApi partyContactQueriesApi;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PartyProfilePort service;

    @BeforeEach
    void setUp() {
        service = new DomainPartyProfileAdapter(
                customersApi, consentCatalogApi, contractsApi, partyContactQueriesApi, objectMapper);
        lenient().when(partyContactQueriesApi.getPartyAddresses(any(), any()))
                .thenReturn(Mono.just(new PaginationResponse()));
        lenient().when(partyContactQueriesApi.getPartyIdentityDocuments(any(), any()))
                .thenReturn(Mono.just(new PaginationResponse()));
    }

    // ── getProfile ─────────────────────────────────────────────────────────────

    @Test
    void getProfile_mapsPersonalDataFromDownstreamDto() {
        UUID partyId = UUID.randomUUID();
        NaturalPersonDTO sdkDto = new NaturalPersonDTO();
        sdkDto.setGivenName("Jane");
        sdkDto.setFamilyName("Doe");
        sdkDto.setDateOfBirth(LocalDate.of(1990, 5, 15));

        when(customersApi.getCustomerInfo(eq(partyId), any())).thenReturn(Mono.just(sdkDto));

        StepVerifier.create(service.getProfile(partyId))
                .expectNextMatches(profile ->
                        partyId.equals(profile.getPartyId())
                        && "Jane".equals(profile.getFirstName())
                        && "Doe".equals(profile.getLastName())
                        && LocalDate.of(1990, 5, 15).equals(profile.getDateOfBirth())
                        && profile.getAddresses().isEmpty()
                        && profile.getIdentityDocuments().isEmpty()
                )
                .verifyComplete();
    }

    @Test
    void getProfile_propagatesDownstreamError() {
        UUID partyId = UUID.randomUUID();
        when(customersApi.getCustomerInfo(eq(partyId), any()))
                .thenReturn(Mono.error(new RuntimeException("downstream unavailable")));

        StepVerifier.create(service.getProfile(partyId))
                .expectError(RuntimeException.class)
                .verify();
    }

    // ── updatePersonalData ─────────────────────────────────────────────────────

    @Test
    void updatePersonalData_delegatesToCustomersApi() {
        UUID partyId = UUID.randomUUID();
        UpdatePersonalDataCommand command = new UpdatePersonalDataCommand();
        command.setFirstName("Alice");
        command.setLastName("Smith");

        when(customersApi.updateCustomer(any(), any())).thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updatePersonalData(partyId, command))
                .verifyComplete();

        verify(customersApi).updateCustomer(any(), any());
    }

    @Test
    void updatePersonalData_skipsNullFields() {
        UUID partyId = UUID.randomUUID();
        UpdatePersonalDataCommand command = new UpdatePersonalDataCommand();
        command.setFirstName("Alice");
        // lastName and dateOfBirth intentionally null — partial update

        when(customersApi.updateCustomer(any(), any())).thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updatePersonalData(partyId, command))
                .verifyComplete();
    }

    // ── updateContactData ──────────────────────────────────────────────────────

    @Test
    void updateContactData_updatesEmailAndPhone() {
        UUID partyId = UUID.randomUUID();
        UpdateContactDataCommand command = new UpdateContactDataCommand();
        command.setEmail("alice@example.com");
        command.setPhone("+34600123456");

        when(customersApi.addCustomerEmail(eq(partyId), any(), any())).thenReturn(Mono.just(new Object()));
        when(customersApi.addCustomerPhone(eq(partyId), any(), any())).thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateContactData(partyId, command))
                .verifyComplete();

        verify(customersApi).addCustomerEmail(eq(partyId), any(), any());
        verify(customersApi).addCustomerPhone(eq(partyId), any(), any());
    }

    @Test
    void updateContactData_skipsEmailWhenNull() {
        UUID partyId = UUID.randomUUID();
        UpdateContactDataCommand command = new UpdateContactDataCommand();
        command.setPhone("+34600999888");

        when(customersApi.addCustomerPhone(eq(partyId), any(), any())).thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateContactData(partyId, command))
                .verifyComplete();
    }

    // ── addAddress ─────────────────────────────────────────────────────────────

    @Test
    void addAddress_returnsBuiltDtoWithInputData() {
        UUID partyId = UUID.randomUUID();
        AddAddressCommand command = new AddAddressCommand();
        command.setType("HOME");
        command.setStreet("Calle Gran Via 1");
        command.setCity("Madrid");
        command.setPostalCode("28013");
        command.setCountry("ES");

        UUID addressId = UUID.randomUUID();
        when(customersApi.addCustomerAddress(eq(partyId), any(), any()))
                .thenReturn(Mono.just(java.util.Map.of("addressId", addressId.toString())));

        StepVerifier.create(service.addAddress(partyId, command))
                .expectNextMatches(dto ->
                        addressId.equals(dto.getAddressId())
                        && "HOME".equals(dto.getType())
                        && "Calle Gran Via 1".equals(dto.getStreet())
                        && "Madrid".equals(dto.getCity())
                        && "28013".equals(dto.getPostalCode())
                        && "ES".equals(dto.getCountry())
                )
                .verifyComplete();
    }

    // ── updateAddress ──────────────────────────────────────────────────────────

    @Test
    void updateAddress_returnsUpdatedDto() {
        UUID partyId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UpdateAddressCommand command = new UpdateAddressCommand();
        command.setCity("Barcelona");
        command.setPostalCode("08001");

        when(customersApi.updateCustomerAddress(eq(partyId), eq(addressId), any(), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateAddress(partyId, addressId, command))
                .expectNextMatches(dto ->
                        addressId.equals(dto.getAddressId())
                        && "Barcelona".equals(dto.getCity())
                        && "08001".equals(dto.getPostalCode())
                )
                .verifyComplete();
    }

    // ── deleteAddress ──────────────────────────────────────────────────────────

    @Test
    void deleteAddress_delegatesToCustomersApi() {
        UUID partyId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        when(customersApi.removeCustomerAddress(eq(partyId), eq(addressId), any())).thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.deleteAddress(partyId, addressId))
                .verifyComplete();

        verify(customersApi).removeCustomerAddress(eq(partyId), eq(addressId), any());
    }

    // ── getAddresses (stub) ────────────────────────────────────────────────────

    @Test
    void getAddresses_returnsEmptyFluxPendingImplementation() {
        UUID partyId = UUID.randomUUID();

        StepVerifier.create(service.getAddresses(partyId))
                .verifyComplete();
    }

    // ── addIdentityDocument ────────────────────────────────────────────────────

    @Test
    void addIdentityDocument_returnsBuiltDtoWithInputData() {
        UUID partyId = UUID.randomUUID();
        AddIdentityDocumentCommand command = new AddIdentityDocumentCommand();
        command.setType("PASSPORT");
        command.setNumber("XYZ123456");
        command.setExpiryDate(LocalDate.of(2030, 12, 31));

        when(customersApi.addTaxId(eq(partyId), any(), any()))
                .thenReturn(Mono.just(java.util.Map.of("identityDocumentId", UUID.randomUUID().toString())));

        StepVerifier.create(service.addIdentityDocument(partyId, command))
                .expectNextMatches(dto ->
                        dto.getDocumentId() != null
                        && "PASSPORT".equals(dto.getType())
                        && "XYZ123456".equals(dto.getNumber())
                        && LocalDate.of(2030, 12, 31).equals(dto.getExpiryDate())
                        && !dto.isVerified()
                )
                .verifyComplete();
    }

    // ── deleteIdentityDocument ─────────────────────────────────────────────────

    @Test
    void deleteIdentityDocument_delegatesToCustomersApi() {
        UUID partyId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        when(customersApi.removeTaxId(eq(partyId), eq(documentId), any())).thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.deleteIdentityDocument(partyId, documentId))
                .verifyComplete();

        verify(customersApi).removeTaxId(eq(partyId), eq(documentId), any());
    }

    // ── getContracts (stub) ────────────────────────────────────────────────────

    @Test
    void getContracts_readsDomainContracts() {
        UUID partyId = UUID.randomUUID();
        when(contractsApi.listByParty(eq(partyId), any())).thenReturn(Mono.just(java.util.Map.of()));

        StepVerifier.create(service.getContracts(partyId))
                .verifyComplete();

        verify(contractsApi).listByParty(eq(partyId), any());
    }

    // ── updateConsent ──────────────────────────────────────────────────────────

    @Test
    void updateConsent_grantedStatus_mapsToGrantedTrueAndDispatchesToCustomersApi() {
        UUID partyId = UUID.randomUUID();
        UUID consentId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        UpdateConsentCommand command = UpdateConsentCommand.builder()
                .status("GRANTED")
                .applicationId(applicationId)
                .build();

        when(customersApi.updateCustomerConsent(eq(partyId), eq(consentId),
                any(io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand.class), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateConsent(partyId, consentId, command))
                .verifyComplete();

        ArgumentCaptor<io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand> dtoCaptor =
                ArgumentCaptor.forClass(io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand.class);
        ArgumentCaptor<String> idemKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(customersApi).updateCustomerConsent(eq(partyId), eq(consentId),
                dtoCaptor.capture(), idemKeyCaptor.capture());

        io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand dispatched = dtoCaptor.getValue();
        assertThat(dispatched.getPartyId()).isEqualTo(partyId);
        assertThat(dispatched.getConsentId()).isEqualTo(consentId);
        assertThat(dispatched.getApplicationId()).isEqualTo(applicationId);
        assertThat(dispatched.getGranted()).isTrue();
        assertThat(idemKeyCaptor.getValue()).isNotBlank();
    }

    @Test
    void updateConsent_acceptedStatus_mapsToGrantedTrue() {
        UUID partyId = UUID.randomUUID();
        UUID consentId = UUID.randomUUID();

        UpdateConsentCommand command = UpdateConsentCommand.builder()
                .status("ACCEPTED")
                .build();

        when(customersApi.updateCustomerConsent(eq(partyId), eq(consentId),
                any(io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand.class), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateConsent(partyId, consentId, command))
                .verifyComplete();

        ArgumentCaptor<io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand> dtoCaptor =
                ArgumentCaptor.forClass(io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand.class);
        verify(customersApi).updateCustomerConsent(eq(partyId), eq(consentId), dtoCaptor.capture(), any());
        assertThat(dtoCaptor.getValue().getGranted()).isTrue();
    }

    @Test
    void updateConsent_revokedStatus_mapsToGrantedFalse() {
        UUID partyId = UUID.randomUUID();
        UUID consentId = UUID.randomUUID();

        UpdateConsentCommand command = UpdateConsentCommand.builder()
                .status("REVOKED")
                // applicationId intentionally null — pre-existing callers
                .build();

        when(customersApi.updateCustomerConsent(eq(partyId), eq(consentId),
                any(io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand.class), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateConsent(partyId, consentId, command))
                .verifyComplete();

        ArgumentCaptor<io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand> dtoCaptor =
                ArgumentCaptor.forClass(io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand.class);
        verify(customersApi).updateCustomerConsent(eq(partyId), eq(consentId), dtoCaptor.capture(), any());
        io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand dispatched = dtoCaptor.getValue();
        assertThat(dispatched.getApplicationId()).isNull();
        assertThat(dispatched.getGranted()).isFalse();
    }

    @Test
    void updateConsent_rejectedStatus_mapsToGrantedFalse() {
        UUID partyId = UUID.randomUUID();
        UUID consentId = UUID.randomUUID();

        UpdateConsentCommand command = UpdateConsentCommand.builder()
                .status("REJECTED")
                .build();

        when(customersApi.updateCustomerConsent(eq(partyId), eq(consentId),
                any(io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand.class), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateConsent(partyId, consentId, command))
                .verifyComplete();

        ArgumentCaptor<io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand> dtoCaptor =
                ArgumentCaptor.forClass(io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand.class);
        verify(customersApi).updateCustomerConsent(eq(partyId), eq(consentId), dtoCaptor.capture(), any());
        assertThat(dtoCaptor.getValue().getGranted()).isFalse();
    }

    @Test
    void updateConsent_unknownStatus_raisesBusinessExceptionAndDoesNotCallSdk() {
        UUID partyId = UUID.randomUUID();
        UUID consentId = UUID.randomUUID();

        UpdateConsentCommand command = UpdateConsentCommand.builder()
                .status("BOGUS")
                .build();

        StepVerifier.create(service.updateConsent(partyId, consentId, command))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(BusinessException.class);
                    BusinessException be = (BusinessException) error;
                    assertThat(be.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(be.getCode()).isEqualTo("INVALID_CONSENT_STATUS");
                    assertThat(be.getMessage())
                            .contains("BOGUS")
                            .contains("GRANTED")
                            .contains("ACCEPTED")
                            .contains("REVOKED")
                            .contains("REJECTED");
                })
                .verify();

        verify(customersApi, org.mockito.Mockito.never())
                .updateCustomerConsent(any(), any(),
                        any(io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand.class), any());
    }

    @Test
    void updateConsent_caseInsensitiveStatusIsAccepted() {
        UUID partyId = UUID.randomUUID();
        UUID consentId = UUID.randomUUID();

        UpdateConsentCommand command = UpdateConsentCommand.builder()
                .status("granted")
                .build();

        when(customersApi.updateCustomerConsent(eq(partyId), eq(consentId),
                any(io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand.class), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateConsent(partyId, consentId, command))
                .verifyComplete();

        ArgumentCaptor<io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand> dtoCaptor =
                ArgumentCaptor.forClass(io.fireflysoftware.domain.people.sdk.model.UpdateConsentCommand.class);
        verify(customersApi).updateCustomerConsent(eq(partyId), eq(consentId), dtoCaptor.capture(), any());
        assertThat(dtoCaptor.getValue().getGranted()).isTrue();
    }
}
