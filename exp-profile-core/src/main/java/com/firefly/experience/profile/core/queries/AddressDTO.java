package com.firefly.experience.profile.core.queries;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Address associated with a party's profile.
 * <p>
 * The {@code type} field maps from the downstream {@code AddressKindEnum}
 * (e.g. HOME, WORK, BILLING) and is surfaced as a plain String
 * to keep the experience-layer contract independent of domain enums.
 */
@Value
@Builder
public class AddressDTO {

    UUID addressId;
    /** Address kind: HOME, WORK, BILLING, etc. */
    String type;
    String street;
    String city;
    String postalCode;
    String country;
}
