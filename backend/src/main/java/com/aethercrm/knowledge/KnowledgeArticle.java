package com.aethercrm.knowledge;

import com.aethercrm.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_articles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KnowledgeArticle extends TenantAwareEntity {
    @Column(nullable = false, length = 500) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @Column(nullable = false, length = 50) @Builder.Default private String status = "DRAFT";
    @Column(length = 100) private String category;
    @Column(name = "tags_json", columnDefinition = "TEXT") private String tagsJson;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "published_at") private Instant publishedAt;
}
