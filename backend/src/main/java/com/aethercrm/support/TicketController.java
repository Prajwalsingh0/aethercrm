package com.aethercrm.support;

import com.aethercrm.support.dto.CommentRequest;
import com.aethercrm.support.dto.TicketRequest;
import com.aethercrm.support.dto.TicketResponse;
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
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TicketResponse> page = ticketService.search(status, priority, assigneeId, q, pageable);
        return ResponseEntity.ok(Map.of(
                "data", page.getContent(),
                "page", page.getNumber(),
                "size", page.getSize(),
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.get(id));
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody TicketRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> update(@PathVariable UUID id, @Valid @RequestBody TicketRequest req) {
        return ResponseEntity.ok(ticketService.update(id, req));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<TicketResponse.CommentResponse> comment(
            @PathVariable UUID id, @Valid @RequestBody CommentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.addComment(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ticketService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
