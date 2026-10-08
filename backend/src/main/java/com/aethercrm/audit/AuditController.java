package com.aethercrm.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Map<String, Object>> page = auditService.list(pageable);
        return ResponseEntity.ok(Map.of(
                "data", page.getContent(),
                "totalElements", page.getTotalElements()
        ));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> recent() {
        return ResponseEntity.ok(auditService.recent());
    }

    @GetMapping("/entity/{type}/{id}")
    public ResponseEntity<List<Map<String, Object>>> forEntity(
            @PathVariable String type, @PathVariable UUID id) {
        return ResponseEntity.ok(auditService.forEntity(type, id));
    }
}
