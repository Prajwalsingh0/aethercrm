package com.aethercrm.sales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {

    @Query("SELECT o FROM Opportunity o WHERE o.organizationId = :orgId AND o.deletedAt IS NULL " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:stageId IS NULL OR o.stageId = :stageId) " +
           "AND (:q IS NULL OR LOWER(o.name) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Opportunity> search(@Param("orgId") UUID orgId,
                             @Param("status") String status,
                             @Param("stageId") UUID stageId,
                             @Param("q") String q,
                             Pageable pageable);

    List<Opportunity> findByOrganizationIdAndDeletedAtIsNullAndStatus(UUID organizationId, String status);

    Optional<Opportunity> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    long countByOrganizationIdAndDeletedAtIsNullAndStatus(UUID organizationId, String status);
}
