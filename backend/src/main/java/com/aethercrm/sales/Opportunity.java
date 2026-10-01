package com.aethercrm.sales;

import com.aethercrm.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity extends TenantAwareEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "contact_id")
    private UUID contactId;

    @Column(name = "stage_id", nullable = false)
    private UUID stageId;

    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(length = 3)
    @Builder.Default
    private String currency = "INR";

    @Builder.Default
    private Integer probability = 0;

    @Column(name = "expected_close_date")
    private LocalDate expectedCloseDate;

    @Column(name = "actual_close_date")
    private LocalDate actualCloseDate;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(length = 100)
    private String source;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "win_reason")
    private String winReason;

    @Column(name = "loss_reason")
    private String lossReason;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "tags_json", columnDefinition = "TEXT")
    private String tagsJson;

    @Column(name = "custom_fields_json", columnDefinition = "TEXT")
    private String customFieldsJson;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
