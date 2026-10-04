package com.aethercrm.workflow;

import com.aethercrm.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WorkflowService {
    private final WorkflowRepository repo;

    public WorkflowService(WorkflowRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list() {
        return repo.findByOrganizationIdOrderByCreatedAtDesc(TenantContext.requireOrganizationId())
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        UUID orgId = TenantContext.requireOrganizationId();
        WorkflowDefinition w = WorkflowDefinition.builder()
                .name(body.get("name").toString())
                .description(body.get("description") != null ? body.get("description").toString() : null)
                .isActive(body.get("isActive") == null || Boolean.TRUE.equals(body.get("isActive")))
                .triggerType(body.get("triggerType") != null ? body.get("triggerType").toString() : "ENTITY_EVENT")
                .triggerConfigJson(body.get("triggerConfigJson") != null ? body.get("triggerConfigJson").toString() : "{}")
                .conditionsJson(body.get("conditionsJson") != null ? body.get("conditionsJson").toString() : null)
                .actionsJson(body.get("actionsJson") != null ? body.get("actionsJson").toString() : "[]")
                .createdBy(TenantContext.requireUserId())
                .build();
        w.setOrganizationId(orgId);
        return toMap(repo.save(w));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> body) {
        WorkflowDefinition w = repo.findByIdAndOrganizationId(id, TenantContext.requireOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));
        if (body.containsKey("name")) w.setName(body.get("name").toString());
        if (body.containsKey("description")) w.setDescription(body.get("description") != null ? body.get("description").toString() : null);
        if (body.containsKey("isActive")) w.setActive(Boolean.TRUE.equals(body.get("isActive")));
        if (body.containsKey("triggerType")) w.setTriggerType(body.get("triggerType").toString());
        if (body.containsKey("triggerConfigJson")) w.setTriggerConfigJson(body.get("triggerConfigJson").toString());
        if (body.containsKey("actionsJson")) w.setActionsJson(body.get("actionsJson").toString());
        return toMap(repo.save(w));
    }

    @Transactional
    public void delete(UUID id) {
        WorkflowDefinition w = repo.findByIdAndOrganizationId(id, TenantContext.requireOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));
        repo.delete(w);
    }

    private Map<String, Object> toMap(WorkflowDefinition w) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", w.getId());
        m.put("name", w.getName());
        m.put("description", w.getDescription());
        m.put("isActive", w.isActive());
        m.put("triggerType", w.getTriggerType());
        m.put("triggerConfigJson", w.getTriggerConfigJson());
        m.put("conditionsJson", w.getConditionsJson());
        m.put("actionsJson", w.getActionsJson());
        m.put("createdAt", w.getCreatedAt());
        return m;
    }
}
