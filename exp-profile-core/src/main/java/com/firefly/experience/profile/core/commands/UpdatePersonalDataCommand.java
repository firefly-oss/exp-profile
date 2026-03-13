package com.firefly.experience.profile.core.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Command to update the personal data of an authenticated party.
 * All fields are optional — only non-null values will be applied downstream.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonalDataCommand {

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
}
