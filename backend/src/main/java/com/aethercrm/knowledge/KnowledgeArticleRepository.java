package com.aethercrm.knowledge;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, UUID> {
    @Query("SELECT a FROM KnowledgeArticle a WHERE a.organizationId = :orgId " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:q IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(a.body) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<KnowledgeArticle> search(@Param("orgId") UUID orgId, @Param("status") String status, @Param("q") String q, Pageable pageable);

    Optional<KnowledgeArticle> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
