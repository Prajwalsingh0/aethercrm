package com.aethercrm.activity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    @Query("SELECT a FROM Activity a WHERE a.organizationId = :orgId AND a.deletedAt IS NULL " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:type IS NULL OR a.type = :type) " +
           "AND (:ownerId IS NULL OR a.ownerId = :ownerId) " +
           "AND (:relatedType IS NULL OR a.relatedType = :relatedType) " +
           "AND (:relatedId IS NULL OR a.relatedId = :relatedId) " +
           "AND (:q IS NULL OR LOWER(a.subject) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Activity> search(@Param("orgId") UUID orgId,
                          @Param("status") String status,
                          @Param("type") String type,
                          @Param("ownerId") UUID ownerId,
                          @Param("relatedType") String relatedType,
                          @Param("relatedId") UUID relatedId,
                          @Param("q") String q,
                          Pageable pageable);

    Optional<Activity> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    List<Activity> findTop20ByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID organizationId);

    long countByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, String status);
}
