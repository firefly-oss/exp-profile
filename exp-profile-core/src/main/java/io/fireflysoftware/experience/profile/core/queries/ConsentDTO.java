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

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consent record for a party, joined with the catalogue metadata the channel
 * needs to render an opt-in checkbox.
 * <p>
 * Catalogue side ({@code required}, {@code label}, {@code order}) always comes
 * from the active consent catalogue; per-party side ({@code status},
 * {@code updatedAt}) reflects the user's last choice. When the user has not
 * recorded a choice yet, {@code status} is {@code PENDING} and
 * {@code updatedAt} is {@code null}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentDTO {

    UUID consentId;
    String type;
    /** ACCEPTED, REJECTED or PENDING — PENDING is the default until the user records a choice. */
    String status;
    LocalDateTime updatedAt;
    /** Whether the user MUST tick this consent before continuing. */
    boolean required;
    /** Human-readable label, may contain HTML for inline links to legal copy. */
    String label;
    /** Rendering order, ascending. */
    Integer order;
}
