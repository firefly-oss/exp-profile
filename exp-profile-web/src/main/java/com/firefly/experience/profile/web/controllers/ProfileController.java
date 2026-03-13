package com.firefly.experience.profile.web.controllers;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for customer profile self-service endpoints.
 * <p>
 * Each endpoint is atomic and stateless — it aggregates or mutates domain data
 * on demand without relying on any persistent journey state.
 * <p>
 * Base path: {@code /api/v1/experience/profile}
 * <p>
 * Authentication: All endpoints require a valid JWT. The party ID is extracted
 * from the token by the gateway. For this MVP the partyId is provided as a
 * placeholder; production implementations should resolve it from the security context.
 */
@RestController
@RequestMapping("/api/v1/experience/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile self-service")
public class ProfileController {

    private final ProfileService profileService;

    // ── Profile ────────────────────────────────────────────────────────────────

    /**
     * Returns the aggregated profile for the authenticated party,
     * including personal data, addresses, and identity documents.
     *
     * @return reactive wrapper containing the HTTP response with the party's {@link ProfileDTO}
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get profile",
            description = "Returns the aggregated profile for the authenticated party, "
                    + "including personal data, addresses, and identity documents."
    )
    public Mono<ResponseEntity<ProfileDTO>> getProfile() {
        // TODO: Extract partyId from JWT token (e.g. via SecurityContextHolder or @CurrentUser)
        UUID partyId = UUID.randomUUID();
        return profileService.getProfile(partyId)
                .map(ResponseEntity::ok);
    }

    // ── Personal & Contact Data ────────────────────────────────────────────────

    /**
     * Partially updates the personal data (name, date of birth) of the authenticated party.
     * Only non-null fields in the command body are applied.
     *
     * @param command the personal data fields to update
     * @return reactive wrapper containing a 204 No Content response on success
     */
    @PatchMapping(value = "/personal-data",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update personal data",
            description = "Partially updates the personal data (name, date of birth) "
                    + "of the authenticated party. Only provided fields are applied."
    )
    public Mono<ResponseEntity<Void>> updatePersonalData(
            @Valid @RequestBody UpdatePersonalDataCommand command) {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.updatePersonalData(partyId, command)
                .thenReturn(ResponseEntity.<Void>noContent().build());
    }

    /**
     * Partially updates the contact data (email, phone) of the authenticated party.
     * Only non-null fields in the command body are applied.
     *
     * @param command the contact data fields to update
     * @return reactive wrapper containing a 204 No Content response on success
     */
    @PatchMapping(value = "/contact-data",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update contact data",
            description = "Partially updates the contact data (email, phone) "
                    + "of the authenticated party. Only provided fields are applied."
    )
    public Mono<ResponseEntity<Void>> updateContactData(
            @Valid @RequestBody UpdateContactDataCommand command) {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.updateContactData(partyId, command)
                .thenReturn(ResponseEntity.<Void>noContent().build());
    }

    // ── Addresses ─────────────────────────────────────────────────────────────

    /**
     * Returns all addresses associated with the authenticated party.
     *
     * @return reactive stream of {@link AddressDTO} records
     */
    @GetMapping(value = "/addresses", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List addresses",
            description = "Returns all addresses associated with the authenticated party."
    )
    public Flux<AddressDTO> getAddresses() {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.getAddresses(partyId);
    }

    /**
     * Adds a new address to the authenticated party's profile.
     *
     * @param command the address details to register
     * @return reactive wrapper containing a 201 Created response with the created {@link AddressDTO}
     */
    @PostMapping(value = "/addresses",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Add address",
            description = "Adds a new address to the authenticated party's profile."
    )
    public Mono<ResponseEntity<AddressDTO>> addAddress(
            @Valid @RequestBody AddAddressCommand command) {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.addAddress(partyId, command)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    /**
     * Updates an existing address for the authenticated party.
     * Only non-null fields in the command body are applied.
     *
     * @param id      the UUID of the address to update
     * @param command the address fields to update
     * @return reactive wrapper containing the updated {@link AddressDTO}
     */
    @PutMapping(value = "/addresses/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update address",
            description = "Updates an existing address for the authenticated party. "
                    + "Only provided fields are applied."
    )
    public Mono<ResponseEntity<AddressDTO>> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAddressCommand command) {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.updateAddress(partyId, id, command)
                .map(ResponseEntity::ok);
    }

    /**
     * Removes an address from the authenticated party's profile.
     *
     * @param id the UUID of the address to delete
     * @return reactive wrapper containing a 204 No Content response on success
     */
    @DeleteMapping(value = "/addresses/{id}")
    @Operation(
            summary = "Delete address",
            description = "Removes an address from the authenticated party's profile."
    )
    public Mono<ResponseEntity<Void>> deleteAddress(@PathVariable UUID id) {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.deleteAddress(partyId, id)
                .thenReturn(ResponseEntity.<Void>noContent().build());
    }

    // ── Documents ─────────────────────────────────────────────────────────────

    /**
     * Returns metadata for all documents uploaded by the authenticated party.
     *
     * @return reactive stream of {@link DocumentDTO} metadata records
     */
    @GetMapping(value = "/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List documents",
            description = "Returns metadata for all documents uploaded by the authenticated party."
    )
    public Flux<DocumentDTO> getDocuments() {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.getDocuments(partyId);
    }

    /**
     * Uploads a document to the authenticated party's profile and returns its metadata.
     *
     * @param command the document content and metadata to upload
     * @return reactive wrapper containing a 201 Created response with the resulting {@link DocumentDTO}
     */
    @PostMapping(value = "/documents",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Upload document",
            description = "Uploads a document and returns its metadata."
    )
    public Mono<ResponseEntity<DocumentDTO>> uploadDocument(
            @Valid @RequestBody UploadDocumentCommand command) {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.uploadDocument(partyId, command)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    /**
     * Downloads the raw byte content of the specified document.
     *
     * @param id the UUID of the document to download
     * @return reactive wrapper containing the document bytes as an octet-stream response
     */
    @GetMapping(value = "/documents/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(
            summary = "Download document",
            description = "Downloads the raw content of the specified document."
    )
    public Mono<ResponseEntity<byte[]>> downloadDocument(@PathVariable UUID id) {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.downloadDocument(partyId, id)
                .map(bytes -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(bytes));
    }

    // ── Consents ───────────────────────────────────────────────────────────────

    /**
     * Returns all consents registered for the authenticated party.
     *
     * @return reactive stream of {@link ConsentDTO} records
     */
    @GetMapping(value = "/consents", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List consents",
            description = "Returns all consents registered for the authenticated party."
    )
    public Flux<ConsentDTO> getConsents() {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.getConsents(partyId);
    }

    /**
     * Updates the status (ACCEPTED or REVOKED) of a specific consent for the authenticated party.
     *
     * @param id      the UUID of the consent record to update
     * @param command the new consent status
     * @return reactive wrapper containing a 200 OK response on success
     */
    @PutMapping(value = "/consents/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Update consent",
            description = "Updates the status (ACCEPTED / REVOKED) of a specific consent."
    )
    public Mono<ResponseEntity<Void>> updateConsent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateConsentCommand command) {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.updateConsent(partyId, id, command)
                .thenReturn(ResponseEntity.<Void>ok().build());
    }

    // ── Identity Documents ─────────────────────────────────────────────────────

    /**
     * Returns all identity documents associated with the authenticated party.
     *
     * @return reactive stream of {@link IdentityDocumentDTO} records
     */
    @GetMapping(value = "/identity-documents", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List identity documents",
            description = "Returns all identity documents associated with the authenticated party."
    )
    public Flux<IdentityDocumentDTO> getIdentityDocuments() {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.getIdentityDocuments(partyId);
    }

    /**
     * Registers a new identity document (passport, national ID, driver's licence, etc.)
     * for the authenticated party.
     *
     * @param command the identity document details to register
     * @return reactive wrapper containing a 201 Created response with the resulting {@link IdentityDocumentDTO}
     */
    @PostMapping(value = "/identity-documents",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Add identity document",
            description = "Registers a new identity document (passport, national ID, etc.) "
                    + "for the authenticated party."
    )
    public Mono<ResponseEntity<IdentityDocumentDTO>> addIdentityDocument(
            @Valid @RequestBody AddIdentityDocumentCommand command) {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.addIdentityDocument(partyId, command)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    /**
     * Removes an identity document from the authenticated party's profile.
     *
     * @param id the UUID of the identity document to delete
     * @return reactive wrapper containing a 204 No Content response on success
     */
    @DeleteMapping(value = "/identity-documents/{id}")
    @Operation(
            summary = "Delete identity document",
            description = "Removes an identity document from the authenticated party's profile."
    )
    public Mono<ResponseEntity<Void>> deleteIdentityDocument(@PathVariable UUID id) {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.deleteIdentityDocument(partyId, id)
                .thenReturn(ResponseEntity.<Void>noContent().build());
    }

    // ── Contracts ─────────────────────────────────────────────────────────────

    /**
     * Returns active contract summaries associated with the authenticated party.
     *
     * @return reactive stream of {@link ContractSummaryDTO} records
     */
    @GetMapping(value = "/contracts", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "List contracts",
            description = "Returns active contract summaries associated with the authenticated party."
    )
    public Flux<ContractSummaryDTO> getContracts() {
        // TODO: Extract partyId from JWT token
        UUID partyId = UUID.randomUUID();
        return profileService.getContracts(partyId);
    }
}
