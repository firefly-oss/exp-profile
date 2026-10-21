/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.profile.core.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Address associated with a party's profile.
 * <p>
 * The {@code type} field maps from the downstream {@code AddressKindEnum}
 * (e.g. HOME, WORK, BILLING) and is surfaced as a plain String
 * to keep the experience-layer contract independent of domain enums.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    UUID addressId;
    /** Address kind: HOME, WORK, BILLING, etc. */
    String type;
    String street;
    String city;
    String postalCode;
    String country;
}
