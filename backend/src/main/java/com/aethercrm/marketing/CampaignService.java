package com.aethercrm.marketing;

import com.aethercrm.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CampaignService {
    private final CampaignRepository repo;

    public CampaignService(CampaignRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> search(String status, String q, Pageable pageable) {
        UUID orgId = TenantContext.requireOrganizationId();
        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        return repo.search(orgId, status, query, pageable).map(this::toMap);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(UUID id) { return toMap(findOwned(id)); }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();
        Campaign c = Campaign.builder()
                .name(str(body, "name"))
                .description(str(body, "description"))
                .type(str(body, "type") != null ? str(body, "type").toUpperCase() : "EMAIL")
                .status(str(body, "status") != null ? str(body, "status").toUpperCase() : "DRAFT")
                .budget(dec(body, "budget"))
                .currency(str(body, "currency") != null ? str(body, "currency") : "INR")
                .startDate(date(body, "startDate"))
                .endDate(date(body, "endDate"))
                .targetAudience(str(body, "targetAudience"))
                .ownerId(userId)
                .createdBy(userId)
                .updatedBy(userId)
                .build();
        c.setOrganizationId(orgId);
        return toMap(repo.save(c));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> body) {
        Campaign c = findOwned(id);
        if (body.containsKey("name")) c.setName(str(body, "name"));
        if (body.containsKey("description")) c.setDescription(str(body, "description"));
        if (body.containsKey("type")) c.setType(str(body, "type").toUpperCase());
        if (body.containsKey("status")) c.setStatus(str(body, "status").toUpperCase());
        if (body.containsKey("budget")) c.setBudget(dec(body, "budget"));
        if (body.containsKey("startDate")) c.setStartDate(date(body, "startDate"));
        if (body.containsKey("endDate")) c.setEndDate(date(body, "endDate"));
        if (body.containsKey("targetAudience")) c.setTargetAudience(str(body, "targetAudience"));
        c.setUpdatedBy(TenantContext.requireUserId());
        return toMap(repo.save(c));
    }

    @Transactional
    public void softDelete(UUID id) {
        Campaign c = findOwned(id);
        c.setDeletedAt(Instant.now());
        repo.save(c);
    }

    private Campaign findOwned(UUID id) {
        return repo.findByIdAndOrganizationIdAndDeletedAtIsNull(id, TenantContext.requireOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found"));
    }

    private Map<String, Object> toMap(Campaign c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("name", c.getName());
        m.put("description", c.getDescription());
        m.put("type", c.getType());
        m.put("status", c.getStatus());
        m.put("budget", c.getBudget());
        m.put("currency", c.getCurrency());
        m.put("startDate", c.getStartDate());
        m.put("endDate", c.getEndDate());
        m.put("targetAudience", c.getTargetAudience());
        m.put("ownerId", c.getOwnerId());
        m.put("createdAt", c.getCreatedAt());
        return m;
    }

    private static String str(Map<String, Object> b, String k) {
        Object v = b.get(k); return v == null ? null : v.toString();
    }
    private static BigDecimal dec(Map<String, Object> b, String k) {
        Object v = b.get(k); if (v == null) return null;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return null; }
    }
    private static LocalDate date(Map<String, Object> b, String k) {
        Object v = b.get(k); if (v == null || v.toString().isBlank()) return null;
        try { return LocalDate.parse(v.toString().substring(0, Math.min(10, v.toString().length()))); }
        catch (Exception e) { return null; }
    }
}
