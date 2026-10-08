package com.aethercrm.audit;

import com.aethercrm.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AuditService {

    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(String action, String entityType, UUID entityId, String summary, String detailsJson) {
        UUID orgId = TenantContext.getOrganizationId();
        if (orgId == null) return;
        UUID actorId = TenantContext.getUserId();
        AuditEvent e = AuditEvent.builder()
                .organizationId(orgId)
                .actorId(actorId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .summary(summary)
                .detailsJson(detailsJson)
                .build();
        repository.save(e);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> list(Pageable pageable) {
        UUID orgId = TenantContext.requireOrganizationId();
        return repository.findByOrganizationIdOrderByCreatedAtDesc(orgId, pageable).map(this::toMap);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> recent() {
        UUID orgId = TenantContext.requireOrganizationId();
        List<Map<String, Object>> out = new ArrayList<>();
        for (AuditEvent e : repository.findTop30ByOrganizationIdOrderByCreatedAtDesc(orgId)) {
            out.add(toMap(e));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> forEntity(String entityType, UUID entityId) {
        UUID orgId = TenantContext.requireOrganizationId();
        List<Map<String, Object>> out = new ArrayList<>();
        for (AuditEvent e : repository.findTop20ByOrganizationIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                orgId, entityType.toUpperCase(), entityId)) {
            out.add(toMap(e));
        }
        return out;
    }

    private Map<String, Object> toMap(AuditEvent e) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", e.getId());
        m.put("action", e.getAction());
        m.put("entityType", e.getEntityType());
        m.put("entityId", e.getEntityId());
        m.put("summary", e.getSummary());
        m.put("detailsJson", e.getDetailsJson());
        m.put("actorId", e.getActorId());
        m.put("createdAt", e.getCreatedAt());
        return m;
    }
}
