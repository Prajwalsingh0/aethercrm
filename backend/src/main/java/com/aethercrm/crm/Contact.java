package com.aethercrm.crm;

import com.aethercrm.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact extends TenantAwareEntity {

    @Column(name = "account_id")
    private UUID accountId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 50)
    private String mobile;

    @Column(length = 150)
    private String title;

    @Column(length = 100)
    private String department;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(length = 100)
    private String source;

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

    public String getFullName() {
        return (firstName + " " + lastName).trim();
    }
}
