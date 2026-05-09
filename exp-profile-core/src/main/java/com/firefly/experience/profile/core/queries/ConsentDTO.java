package com.firefly.experience.profile.core.queries;

import lombok.Builder;
import lombok.Value;

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
@Value
@Builder
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
