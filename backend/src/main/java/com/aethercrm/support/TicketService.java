package com.aethercrm.support;

import com.aethercrm.support.dto.CommentRequest;
import com.aethercrm.support.dto.TicketRequest;
import com.aethercrm.support.dto.TicketResponse;
import com.aethercrm.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    public TicketService(TicketRepository ticketRepository, TicketCommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> search(String status, String priority, UUID assigneeId, String q, Pageable pageable) {
        UUID orgId = TenantContext.requireOrganizationId();
        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        return ticketRepository.search(orgId, status, priority, assigneeId, query, pageable)
                .map(TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public TicketResponse get(UUID id) {
        Ticket t = findOwned(id);
        TicketResponse resp = TicketResponse.from(t);
        List<TicketResponse.CommentResponse> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(id)
                .stream()
                .map(c -> TicketResponse.CommentResponse.builder()
                        .id(c.getId())
                        .authorId(c.getAuthorId())
                        .body(c.getBody())
                        .isInternal(c.isInternal())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        resp.setComments(comments);
        return resp;
    }

    @Transactional
    public TicketResponse create(TicketRequest req) {
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();
        String number = "TCK-" + Year.now().getValue() + "-" + String.format("%05d", SEQ.incrementAndGet() % 100000);

        Ticket t = Ticket.builder()
                .ticketNumber(number)
                .subject(req.getSubject().trim())
                .description(req.getDescription())
                .status(req.getStatus() != null ? req.getStatus().toUpperCase() : "OPEN")
                .priority(req.getPriority() != null ? req.getPriority().toUpperCase() : "MEDIUM")
                .category(req.getCategory())
                .contactId(req.getContactId())
                .accountId(req.getAccountId())
                .assigneeId(req.getAssigneeId() != null ? req.getAssigneeId() : userId)
                .createdBy(userId)
                .updatedBy(userId)
                .build();
        t.setOrganizationId(orgId);
        return TicketResponse.from(ticketRepository.save(t));
    }

    @Transactional
    public TicketResponse update(UUID id, TicketRequest req) {
        Ticket t = findOwned(id);
        UUID userId = TenantContext.requireUserId();
        if (req.getSubject() != null) t.setSubject(req.getSubject().trim());
        if (req.getDescription() != null) t.setDescription(req.getDescription());
        if (req.getStatus() != null) {
            String st = req.getStatus().toUpperCase();
            t.setStatus(st);
            if (("RESOLVED".equals(st) || "CLOSED".equals(st)) && t.getResolvedAt() == null) {
                t.setResolvedAt(Instant.now());
            }
        }
        if (req.getPriority() != null) t.setPriority(req.getPriority().toUpperCase());
        if (req.getCategory() != null) t.setCategory(req.getCategory());
        if (req.getContactId() != null) t.setContactId(req.getContactId());
        if (req.getAccountId() != null) t.setAccountId(req.getAccountId());
        if (req.getAssigneeId() != null) t.setAssigneeId(req.getAssigneeId());
        t.setUpdatedBy(userId);
        return TicketResponse.from(ticketRepository.save(t));
    }

    @Transactional
    public TicketResponse.CommentResponse addComment(UUID ticketId, CommentRequest req) {
        findOwned(ticketId);
        UUID userId = TenantContext.requireUserId();
        TicketComment c = TicketComment.builder()
                .ticketId(ticketId)
                .authorId(userId)
                .body(req.getBody().trim())
                .isInternal(req.isInternal())
                .build();
        c = commentRepository.save(c);
        return TicketResponse.CommentResponse.builder()
                .id(c.getId())
                .authorId(c.getAuthorId())
                .body(c.getBody())
                .isInternal(c.isInternal())
                .createdAt(c.getCreatedAt())
                .build();
    }

    @Transactional
    public void softDelete(UUID id) {
        Ticket t = findOwned(id);
        t.setDeletedAt(Instant.now());
        t.setUpdatedBy(TenantContext.requireUserId());
        ticketRepository.save(t);
    }

    private Ticket findOwned(UUID id) {
        return ticketRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, TenantContext.requireOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }
}
