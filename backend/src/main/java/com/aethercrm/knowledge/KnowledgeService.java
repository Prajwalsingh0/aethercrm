package com.aethercrm.knowledge;

import com.aethercrm.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class KnowledgeService {
    private final KnowledgeArticleRepository repo;

    public KnowledgeService(KnowledgeArticleRepository repo) { this.repo = repo; }

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
        String status = body.get("status") != null ? body.get("status").toString().toUpperCase() : "DRAFT";
        KnowledgeArticle a = KnowledgeArticle.builder()
                .title(body.get("title").toString())
                .body(body.get("body") != null ? body.get("body").toString() : "")
                .status(status)
                .category(body.get("category") != null ? body.get("category").toString() : null)
                .createdBy(userId)
                .publishedAt("PUBLISHED".equals(status) ? Instant.now() : null)
                .build();
        a.setOrganizationId(orgId);
        return toMap(repo.save(a));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> body) {
        KnowledgeArticle a = findOwned(id);
        if (body.containsKey("title")) a.setTitle(body.get("title").toString());
        if (body.containsKey("body")) a.setBody(body.get("body").toString());
        if (body.containsKey("category")) a.setCategory(body.get("category") != null ? body.get("category").toString() : null);
        if (body.containsKey("status")) {
            String st = body.get("status").toString().toUpperCase();
            a.setStatus(st);
            if ("PUBLISHED".equals(st) && a.getPublishedAt() == null) a.setPublishedAt(Instant.now());
        }
        return toMap(repo.save(a));
    }

    @Transactional
    public void delete(UUID id) {
        repo.delete(findOwned(id));
    }

    private KnowledgeArticle findOwned(UUID id) {
        return repo.findByIdAndOrganizationId(id, TenantContext.requireOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
    }

    private Map<String, Object> toMap(KnowledgeArticle a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("title", a.getTitle());
        m.put("body", a.getBody());
        m.put("status", a.getStatus());
        m.put("category", a.getCategory());
        m.put("publishedAt", a.getPublishedAt());
        m.put("createdAt", a.getCreatedAt());
        m.put("updatedAt", a.getUpdatedAt());
        return m;
    }
}
