package com.firefly.experience.profile.core.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to upload a document to a party's profile.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocumentCommand {

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    @NotNull
    private byte[] content;
}
