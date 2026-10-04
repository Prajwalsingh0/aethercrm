package com.aethercrm.email;

import com.aethercrm.activity.Activity;
import com.aethercrm.activity.ActivityRepository;
import com.aethercrm.tenant.TenantContext;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
public class EmailService {

    private final EmailMessageRepository emailRepository;
    private final ActivityRepository activityRepository;
    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${aethercrm.email.from:noreply@aethercrm.local}")
    private String defaultFrom;

    @Value("${aethercrm.email.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.host:}")
    private String mailHost;

    public EmailService(EmailMessageRepository emailRepository,
                        ActivityRepository activityRepository,
                        ObjectProvider<JavaMailSender> mailSender) {
        this.emailRepository = emailRepository;
        this.activityRepository = activityRepository;
        this.mailSender = mailSender;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> search(String status, String relatedType, UUID relatedId, String q, Pageable pageable) {
        UUID orgId = TenantContext.requireOrganizationId();
        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        return emailRepository.search(orgId, status, relatedType, relatedId, query, pageable).map(this::toMap);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(UUID id) {
        return toMap(findOwned(id));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> status() {
        boolean configured = mailEnabled && mailHost != null && !mailHost.isBlank() && mailSender.getIfAvailable() != null;
        Map<String, Object> m = new HashMap<>();
        m.put("enabled", mailEnabled);
        m.put("configured", configured);
        m.put("from", defaultFrom);
        m.put("host", mailHost != null && !mailHost.isBlank() ? mailHost : null);
        m.put("mode", configured ? "SMTP" : "LOG_ONLY");
        return m;
    }

    @Transactional
    public Map<String, Object> send(Map<String, Object> body) {
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();

        String to = required(body, "to");
        String subject = required(body, "subject");
        String text = body.get("body") != null ? body.get("body").toString() : "";
        String html = body.get("bodyHtml") != null ? body.get("bodyHtml").toString() : null;
        String from = body.get("from") != null ? body.get("from").toString() : defaultFrom;
        String cc = body.get("cc") != null ? body.get("cc").toString() : null;
        String relatedType = body.get("relatedType") != null ? body.get("relatedType").toString().toUpperCase() : null;
        UUID relatedId = body.get("relatedId") != null ? UUID.fromString(body.get("relatedId").toString()) : null;

        EmailMessage msg = EmailMessage.builder()
                .organizationId(orgId)
                .direction("OUTBOUND")
                .status("QUEUED")
                .fromAddress(from)
                .toAddresses(to)
                .ccAddresses(cc)
                .subject(subject.trim())
                .bodyText(text)
                .bodyHtml(html)
                .relatedType(relatedType)
                .relatedId(relatedId)
                .createdBy(userId)
                .build();

        boolean smtpReady = mailEnabled && mailHost != null && !mailHost.isBlank() && mailSender.getIfAvailable() != null;

        if (smtpReady) {
            try {
                sendViaSmtp(msg);
                msg.setStatus("SENT");
                msg.setProvider("SMTP");
                msg.setSentAt(Instant.now());
            } catch (Exception ex) {
                msg.setStatus("FAILED");
                msg.setProvider("SMTP");
                msg.setErrorMessage(truncate(ex.getMessage(), 1000));
            }
        } else {
            msg.setStatus("LOGGED");
            msg.setProvider("LOG_ONLY");
            msg.setSentAt(Instant.now());
        }

        msg = emailRepository.save(msg);
        createEmailActivity(msg, userId, orgId);
        return toMap(msg);
    }

    private void sendViaSmtp(EmailMessage msg) throws Exception {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) throw new IllegalStateException("JavaMailSender not available");
        MimeMessage mime = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
        helper.setFrom(msg.getFromAddress());
        helper.setTo(splitAddresses(msg.getToAddresses()));
        if (msg.getCcAddresses() != null && !msg.getCcAddresses().isBlank()) {
            helper.setCc(splitAddresses(msg.getCcAddresses()));
        }
        helper.setSubject(msg.getSubject());
        if (msg.getBodyHtml() != null && !msg.getBodyHtml().isBlank()) {
            helper.setText(msg.getBodyText() != null ? msg.getBodyText() : "", msg.getBodyHtml());
        } else {
            helper.setText(msg.getBodyText() != null ? msg.getBodyText() : "", false);
        }
        sender.send(mime);
    }

    private void createEmailActivity(EmailMessage msg, UUID userId, UUID orgId) {
        try {
            Activity a = Activity.builder()
                    .type("EMAIL")
                    .subject("Email: " + msg.getSubject())
                    .description("To: " + msg.getToAddresses() + "\nStatus: " + msg.getStatus() + "\n\n" +
                            (msg.getBodyText() != null ? msg.getBodyText() : ""))
                    .status("COMPLETED")
                    .priority("MEDIUM")
                    .ownerId(userId)
                    .relatedType(msg.getRelatedType())
                    .relatedId(msg.getRelatedId())
                    .completedAt(Instant.now())
                    .createdBy(userId)
                    .updatedBy(userId)
                    .build();
            a.setOrganizationId(orgId);
            activityRepository.save(a);
        } catch (Exception ignored) {
        }
    }

    private EmailMessage findOwned(UUID id) {
        return emailRepository.findByIdAndOrganizationId(id, TenantContext.requireOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found"));
    }

    private Map<String, Object> toMap(EmailMessage e) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", e.getId());
        m.put("direction", e.getDirection());
        m.put("status", e.getStatus());
        m.put("from", e.getFromAddress());
        m.put("to", e.getToAddresses());
        m.put("cc", e.getCcAddresses());
        m.put("subject", e.getSubject());
        m.put("body", e.getBodyText());
        m.put("bodyHtml", e.getBodyHtml());
        m.put("relatedType", e.getRelatedType());
        m.put("relatedId", e.getRelatedId());
        m.put("provider", e.getProvider());
        m.put("errorMessage", e.getErrorMessage());
        m.put("sentAt", e.getSentAt());
        m.put("createdAt", e.getCreatedAt());
        m.put("createdBy", e.getCreatedBy());
        return m;
    }

    private static String required(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null || v.toString().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " is required");
        }
        return v.toString().trim();
    }

    private static String[] splitAddresses(String raw) {
        return Arrays.stream(raw.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
