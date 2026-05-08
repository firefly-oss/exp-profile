package com.firefly.experience.profile.core.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to update the consent status for a specific consent type.
 * <p>
 * Accepted values for {@code status} are {@code GRANTED}, {@code ACCEPTED},
 * {@code REVOKED} and {@code REJECTED} (case-insensitive). The first two map
 * to {@code granted = true} and the last two to {@code granted = false} in
 * the downstream domain payload.
 * <p>
 * The optional {@code applicationId} field is a soft link to the loan/onboarding
 * application that originated this consent change. It is propagated downstream to
 * {@code domain-customer-people} so that consents can be traced back to the
 * application that produced them. No validation is enforced — the field is purely
 * informational and may be {@code null} for consent changes initiated outside the
 * scope of an application (e.g. self-service preference updates).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConsentCommand {

    @NotBlank
    @Pattern(
            regexp = "^(GRANTED|ACCEPTED|REVOKED|REJECTED)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "status must be one of GRANTED, ACCEPTED, REVOKED, REJECTED"
    )
    private String status;

    /**
     * Optional soft link to the originating application (loan or onboarding).
     */
    private UUID applicationId;
}
