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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Aggregated profile summary for a party, composed from personal data,
 * primary contact details, addresses, and identity documents.
 * <p>
 * Built by {@link io.fireflysoftware.experience.profile.core.services.ProfileService#getProfile(UUID)}
 * using parallel downstream calls via {@code Mono.zip()}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

    UUID partyId;
    String firstName;
    String lastName;
    String email;
    String phone;
    LocalDate dateOfBirth;
    List<AddressDTO> addresses;
    List<IdentityDocumentDTO> identityDocuments;
}
