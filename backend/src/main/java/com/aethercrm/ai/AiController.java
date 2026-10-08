package com.aethercrm.ai;

import com.aethercrm.activity.ActivityRepository;
import com.aethercrm.crm.LeadRepository;
import com.aethercrm.sales.OpportunityService;
import com.aethercrm.support.TicketRepository;
import com.aethercrm.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final LeadRepository leadRepository;
    private final OpportunityService opportunityService;
    private final TicketRepository ticketRepository;
    private final ActivityRepository activityRepository;
    private final AiProviderService aiProviderService;
    private final AiActionService aiActionService;

    public AiController(LeadRepository leadRepository,
                        OpportunityService opportunityService,
                        TicketRepository ticketRepository,
                        ActivityRepository activityRepository,
                        AiProviderService aiProviderService,
                        AiActionService aiActionService) {
        this.leadRepository = leadRepository;
        this.opportunityService = opportunityService;
        this.ticketRepository = ticketRepository;
        this.activityRepository = activityRepository;
        this.aiProviderService = aiProviderService;
        this.aiActionService = aiActionService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        String message = body.get("message") != null ? body.get("message").toString().trim() : "";
        UUID orgId = TenantContext.requireOrganizationId();

        long totalLeads = leadRepository.countByOrganizationIdAndDeletedAtIsNull(orgId);
        long newLeads = leadRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "NEW");
        long qualified = leadRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "QUALIFIED");
        Map<String, Object> pipe = opportunityService.pipelineSummary();
        long openTickets = ticketRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "OPEN");
        long openTasks = activityRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "OPEN");

        String contextBlock = String.format(
                "Tenant CRM snapshot: leads total=%d, new=%d, qualified=%d; open deals=%s, pipeline value=%s, weighted=%s; open tickets=%d; open tasks=%d.",
                totalLeads, newLeads, qualified,
                pipe.get("openDeals"), pipe.get("openValue"), pipe.get("weightedValue"),
                openTickets, openTasks);

        List<Map<String, Object>> suggestions = new ArrayList<>();
        List<Map<String, Object>> proposedActions = aiActionService.propose(message);
        String reply;
        boolean demoMode = !aiProviderService.isLive();

        if (aiProviderService.isLive()) {
            String system = "You are AetherCRM Copilot. Answer using the CRM snapshot. "
                    + "You may suggest actions but never claim you already changed records unless the user executed an action.\n\n"
                    + contextBlock;
            String live = aiProviderService.chat(system, message);
            if (live != null && !live.isBlank()) {
                reply = live;
            } else {
                reply = mockReply(message, totalLeads, newLeads, pipe, openTickets, openTasks, suggestions);
                demoMode = true;
            }
        } else {
            reply = mockReply(message, totalLeads, newLeads, pipe, openTickets, openTasks, suggestions);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reply", reply);
        result.put("demoMode", demoMode);
        result.put("provider", aiProviderService.getProviderName());
        result.put("suggestions", suggestions);
        result.put("proposedActions", proposedActions);
        result.put("userMessage", message);
        result.put("context", Map.of(
                "leads", totalLeads,
                "newLeads", newLeads,
                "openDeals", pipe.get("openDeals"),
                "openTickets", openTickets,
                "openTasks", openTasks
        ));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/actions")
    public ResponseEntity<List<Map<String, Object>>> listActions() {
        return ResponseEntity.ok(aiActionService.propose(""));
    }

    @PostMapping("/actions/execute")
    public ResponseEntity<Map<String, Object>> execute(@RequestBody Map<String, Object> body) {
        String actionType = body.get("actionType") != null ? body.get("actionType").toString() : null;
        @SuppressWarnings("unchecked")
        Map<String, Object> params = body.get("params") instanceof Map
                ? (Map<String, Object>) body.get("params")
                : Map.of();
        return ResponseEntity.ok(aiActionService.execute(actionType, params));
    }

    private String mockReply(String message, long totalLeads, long newLeads, Map<String, Object> pipe,
                             long openTickets, long openTasks, List<Map<String, Object>> suggestions) {
        String lower = message.toLowerCase();
        if (lower.contains("lead") || lower.contains("score")) {
            suggestions.add(Map.of("label", "Rescore leads", "action", "SCORE_TOP_LEADS"));
            return String.format(
                    "You have %d leads (%d NEW). Use explainable scoring and AI Actions to rescore or create follow-ups.",
                    totalLeads, newLeads);
        } else if (lower.contains("deal") || lower.contains("pipeline") || lower.contains("forecast")) {
            return String.format("Open pipeline: %s deals · value %s · weighted %s (INR).",
                    pipe.get("openDeals"), pipe.get("openValue"), pipe.get("weightedValue"));
        } else if (lower.contains("ticket") || lower.contains("support")) {
            return String.format("Support: %d open tickets. Prioritize by SLA on the Support page.", openTickets);
        } else if (lower.contains("task") || lower.contains("follow")) {
            suggestions.add(Map.of("label", "Create follow-up tasks", "action", "CREATE_FOLLOW_UP_TASKS"));
            return String.format("You have %d open tasks. I can create follow-ups for NEW leads — confirm an AI Action below.", openTasks);
        } else if (lower.contains("email")) {
            return "I can log a draft outreach email against a NEW lead (SMTP or log-only). Use the Draft email action.";
        }
        return String.format(
                "AetherCRM snapshot — leads: %d (%d new), open deals: %s, open tickets: %d, open tasks: %d.\n"
                        + "Try: score leads, create follow-up tasks, or pipeline summary.",
                totalLeads, newLeads, pipe.get("openDeals"), openTickets, openTasks);
    }
}
