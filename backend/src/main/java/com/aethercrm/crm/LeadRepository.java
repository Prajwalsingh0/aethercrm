package com.aethercrm.crm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    @Query("SELECT l FROM Lead l WHERE l.organizationId = :orgId AND l.deletedAt IS NULL " +
           "AND (:status IS NULL OR l.status = :status) " +
           "AND (:ownerId IS NULL OR l.ownerId = :ownerId) " +
           "AND (:q IS NULL OR LOWER(l.firstName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(l.lastName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(l.email) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(l.company) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Lead> search(@Param("orgId") UUID orgId,
                      @Param("status") String status,
                      @Param("ownerId") UUID ownerId,
                      @Param("q") String q,
                      Pageable pageable);

    Optional<Lead> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);

    long countByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, String status);

    List<Lead> findByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, String status);

    List<Lead> findByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID organizationId);
}
