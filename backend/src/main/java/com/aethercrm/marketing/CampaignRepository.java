package com.aethercrm.marketing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    @Query("SELECT c FROM Campaign c WHERE c.organizationId = :orgId AND c.deletedAt IS NULL " +
           "AND (:status IS NULL OR c.status = :status) " +
           "AND (:q IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Campaign> search(@Param("orgId") UUID orgId, @Param("status") String status, @Param("q") String q, Pageable pageable);

    Optional<Campaign> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);
    long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);
}
