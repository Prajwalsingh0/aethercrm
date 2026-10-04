package com.aethercrm.marketing;

import com.aethercrm.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Campaign extends TenantAwareEntity {
    @Column(nullable = false) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(nullable = false, length = 50) @Builder.Default private String type = "EMAIL";
    @Column(nullable = false, length = 50) @Builder.Default private String status = "DRAFT";
    @Column(precision = 19, scale = 4) private BigDecimal budget;
    @Column(length = 3) @Builder.Default private String currency = "INR";
    @Column(name = "start_date") private LocalDate startDate;
    @Column(name = "end_date") private LocalDate endDate;
    @Column(name = "target_audience", columnDefinition = "TEXT") private String targetAudience;
    @Column(name = "metrics_json", columnDefinition = "TEXT") private String metricsJson;
    @Column(name = "owner_id") private UUID ownerId;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "updated_by") private UUID updatedBy;
    @Column(name = "deleted_at") private Instant deletedAt;
}
