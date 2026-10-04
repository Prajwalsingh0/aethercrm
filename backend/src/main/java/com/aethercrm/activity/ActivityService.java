package com.aethercrm.activity;

import com.aethercrm.activity.dto.ActivityRequest;
import com.aethercrm.activity.dto.ActivityResponse;
import com.aethercrm.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> search(String status, String type, UUID ownerId,
                                         String relatedType, UUID relatedId, String q, Pageable pageable) {
        UUID orgId = TenantContext.requireOrganizationId();
        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        return activityRepository.search(orgId, status, type, ownerId, relatedType, relatedId, query, pageable)
                .map(ActivityResponse::from);
    }

    @Transactional(readOnly = true)
    public ActivityResponse get(UUID id) {
        return ActivityResponse.from(findOwned(id));
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> recent() {
        UUID orgId = TenantContext.requireOrganizationId();
        return activityRepository.findTop20ByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(orgId)
                .stream().map(ActivityResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public ActivityResponse create(ActivityRequest req) {
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();

        Activity a = Activity.builder()
                .type(req.getType().toUpperCase())
                .subject(req.getSubject().trim())
                .description(req.getDescription())
                .status(req.getStatus() != null ? req.getStatus().toUpperCase() : "OPEN")
                .priority(req.getPriority() != null ? req.getPriority().toUpperCase() : "MEDIUM")
                .dueAt(req.getDueAt())
                .ownerId(req.getOwnerId() != null ? req.getOwnerId() : userId)
                .relatedType(req.getRelatedType() != null ? req.getRelatedType().toUpperCase() : null)
                .relatedId(req.getRelatedId())
                .createdBy(userId)
                .updatedBy(userId)
                .build();
        a.setOrganizationId(orgId);
        return ActivityResponse.from(activityRepository.save(a));
    }

    @Transactional
    public ActivityResponse update(UUID id, ActivityRequest req) {
        Activity a = findOwned(id);
        UUID userId = TenantContext.requireUserId();
        if (req.getType() != null) a.setType(req.getType().toUpperCase());
        if (req.getSubject() != null) a.setSubject(req.getSubject().trim());
        if (req.getDescription() != null) a.setDescription(req.getDescription());
        if (req.getStatus() != null) {
            a.setStatus(req.getStatus().toUpperCase());
            if ("COMPLETED".equalsIgnoreCase(req.getStatus()) && a.getCompletedAt() == null) {
                a.setCompletedAt(Instant.now());
            }
        }
        if (req.getPriority() != null) a.setPriority(req.getPriority().toUpperCase());
        if (req.getDueAt() != null) a.setDueAt(req.getDueAt());
        if (req.getOwnerId() != null) a.setOwnerId(req.getOwnerId());
        if (req.getRelatedType() != null) a.setRelatedType(req.getRelatedType().toUpperCase());
        if (req.getRelatedId() != null) a.setRelatedId(req.getRelatedId());
        a.setUpdatedBy(userId);
        return ActivityResponse.from(activityRepository.save(a));
    }

    @Transactional
    public ActivityResponse complete(UUID id) {
        Activity a = findOwned(id);
        a.setStatus("COMPLETED");
        a.setCompletedAt(Instant.now());
        a.setUpdatedBy(TenantContext.requireUserId());
        return ActivityResponse.from(activityRepository.save(a));
    }

    @Transactional
    public void softDelete(UUID id) {
        Activity a = findOwned(id);
        a.setDeletedAt(Instant.now());
        a.setUpdatedBy(TenantContext.requireUserId());
        activityRepository.save(a);
    }

    private Activity findOwned(UUID id) {
        return activityRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, TenantContext.requireOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
    }
}
