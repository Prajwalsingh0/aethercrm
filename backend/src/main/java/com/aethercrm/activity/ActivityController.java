package com.aethercrm.activity;

import com.aethercrm.activity.dto.ActivityRequest;
import com.aethercrm.activity.dto.ActivityResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) String relatedType,
            @RequestParam(required = false) UUID relatedId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ActivityResponse> page = activityService.search(status, type, ownerId, relatedType, relatedId, q, pageable);
        return ResponseEntity.ok(Map.of(
                "data", page.getContent(),
                "page", page.getNumber(),
                "size", page.getSize(),
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages()
        ));
    }

    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> recent() {
        List<ActivityResponse> list = activityService.recent();
        return ResponseEntity.ok(Map.of("data", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(activityService.get(id));
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> create(@Valid @RequestBody ActivityRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> update(@PathVariable UUID id, @Valid @RequestBody ActivityRequest req) {
        return ResponseEntity.ok(activityService.update(id, req));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ActivityResponse> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(activityService.complete(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        activityService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
