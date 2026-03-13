package com.firefly.experience.profile.core.commands;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to update the consent status for a specific consent type.
 * {@code status} must be either {@code ACCEPTED} or {@code REVOKED}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConsentCommand {

    @NotBlank
    private String status;
}
