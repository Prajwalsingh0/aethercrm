package com.aethercrm.workflow;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<WorkflowDefinition, UUID> {
    List<WorkflowDefinition> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
    Optional<WorkflowDefinition> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
