package com.aethercrm.support;

import com.aethercrm.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket extends TenantAwareEntity {

    @Column(name = "ticket_number", nullable = false, length = 50)
    private String ticketNumber;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "OPEN";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String priority = "MEDIUM";

    @Column(length = 100)
    private String category;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(name = "sla_due_at")
    private Instant slaDueAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "satisfaction_score")
    private Integer satisfactionScore;

    @Column(name = "tags_json", columnDefinition = "TEXT")
    private String tagsJson;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
