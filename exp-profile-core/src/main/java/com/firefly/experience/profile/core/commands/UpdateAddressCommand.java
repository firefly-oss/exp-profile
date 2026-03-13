package com.firefly.experience.profile.core.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to update fields of an existing address.
 * All fields are optional — only non-null values will be applied downstream.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAddressCommand {

    private String street;
    private String city;
    private String postalCode;
    private String country;
}
