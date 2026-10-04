package com.aethercrm.email;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(emailService.status());
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String relatedType,
            @RequestParam(required = false) UUID relatedId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Map<String, Object>> page = emailService.search(status, relatedType, relatedId, q, pageable);
        return ResponseEntity.ok(Map.of(
                "data", page.getContent(),
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(emailService.get(id));
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> send(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emailService.send(body));
    }
}
