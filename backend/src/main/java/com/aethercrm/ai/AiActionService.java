package com.aethercrm.ai;

import com.aethercrm.activity.Activity;
import com.aethercrm.activity.ActivityRepository;
import com.aethercrm.audit.AuditService;
import com.aethercrm.crm.Lead;
import com.aethercrm.crm.LeadRepository;
import com.aethercrm.crm.LeadService;
import com.aethercrm.crm.dto.LeadResponse;
import com.aethercrm.email.EmailService;
import com.aethercrm.tenant.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Permission-aware AI actions that mutate CRM state.
 * Differentiator: copilot that executes (with explicit action type), not only chats.
 */
@Service
public class AiActionService {

    private final LeadRepository leadRepository;
    private final LeadService leadService;
    private final ActivityRepository activityRepository;
    private final EmailService emailService;
    private final AuditService auditService;

    public AiActionService(LeadRepository leadRepository,
                           LeadService leadService,
                           ActivityRepository activityRepository,
                           EmailService emailService,
                           AuditService auditService) {
        this.leadRepository = leadRepository;
        this.leadService = leadService;
        this.activityRepository = activityRepository;
        this.emailService = emailService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> propose(String message) {
        List<Map<String, Object>> actions = new ArrayList<>();
        String lower = message == null ? "" : message.toLowerCase();
        UUID orgId = TenantContext.requireOrganizationId();

        if (lower.contains("score") || lower.contains("rank") || lower.contains("hot lead")) {
            actions.add(action("SCORE_TOP_LEADS", "Rescore top open leads with explainable factors", Map.of()));
        }
        if (lower.contains("task") || lower.contains("follow up") || lower.contains("follow-up")) {
            actions.add(action("CREATE_FOLLOW_UP_TASKS", "Create follow-up tasks for NEW leads without open tasks", Map.of("limit", 5)));
        }
        if (lower.contains("email") || lower.contains("draft")) {
            actions.add(action("DRAFT_LOG_EMAIL", "Log a draft outreach email against the newest NEW lead", Map.of()));
        }
        if (lower.contains("qualif")) {
            actions.add(action("QUALIFY_HIGH_SCORE", "Mark leads with score >= 70 as QUALIFIED", Map.of("minScore", 70)));
        }
        if (actions.isEmpty()) {
            long newLeads = leadRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "NEW");
            if (newLeads > 0) {
                actions.add(action("CREATE_FOLLOW_UP_TASKS", "Create follow-up tasks for NEW leads", Map.of("limit", 3)));
                actions.add(action("SCORE_TOP_LEADS", "Refresh lead scores", Map.of()));
            }
        }
        return actions;
    }

    @Transactional
    public Map<String, Object> execute(String actionType, Map<String, Object> params) {
        if (actionType == null || actionType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "actionType is required");
        }
        String type = actionType.toUpperCase().trim();
        return switch (type) {
            case "SCORE_TOP_LEADS", "SCORE_LEAD" -> scoreLeads(params);
            case "CREATE_FOLLOW_UP_TASKS", "CREATE_TASK" -> createFollowUpTasks(params);
            case "DRAFT_LOG_EMAIL", "LOG_EMAIL" -> draftLogEmail(params);
            case "QUALIFY_HIGH_SCORE", "QUALIFY_LEAD" -> qualifyHighScore(params);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown action: " + type);
        };
    }

    private Map<String, Object> scoreLeads(Map<String, Object> params) {
        UUID orgId = TenantContext.requireOrganizationId();
        List<Lead> leads = leadRepository.findByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(orgId);
        int limit = intParam(params, "limit", 20);
        int done = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        for (Lead lead : leads) {
            if ("CONVERTED".equals(lead.getStatus())) continue;
            LeadResponse r = leadService.rescore(lead.getId());
            results.add(Map.of("id", r.getId(), "name", r.getFullName() != null ? r.getFullName() : "",
                    "score", r.getScore() != null ? r.getScore() : 0));
            done++;
            if (done >= limit) break;
        }
        auditService.record("AI_ACTION", "LEAD", null, "AI scored " + done + " leads", null);
        return Map.of(
                "action", "SCORE_TOP_LEADS",
                "status", "EXECUTED",
                "affected", done,
                "results", results,
                "message", "Rescored " + done + " leads with explainable factors."
        );
    }

    private Map<String, Object> createFollowUpTasks(Map<String, Object> params) {
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();
        int limit = intParam(params, "limit", 5);
        List<Lead> leads = leadRepository.findByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "NEW");
        int done = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        for (Lead lead : leads) {
            if (done >= limit) break;
            Activity a = Activity.builder()
                    .type("TASK")
                    .subject("Follow up: " + lead.getFullName())
                    .description("AI-created follow-up for NEW lead"
                            + (lead.getCompany() != null ? " at " + lead.getCompany() : "")
                            + ". Score: " + (lead.getScore() != null ? lead.getScore() : 0))
                    .status("OPEN")
                    .priority(lead.getScore() != null && lead.getScore() >= 70 ? "HIGH" : "MEDIUM")
                    .ownerId(userId)
                    .relatedType("LEAD")
                    .relatedId(lead.getId())
                    .createdBy(userId)
                    .updatedBy(userId)
                    .build();
            a.setOrganizationId(orgId);
            activityRepository.save(a);
            results.add(Map.of("taskId", a.getId(), "leadId", lead.getId(), "subject", a.getSubject()));
            done++;
        }
        auditService.record("AI_ACTION", "ACTIVITY", null, "AI created " + done + " follow-up tasks", null);
        return Map.of(
                "action", "CREATE_FOLLOW_UP_TASKS",
                "status", "EXECUTED",
                "affected", done,
                "results", results,
                "message", "Created " + done + " follow-up task(s). See Activities."
        );
    }

    private Map<String, Object> draftLogEmail(Map<String, Object> params) {
        UUID orgId = TenantContext.requireOrganizationId();
        List<Lead> leads = leadRepository.findByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "NEW");
        if (leads.isEmpty()) {
            return Map.of("action", "DRAFT_LOG_EMAIL", "status", "SKIPPED", "message", "No NEW leads to email.");
        }
        Lead lead = leads.get(0);
        if (lead.getEmail() == null || lead.getEmail().isBlank()) {
            return Map.of("action", "DRAFT_LOG_EMAIL", "status", "SKIPPED", "message", "Newest NEW lead has no email.");
        }
        String subject = "Following up — " + (lead.getCompany() != null ? lead.getCompany() : "quick intro");
        String body = "Hi " + (lead.getFirstName() != null ? lead.getFirstName() : "there") + ",\n\n"
                + "Thanks for your interest. I'd love to understand your goals and share how we can help"
                + (lead.getCompany() != null ? " at " + lead.getCompany() : "") + ".\n\n"
                + "Would you have 15 minutes this week?\n\nBest regards";
        Map<String, Object> sendBody = new HashMap<>();
        sendBody.put("to", lead.getEmail());
        sendBody.put("subject", subject);
        sendBody.put("body", body);
        sendBody.put("relatedType", "LEAD");
        sendBody.put("relatedId", lead.getId().toString());
        Map<String, Object> emailResult = emailService.send(sendBody);
        auditService.record("AI_ACTION", "EMAIL", lead.getId(), "AI logged outreach email for " + lead.getFullName(), null);
        return Map.of(
                "action", "DRAFT_LOG_EMAIL",
                "status", "EXECUTED",
                "affected", 1,
                "email", emailResult,
                "message", "Logged outreach email to " + lead.getEmail() + " (SMTP or log-only)."
        );
    }

    private Map<String, Object> qualifyHighScore(Map<String, Object> params) {
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();
        int minScore = intParam(params, "minScore", 70);
        List<Lead> leads = leadRepository.findByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(orgId);
        int done = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        for (Lead lead : leads) {
            if ("CONVERTED".equals(lead.getStatus()) || "QUALIFIED".equals(lead.getStatus())) continue;
            int score = lead.getScore() != null ? lead.getScore() : 0;
            if (score < minScore) continue;
            lead.setStatus("QUALIFIED");
            lead.setUpdatedBy(userId);
            leadRepository.save(lead);
            results.add(Map.of("id", lead.getId(), "name", lead.getFullName(), "score", score));
            done++;
        }
        auditService.record("AI_ACTION", "LEAD", null, "AI qualified " + done + " high-score leads", null);
        return Map.of(
                "action", "QUALIFY_HIGH_SCORE",
                "status", "EXECUTED",
                "affected", done,
                "results", results,
                "message", "Qualified " + done + " lead(s) with score >= " + minScore + "."
        );
    }

    private static Map<String, Object> action(String type, String label, Map<String, Object> params) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type);
        m.put("label", label);
        m.put("params", params);
        m.put("requiresConfirm", true);
        return m;
    }

    private static int intParam(Map<String, Object> params, String key, int def) {
        if (params == null || params.get(key) == null) return def;
        try {
            return Integer.parseInt(params.get(key).toString());
        } catch (Exception e) {
            return def;
        }
    }
}
