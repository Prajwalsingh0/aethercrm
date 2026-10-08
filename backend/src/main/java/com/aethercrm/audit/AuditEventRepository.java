package com.aethercrm.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    Page<AuditEvent> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);

    List<AuditEvent> findTop30ByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    List<AuditEvent> findTop20ByOrganizationIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
            UUID organizationId, String entityType, UUID entityId);
}
