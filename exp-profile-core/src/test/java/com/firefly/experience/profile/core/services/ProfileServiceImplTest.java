package com.firefly.experience.profile.core.services;

import com.firefly.domain.common.contracts.sdk.api.ContractsApi;
import com.firefly.domain.people.sdk.api.CustomersApi;
import com.firefly.domain.people.sdk.model.NaturalPersonDTO;
import com.firefly.experience.profile.core.commands.AddAddressCommand;
import com.firefly.experience.profile.core.commands.AddIdentityDocumentCommand;
import com.firefly.experience.profile.core.commands.UpdateAddressCommand;
import com.firefly.experience.profile.core.commands.UpdateConsentCommand;
import com.firefly.experience.profile.core.commands.UpdateContactDataCommand;
import com.firefly.experience.profile.core.commands.UpdatePersonalDataCommand;
import com.firefly.experience.profile.core.queries.AddressDTO;
import com.firefly.experience.profile.core.queries.IdentityDocumentDTO;
import com.firefly.experience.profile.core.queries.ProfileDTO;
import com.firefly.experience.profile.core.services.impl.ProfileServiceImpl;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private CustomersApi customersApi;

    @Mock
    private ContractsApi contractsApi;

    private ProfileService service;

    @BeforeEach
    void setUp() {
        service = new ProfileServiceImpl(customersApi, contractsApi);
    }

    // ── getProfile ─────────────────────────────────────────────────────────────

    @Test
    void getProfile_mapsPersonalDataFromDownstreamDto() {
        UUID partyId = UUID.randomUUID();
        NaturalPersonDTO sdkDto = new NaturalPersonDTO();
        sdkDto.setGivenName("Jane");
        sdkDto.setFamilyName1("Doe");
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

        when(customersApi.addCustomerAddress(eq(partyId), any(), any())).thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.addAddress(partyId, command))
                .expectNextMatches(dto ->
                        dto.getAddressId() != null
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

        when(customersApi.addTaxId(eq(partyId), any(), any())).thenReturn(Mono.just(new Object()));

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
    void getContracts_returnsEmptyFluxPendingImplementation() {
        UUID partyId = UUID.randomUUID();

        StepVerifier.create(service.getContracts(partyId))
                .verifyComplete();
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
                any(com.firefly.domain.people.sdk.model.UpdateConsentCommand.class), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateConsent(partyId, consentId, command))
                .verifyComplete();

        ArgumentCaptor<com.firefly.domain.people.sdk.model.UpdateConsentCommand> dtoCaptor =
                ArgumentCaptor.forClass(com.firefly.domain.people.sdk.model.UpdateConsentCommand.class);
        ArgumentCaptor<String> idemKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(customersApi).updateCustomerConsent(eq(partyId), eq(consentId),
                dtoCaptor.capture(), idemKeyCaptor.capture());

        com.firefly.domain.people.sdk.model.UpdateConsentCommand dispatched = dtoCaptor.getValue();
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
                any(com.firefly.domain.people.sdk.model.UpdateConsentCommand.class), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateConsent(partyId, consentId, command))
                .verifyComplete();

        ArgumentCaptor<com.firefly.domain.people.sdk.model.UpdateConsentCommand> dtoCaptor =
                ArgumentCaptor.forClass(com.firefly.domain.people.sdk.model.UpdateConsentCommand.class);
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
                any(com.firefly.domain.people.sdk.model.UpdateConsentCommand.class), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateConsent(partyId, consentId, command))
                .verifyComplete();

        ArgumentCaptor<com.firefly.domain.people.sdk.model.UpdateConsentCommand> dtoCaptor =
                ArgumentCaptor.forClass(com.firefly.domain.people.sdk.model.UpdateConsentCommand.class);
        verify(customersApi).updateCustomerConsent(eq(partyId), eq(consentId), dtoCaptor.capture(), any());
        com.firefly.domain.people.sdk.model.UpdateConsentCommand dispatched = dtoCaptor.getValue();
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
                any(com.firefly.domain.people.sdk.model.UpdateConsentCommand.class), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateConsent(partyId, consentId, command))
                .verifyComplete();

        ArgumentCaptor<com.firefly.domain.people.sdk.model.UpdateConsentCommand> dtoCaptor =
                ArgumentCaptor.forClass(com.firefly.domain.people.sdk.model.UpdateConsentCommand.class);
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
                        any(com.firefly.domain.people.sdk.model.UpdateConsentCommand.class), any());
    }

    @Test
    void updateConsent_caseInsensitiveStatusIsAccepted() {
        UUID partyId = UUID.randomUUID();
        UUID consentId = UUID.randomUUID();

        UpdateConsentCommand command = UpdateConsentCommand.builder()
                .status("granted")
                .build();

        when(customersApi.updateCustomerConsent(eq(partyId), eq(consentId),
                any(com.firefly.domain.people.sdk.model.UpdateConsentCommand.class), any()))
                .thenReturn(Mono.just(new Object()));

        StepVerifier.create(service.updateConsent(partyId, consentId, command))
                .verifyComplete();

        ArgumentCaptor<com.firefly.domain.people.sdk.model.UpdateConsentCommand> dtoCaptor =
                ArgumentCaptor.forClass(com.firefly.domain.people.sdk.model.UpdateConsentCommand.class);
        verify(customersApi).updateCustomerConsent(eq(partyId), eq(consentId), dtoCaptor.capture(), any());
        assertThat(dtoCaptor.getValue().getGranted()).isTrue();
    }
}
