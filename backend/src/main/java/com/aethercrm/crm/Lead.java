package com.aethercrm.crm;

import com.aethercrm.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead extends TenantAwareEntity {

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 255)
    private String company;

    @Column(length = 150)
    private String title;

    @Column(length = 100)
    private String source;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "NEW";

    @Builder.Default
    private Integer score = 0;

    @Column(name = "score_factors_json", columnDefinition = "TEXT")
    private String scoreFactorsJson;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 500)
    private String website;

    @Column(name = "annual_revenue", precision = 19, scale = 4)
    private BigDecimal annualRevenue;

    @Column(length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "tags_json", columnDefinition = "TEXT")
    private String tagsJson;

    @Column(name = "custom_fields_json", columnDefinition = "TEXT")
    private String customFieldsJson;

    @Column(name = "converted_at")
    private Instant convertedAt;

    @Column(name = "converted_contact_id")
    private UUID convertedContactId;

    @Column(name = "converted_account_id")
    private UUID convertedAccountId;

    @Column(name = "converted_opportunity_id")
    private UUID convertedOpportunityId;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public String getFullName() {
        String f = firstName != null ? firstName : "";
        String l = lastName != null ? lastName : "";
        return (f + " " + l).trim();
    }
}
