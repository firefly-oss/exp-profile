package com.firefly.experience.profile.core.commands;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to add a new address to a party's profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddAddressCommand {

    @NotBlank
    private String type;

    @NotBlank
    private String street;

    @NotBlank
    private String city;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String country;
}
