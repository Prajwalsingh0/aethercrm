package com.aethercrm.product;

import com.aethercrm.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> search(boolean activeOnly, String q, Pageable pageable) {
        UUID orgId = TenantContext.requireOrganizationId();
        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        return repo.search(orgId, activeOnly, query, pageable).map(this::toMap);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(UUID id) {
        return toMap(findOwned(id));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        UUID orgId = TenantContext.requireOrganizationId();
        Product p = Product.builder()
                .name(str(body, "name"))
                .sku(str(body, "sku"))
                .description(str(body, "description"))
                .unitPrice(dec(body, "unitPrice", BigDecimal.ZERO))
                .currency(str(body, "currency") != null ? str(body, "currency") : "INR")
                .isActive(body.get("isActive") == null || Boolean.TRUE.equals(body.get("isActive")))
                .category(str(body, "category"))
                .build();
        p.setOrganizationId(orgId);
        return toMap(repo.save(p));
    }

    @Transactional
    public Map<String, Object> update(UUID id, Map<String, Object> body) {
        Product p = findOwned(id);
        if (body.containsKey("name")) p.setName(str(body, "name"));
        if (body.containsKey("sku")) p.setSku(str(body, "sku"));
        if (body.containsKey("description")) p.setDescription(str(body, "description"));
        if (body.containsKey("unitPrice")) p.setUnitPrice(dec(body, "unitPrice", p.getUnitPrice()));
        if (body.containsKey("currency")) p.setCurrency(str(body, "currency"));
        if (body.containsKey("isActive")) p.setActive(Boolean.TRUE.equals(body.get("isActive")));
        if (body.containsKey("category")) p.setCategory(str(body, "category"));
        return toMap(repo.save(p));
    }

    @Transactional
    public void softDelete(UUID id) {
        Product p = findOwned(id);
        p.setDeletedAt(Instant.now());
        repo.save(p);
    }

    private Product findOwned(UUID id) {
        return repo.findByIdAndOrganizationIdAndDeletedAtIsNull(id, TenantContext.requireOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private Map<String, Object> toMap(Product p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("sku", p.getSku());
        m.put("description", p.getDescription());
        m.put("unitPrice", p.getUnitPrice());
        m.put("currency", p.getCurrency());
        m.put("isActive", p.isActive());
        m.put("category", p.getCategory());
        m.put("createdAt", p.getCreatedAt());
        return m;
    }

    private static String str(Map<String, Object> b, String k) {
        Object v = b.get(k);
        return v == null ? null : v.toString();
    }

    private static BigDecimal dec(Map<String, Object> b, String k, BigDecimal def) {
        Object v = b.get(k);
        if (v == null) return def;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return def; }
    }
}
