package com.firefly.experience.profile.core.queries;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Single template from the platform-wide consent catalogue, ready to be
 * rendered as an opt-in checkbox on a journey screen.
 * <p>
 * Differs from {@link ConsentDTO} (a per-party consent record with a
 * GRANTED/REVOKED status) in that this is the catalogue side: the consent
 * the channel can ask the user to accept, plus the metadata it needs to
 * decide how to render it.
 */
@Value
@Builder
public class ConsentCatalogEntryDTO {

    /** Stable consent identifier; used as the option value when persisting the user's choice. */
    UUID consentId;

    /** Type code (TERMS, PRIVACY, MARKETING, ...). */
    String type;

    /** Human-readable description, used as the checkbox label. */
    String description;

    /** Schema version of the consent text; persisted with the user's acceptance for audit. */
    String version;

    /** Whether the user MUST tick this consent before continuing. */
    boolean required;

    /** Rendering order, ascending. */
    Integer order;

    /** Product this consent applies to ({@code null} = global). */
    String applicableProduct;
}
