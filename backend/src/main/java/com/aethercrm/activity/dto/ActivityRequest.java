package com.aethercrm.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ActivityRequest {
    @NotBlank
    @Size(max = 50)
    private String type;

    @NotBlank
    @Size(max = 500)
    private String subject;

    private String description;

    @Size(max = 50)
    private String status;

    @Size(max = 20)
    private String priority;

    private Instant dueAt;
    private UUID ownerId;
    private String relatedType;
    private UUID relatedId;
}
