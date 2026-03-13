package com.firefly.experience.profile.web.controllers;

import com.firefly.experience.profile.core.commands.AddAddressCommand;
import com.firefly.experience.profile.core.commands.AddIdentityDocumentCommand;
import com.firefly.experience.profile.core.commands.UpdateAddressCommand;
import com.firefly.experience.profile.core.commands.UpdateConsentCommand;
import com.firefly.experience.profile.core.commands.UpdateContactDataCommand;
import com.firefly.experience.profile.core.commands.UpdatePersonalDataCommand;
import com.firefly.experience.profile.core.queries.AddressDTO;
import com.firefly.experience.profile.core.queries.IdentityDocumentDTO;
import com.firefly.experience.profile.core.queries.ProfileDTO;
import com.firefly.experience.profile.core.services.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProfileController}.
 * Uses {@code WebTestClient.bindToController()} to avoid loading the full Spring
 * application context, keeping tests fast and isolated.
 */
@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private ProfileService profileService;

    private WebTestClient webTestClient;

    private static final String BASE_PATH = "/api/v1/experience/profile";

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
                .bindToController(new ProfileController(profileService))
                .build();
    }

    @Test
    void getProfile_returns200WithProfileDto() {
        ProfileDTO profileDto = ProfileDTO.builder()
                .partyId(UUID.randomUUID())
                .firstName("Jane")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .addresses(Collections.emptyList())
                .identityDocuments(Collections.emptyList())
                .build();

        when(profileService.getProfile(any())).thenReturn(Mono.just(profileDto));

        webTestClient.get()
                .uri(BASE_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProfileDTO.class)
                .value(body -> {
                    assertThat(body.getFirstName()).isEqualTo("Jane");
                    assertThat(body.getLastName()).isEqualTo("Doe");
                });
    }

    @Test
    void updatePersonalData_returns204() {
        when(profileService.updatePersonalData(any(), any())).thenReturn(Mono.empty());

        UpdatePersonalDataCommand command = new UpdatePersonalDataCommand();
        command.setFirstName("Alice");

        webTestClient.patch()
                .uri(BASE_PATH + "/personal-data")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange()
                .expectStatus().isNoContent();

        verify(profileService).updatePersonalData(any(), any());
    }

    @Test
    void updateContactData_returns204() {
        when(profileService.updateContactData(any(), any())).thenReturn(Mono.empty());

        UpdateContactDataCommand command = new UpdateContactDataCommand();
        command.setEmail("alice@example.com");

        webTestClient.patch()
                .uri(BASE_PATH + "/contact-data")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getAddresses_returns200WithEmptyList() {
        when(profileService.getAddresses(any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_PATH + "/addresses")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AddressDTO.class)
                .hasSize(0);
    }

    @Test
    void addAddress_returns201WithCreatedDto() {
        AddressDTO created = AddressDTO.builder()
                .addressId(UUID.randomUUID())
                .type("HOME")
                .street("Calle Gran Via 1")
                .city("Madrid")
                .postalCode("28013")
                .country("ES")
                .build();

        when(profileService.addAddress(any(), any())).thenReturn(Mono.just(created));

        AddAddressCommand command = new AddAddressCommand();
        command.setType("HOME");
        command.setStreet("Calle Gran Via 1");
        command.setCity("Madrid");
        command.setPostalCode("28013");
        command.setCountry("ES");

        webTestClient.post()
                .uri(BASE_PATH + "/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AddressDTO.class)
                .value(body -> assertThat(body.getCity()).isEqualTo("Madrid"));
    }

    @Test
    void updateAddress_returns200WithUpdatedDto() {
        UUID addressId = UUID.randomUUID();
        AddressDTO updated = AddressDTO.builder()
                .addressId(addressId)
                .city("Barcelona")
                .postalCode("08001")
                .build();

        when(profileService.updateAddress(any(), any(), any())).thenReturn(Mono.just(updated));

        UpdateAddressCommand command = new UpdateAddressCommand();
        command.setCity("Barcelona");

        webTestClient.put()
                .uri(BASE_PATH + "/addresses/" + addressId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AddressDTO.class)
                .value(body -> assertThat(body.getCity()).isEqualTo("Barcelona"));
    }

    @Test
    void deleteAddress_returns204() {
        when(profileService.deleteAddress(any(), any())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE_PATH + "/addresses/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getConsents_returns200() {
        when(profileService.getConsents(any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_PATH + "/consents")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateConsent_returns200() {
        when(profileService.updateConsent(any(), any(), any())).thenReturn(Mono.empty());

        UpdateConsentCommand command = new UpdateConsentCommand();
        command.setStatus("ACCEPTED");

        webTestClient.put()
                .uri(BASE_PATH + "/consents/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getIdentityDocuments_returns200() {
        when(profileService.getIdentityDocuments(any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_PATH + "/identity-documents")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void addIdentityDocument_returns201WithCreatedDto() {
        IdentityDocumentDTO created = IdentityDocumentDTO.builder()
                .documentId(UUID.randomUUID())
                .type("PASSPORT")
                .number("XYZ123456")
                .expiryDate(LocalDate.of(2030, 12, 31))
                .verified(false)
                .build();

        when(profileService.addIdentityDocument(any(), any())).thenReturn(Mono.just(created));

        AddIdentityDocumentCommand command = new AddIdentityDocumentCommand();
        command.setType("PASSPORT");
        command.setNumber("XYZ123456");
        command.setExpiryDate(LocalDate.of(2030, 12, 31));

        webTestClient.post()
                .uri(BASE_PATH + "/identity-documents")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(IdentityDocumentDTO.class)
                .value(body -> assertThat(body.getType()).isEqualTo("PASSPORT"));
    }

    @Test
    void deleteIdentityDocument_returns204() {
        when(profileService.deleteIdentityDocument(any(), any())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE_PATH + "/identity-documents/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getContracts_returns200() {
        when(profileService.getContracts(any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_PATH + "/contracts")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
    }
}
