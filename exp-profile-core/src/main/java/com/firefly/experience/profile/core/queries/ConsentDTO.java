package com.firefly.experience.profile.core.queries;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consent record for a party, e.g. marketing consent, data processing consent.
 * <p>
 * {@code status} is either {@code ACCEPTED} or {@code REVOKED}, mapped from
 * the downstream {@code granted} boolean.
 */
@Value
@Builder
public class ConsentDTO {

    UUID consentId;
    String type;
    /** ACCEPTED or REVOKED */
    String status;
    LocalDateTime updatedAt;
}
