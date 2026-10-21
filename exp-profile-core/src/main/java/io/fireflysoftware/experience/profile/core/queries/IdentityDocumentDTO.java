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
import java.util.UUID;

/**
 * Identity document associated with a party (e.g. passport, national ID).
 * <p>
 * The {@code isVerified} flag reflects whether the document passed
 * KYC evidence validation in {@code domain-customer-kyc-kyb}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityDocumentDTO {

    UUID documentId;
    /** Document kind: NATIONAL_ID, PASSPORT, DRIVERS_LICENSE, etc. */
    String type;
    String number;
    LocalDate expiryDate;
    boolean verified;
}
