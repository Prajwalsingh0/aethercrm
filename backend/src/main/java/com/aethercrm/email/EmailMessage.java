package com.aethercrm.email;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailMessage {

    @Id
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String direction = "OUTBOUND";

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "QUEUED";

    @Column(name = "from_address", nullable = false)
    private String fromAddress;

    @Column(name = "to_addresses", nullable = false, columnDefinition = "TEXT")
    private String toAddresses;

    @Column(name = "cc_addresses", columnDefinition = "TEXT")
    private String ccAddresses;

    @Column(name = "bcc_addresses", columnDefinition = "TEXT")
    private String bccAddresses;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "body_html", columnDefinition = "TEXT")
    private String bodyHtml;

    @Column(name = "related_type", length = 50)
    private String relatedType;

    @Column(name = "related_id")
    private UUID relatedId;

    @Column(length = 50)
    private String provider;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }
}
