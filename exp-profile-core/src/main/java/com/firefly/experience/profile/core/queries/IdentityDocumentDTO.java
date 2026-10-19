package com.firefly.experience.profile.core.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Identity document associated with a party (e.g. passport, national ID).
 * <p>
 * The {@code isVerified} flag reflects whether the document passed
 * KYC evidence validation in {@code domain-customer-kyc-kyb}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityDocumentDTO {

    UUID documentId;
    /** Document kind: NATIONAL_ID, PASSPORT, DRIVERS_LICENSE, etc. */
    String type;
    String number;
    LocalDate expiryDate;
    boolean verified;
}
