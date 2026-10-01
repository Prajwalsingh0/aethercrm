package com.aethercrm.crm;

import com.aethercrm.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends TenantAwareEntity {

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String website;

    @Column(length = 100)
    private String industry;

    @Column(length = 50)
    private String size;

    @Column(name = "annual_revenue", precision = 19, scale = 4)
    private BigDecimal annualRevenue;

    @Column(length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE";

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
