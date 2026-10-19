package com.firefly.experience.profile.core.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Aggregated profile summary for a party, composed from personal data,
 * primary contact details, addresses, and identity documents.
 * <p>
 * Built by {@link com.firefly.experience.profile.core.services.ProfileService#getProfile(UUID)}
 * using parallel downstream calls via {@code Mono.zip()}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

    UUID partyId;
    String firstName;
    String lastName;
    String email;
    String phone;
    LocalDate dateOfBirth;
    List<AddressDTO> addresses;
    List<IdentityDocumentDTO> identityDocuments;
}
