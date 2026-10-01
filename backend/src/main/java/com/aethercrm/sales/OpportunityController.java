package com.aethercrm.sales;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class OpportunityController {

    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping("/pipeline/stages")
    public ResponseEntity<List<Map<String, Object>>> stages() {
        return ResponseEntity.ok(opportunityService.listStages());
    }

    @GetMapping("/pipeline/summary")
    public ResponseEntity<Map<String, Object>> pipelineSummary() {
        return ResponseEntity.ok(opportunityService.pipelineSummary());
    }

    @GetMapping("/opportunities")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID stageId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Map<String, Object>> page = opportunityService.search(status, stageId, q, pageable);
        return ResponseEntity.ok(Map.of("data", page.getContent(),
                "meta", Map.of("page", page.getNumber(), "size", page.getSize(),
                        "totalElements", page.getTotalElements(), "totalPages", page.getTotalPages())));
    }

    @GetMapping("/opportunities/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(opportunityService.get(id));
    }

    @PostMapping("/opportunities")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityService.create(body));
    }

    @PostMapping("/opportunities/{id}/move")
    public ResponseEntity<Map<String, Object>> move(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        UUID stageId = UUID.fromString(body.get("stageId").toString());
        String note = body.get("note") != null ? body.get("note").toString() : null;
        return ResponseEntity.ok(opportunityService.moveStage(id, stageId, note));
    }

    @DeleteMapping("/opportunities/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        opportunityService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
