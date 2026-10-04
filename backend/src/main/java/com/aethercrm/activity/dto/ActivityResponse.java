package com.aethercrm.activity.dto;

import com.aethercrm.activity.Activity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ActivityResponse {
    private UUID id;
    private String type;
    private String subject;
    private String description;
    private String status;
    private String priority;
    private Instant dueAt;
    private Instant completedAt;
    private UUID ownerId;
    private String relatedType;
    private UUID relatedId;
    private Instant createdAt;
    private Instant updatedAt;

    public static ActivityResponse from(Activity a) {
        return ActivityResponse.builder()
                .id(a.getId())
                .type(a.getType())
                .subject(a.getSubject())
                .description(a.getDescription())
                .status(a.getStatus())
                .priority(a.getPriority())
                .dueAt(a.getDueAt())
                .completedAt(a.getCompletedAt())
                .ownerId(a.getOwnerId())
                .relatedType(a.getRelatedType())
                .relatedId(a.getRelatedId())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
