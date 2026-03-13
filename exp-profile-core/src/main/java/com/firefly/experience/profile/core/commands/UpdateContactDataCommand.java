package com.firefly.experience.profile.core.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to update the contact details of an authenticated party.
 * Both fields are optional — only non-null values will be applied downstream.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateContactDataCommand {

    private String email;
    private String phone;
}
