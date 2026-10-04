package com.aethercrm.notification;

import com.aethercrm.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> list(Pageable pageable) {
        UUID userId = TenantContext.requireUserId();
        UUID orgId = TenantContext.requireOrganizationId();
        return notificationRepository.findByUserIdAndOrganizationIdOrderByCreatedAtDesc(userId, orgId, pageable)
                .map(this::toMap);
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return notificationRepository.countByUserIdAndOrganizationIdAndIsReadFalse(
                TenantContext.requireUserId(), TenantContext.requireOrganizationId());
    }

    @Transactional
    public Map<String, Object> markRead(UUID id) {
        Notification n = notificationRepository.findByIdAndUserId(id, TenantContext.requireUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        n.setRead(true);
        return toMap(notificationRepository.save(n));
    }

    @Transactional
    public int markAllRead() {
        return notificationRepository.markAllRead(
                TenantContext.requireUserId(), TenantContext.requireOrganizationId());
    }

    @Transactional
    public void notify(UUID userId, String type, String title, String body, String link) {
        Notification n = Notification.builder()
                .organizationId(TenantContext.requireOrganizationId())
                .userId(userId)
                .type(type)
                .title(title)
                .body(body)
                .link(link)
                .isRead(false)
                .build();
        notificationRepository.save(n);
    }

    private Map<String, Object> toMap(Notification n) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", n.getId());
        m.put("type", n.getType());
        m.put("title", n.getTitle());
        m.put("body", n.getBody());
        m.put("link", n.getLink());
        m.put("isRead", n.isRead());
        m.put("createdAt", n.getCreatedAt());
        return m;
    }
}
