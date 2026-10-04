package com.aethercrm.support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank
    private String body;
    private boolean isInternal = false;
}
