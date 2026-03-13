package com.firefly.experience.profile.core.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Command to register a new identity document for a party
 * (e.g. NATIONAL_ID, PASSPORT, DRIVERS_LICENSE).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddIdentityDocumentCommand {

    @NotBlank
    private String type;

    @NotBlank
    private String number;

    @NotNull
    private LocalDate expiryDate;
}
