package com.aethercrm.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Query("SELECT t FROM Ticket t WHERE t.organizationId = :orgId AND t.deletedAt IS NULL " +
           "AND (:status IS NULL OR t.status = :status) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:assigneeId IS NULL OR t.assigneeId = :assigneeId) " +
           "AND (:q IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Ticket> search(@Param("orgId") UUID orgId,
                        @Param("status") String status,
                        @Param("priority") String priority,
                        @Param("assigneeId") UUID assigneeId,
                        @Param("q") String q,
                        Pageable pageable);

    Optional<Ticket> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);

    long countByOrganizationIdAndStatusAndDeletedAtIsNull(UUID organizationId, String status);
}
