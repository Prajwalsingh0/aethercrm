package com.aethercrm.crm;

import com.aethercrm.crm.dto.ContactRequest;
import com.aethercrm.crm.dto.ContactResponse;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ContactResponse> page = contactService.search(accountId, q, pageable);
        return ResponseEntity.ok(Map.of("data", page.getContent(),
                "meta", Map.of("page", page.getNumber(), "size", page.getSize(),
                        "totalElements", page.getTotalElements(), "totalPages", page.getTotalPages())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(contactService.get(id));
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(@Valid @RequestBody ContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> update(@PathVariable UUID id, @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(contactService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        contactService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
