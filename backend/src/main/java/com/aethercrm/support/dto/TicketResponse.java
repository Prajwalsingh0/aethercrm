package com.aethercrm.support.dto;

import com.aethercrm.support.Ticket;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TicketResponse {
    private UUID id;
    private String ticketNumber;
    private String subject;
    private String description;
    private String status;
    private String priority;
    private String category;
    private UUID contactId;
    private UUID accountId;
    private UUID assigneeId;
    private Instant slaDueAt;
    private Instant resolvedAt;
    private Integer satisfactionScore;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CommentResponse> comments;

    @Data
    @Builder
    public static class CommentResponse {
        private UUID id;
        private UUID authorId;
        private String body;
        private boolean isInternal;
        private Instant createdAt;
    }

    public static TicketResponse from(Ticket t) {
        return TicketResponse.builder()
                .id(t.getId())
                .ticketNumber(t.getTicketNumber())
                .subject(t.getSubject())
                .description(t.getDescription())
                .status(t.getStatus())
                .priority(t.getPriority())
                .category(t.getCategory())
                .contactId(t.getContactId())
                .accountId(t.getAccountId())
                .assigneeId(t.getAssigneeId())
                .slaDueAt(t.getSlaDueAt())
                .resolvedAt(t.getResolvedAt())
                .satisfactionScore(t.getSatisfactionScore())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
