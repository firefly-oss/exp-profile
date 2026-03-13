package com.firefly.experience.profile.core.queries;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Identity document associated with a party (e.g. passport, national ID).
 * <p>
 * The {@code isVerified} flag reflects whether the document passed
 * KYC evidence validation in {@code domain-customer-kyc-kyb}.
 */
@Value
@Builder
public class IdentityDocumentDTO {

    UUID documentId;
    /** Document kind: NATIONAL_ID, PASSPORT, DRIVERS_LICENSE, etc. */
    String type;
    String number;
    LocalDate expiryDate;
    boolean verified;
}
