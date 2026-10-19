package com.firefly.experience.profile.core.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Metadata for a document uploaded to a party's profile.
 * <p>
 * Document content is not embedded here; retrieve it separately
 * via {@link com.firefly.experience.profile.core.services.ProfileService#downloadDocument(UUID, UUID)}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {

    UUID documentId;
    String name;
    String type;
    LocalDateTime uploadedAt;
    Long size;
}
