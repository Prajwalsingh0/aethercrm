package com.aethercrm.email;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailMessageRepository extends JpaRepository<EmailMessage, UUID> {

    @Query("SELECT e FROM EmailMessage e WHERE e.organizationId = :orgId " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:relatedType IS NULL OR e.relatedType = :relatedType) " +
           "AND (:relatedId IS NULL OR e.relatedId = :relatedId) " +
           "AND (:q IS NULL OR LOWER(e.subject) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(e.toAddresses) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<EmailMessage> search(@Param("orgId") UUID orgId,
                              @Param("status") String status,
                              @Param("relatedType") String relatedType,
                              @Param("relatedId") UUID relatedId,
                              @Param("q") String q,
                              Pageable pageable);

    Optional<EmailMessage> findByIdAndOrganizationId(UUID id, UUID organizationId);

    List<EmailMessage> findTop20ByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
