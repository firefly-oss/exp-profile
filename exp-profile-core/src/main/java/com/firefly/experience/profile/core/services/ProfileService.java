package com.firefly.experience.profile.core.services;

import com.firefly.experience.profile.core.commands.*;
import com.firefly.experience.profile.core.queries.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Experience-layer service for customer profile self-service operations.
 * <p>
 * This service is stateless: every method performs an on-demand composition
 * of downstream domain SDK calls. No journey state is persisted between requests.
 * <p>
 * Downstream dependencies:
 * <ul>
 *   <li>{@code domain-customer-people-sdk} — personal data, contact data, addresses,
 *       identity documents, and consents</li>
 *   <li>{@code domain-common-contracts-sdk} — contract summaries</li>
 * </ul>
 */
public interface ProfileService {

    // ── Profile ────────────────────────────────────────────────────────────────

    /**
     * Retrieves the full profile for the given party by aggregating personal data,
     * addresses, and identity documents in parallel via {@code Mono.zip()}.
     */
    Mono<ProfileDTO> getProfile(UUID partyId);

    // ── Personal & Contact Data ────────────────────────────────────────────────

    /**
     * Partially updates personal data (name, date of birth) for the given party.
     * Only non-null fields in the command are forwarded to the domain service.
     */
    Mono<Void> updatePersonalData(UUID partyId, UpdatePersonalDataCommand command);

    /**
     * Partially updates contact data (email, phone) for the given party.
     * Only non-null fields in the command are forwarded to the domain service.
     */
    Mono<Void> updateContactData(UUID partyId, UpdateContactDataCommand command);

    // ── Addresses ─────────────────────────────────────────────────────────────

    /**
     * Returns all addresses associated with the given party.
     */
    Flux<AddressDTO> getAddresses(UUID partyId);

    /**
     * Adds a new address to the given party's profile.
     *
     * @return the created address with its assigned {@code addressId}
     */
    Mono<AddressDTO> addAddress(UUID partyId, AddAddressCommand command);

    /**
     * Updates an existing address for the given party.
     * Only non-null fields in the command are applied.
     *
     * @return the updated address
     */
    Mono<AddressDTO> updateAddress(UUID partyId, UUID addressId, UpdateAddressCommand command);

    /**
     * Removes an address from the given party's profile.
     */
    Mono<Void> deleteAddress(UUID partyId, UUID addressId);

    // ── Documents ─────────────────────────────────────────────────────────────

    /**
     * Returns metadata for all documents uploaded by the given party.
     */
    Flux<DocumentDTO> getDocuments(UUID partyId);

    /**
     * Uploads a document and returns its metadata.
     */
    Mono<DocumentDTO> uploadDocument(UUID partyId, UploadDocumentCommand command);

    /**
     * Downloads the raw byte content of the given document.
     */
    Mono<byte[]> downloadDocument(UUID partyId, UUID documentId);

    // ── Consents ───────────────────────────────────────────────────────────────

    /**
     * Returns all consents registered for the given party.
     */
    Flux<ConsentDTO> getConsents(UUID partyId);

    /**
     * Updates the status (ACCEPTED / REVOKED) of a specific consent.
     */
    Mono<Void> updateConsent(UUID partyId, UUID consentId, UpdateConsentCommand command);

    // ── Identity Documents ─────────────────────────────────────────────────────

    /**
     * Returns all identity documents associated with the given party.
     */
    Flux<IdentityDocumentDTO> getIdentityDocuments(UUID partyId);

    /**
     * Registers a new identity document (passport, national ID, etc.) for the party.
     *
     * @return the created identity document record
     */
    Mono<IdentityDocumentDTO> addIdentityDocument(UUID partyId, AddIdentityDocumentCommand command);

    /**
     * Removes an identity document from the given party's profile.
     */
    Mono<Void> deleteIdentityDocument(UUID partyId, UUID documentId);

    // ── Contracts ─────────────────────────────────────────────────────────────

    /**
     * Returns contract summaries associated with the given party,
     * filtered to active contracts only.
     */
    Flux<ContractSummaryDTO> getContracts(UUID partyId);
}
