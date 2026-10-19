package com.firefly.experience.profile.core.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight summary of a contract associated with a party.
 * <p>
 * Full contract details (terms, documents) are available via
 * {@code domain-common-contracts} or {@code core-common-contract-mgmt} directly.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractSummaryDTO {

    UUID contractId;
    /** Contract number as assigned by the originating service. */
    String type;
    /** Current contract status (e.g. ACTIVE, CLOSED). */
    String status;
    LocalDateTime signedAt;
}
