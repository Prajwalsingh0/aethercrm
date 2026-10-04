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

    public AiController(LeadRepository leadRepository,
                        OpportunityService opportunityService,
                        TicketRepository ticketRepository,
                        ActivityRepository activityRepository,
                        AiProviderService aiProviderService) {
        this.leadRepository = leadRepository;
        this.opportunityService = opportunityService;
        this.ticketRepository = ticketRepository;
        this.activityRepository = activityRepository;
        this.aiProviderService = aiProviderService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        String message = body.get("message") != null ? body.get("message").toString().trim() : "";
        UUID orgId = TenantContext.requireOrganizationId();

        long totalLeads = leadRepository.countByOrganizationIdAndDeletedAtIsNull(orgId);
        long newLeads = leadRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "NEW");
        Map<String, Object> pipe = opportunityService.pipelineSummary();
        long openTickets = ticketRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "OPEN");
        long openTasks = activityRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "OPEN");

        String contextBlock = String.format(
                "Tenant CRM snapshot: leads total=%d, new=%d; open deals=%s, pipeline value=%s, weighted=%s; open tickets=%d; open tasks=%d.",
                totalLeads, newLeads, pipe.get("openDeals"), pipe.get("openValue"), pipe.get("weightedValue"),
                openTickets, openTasks);

        List<Map<String, Object>> suggestions = new ArrayList<>();
        String reply;
        boolean demoMode = !aiProviderService.isLive();

        if (aiProviderService.isLive()) {
            String system = "You are AetherCRM Copilot. Answer using the CRM snapshot only. Be concise. Never invent private data.\n\n" + contextBlock;
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

    private String mockReply(String message, long totalLeads, long newLeads, Map<String, Object> pipe,
                             long openTickets, long openTasks, List<Map<String, Object>> suggestions) {
        String lower = message.toLowerCase();
        if (lower.contains("lead")) {
            return String.format("[Demo AI] You have %d leads total (%d NEW). Open the Leads page to filter and convert.", totalLeads, newLeads);
        } else if (lower.contains("deal") || lower.contains("pipeline") || lower.contains("forecast")) {
            return String.format("[Demo AI] Open pipeline: %s deals, value %s, weighted %s.",
                    pipe.get("openDeals"), pipe.get("openValue"), pipe.get("weightedValue"));
        } else if (lower.contains("ticket") || lower.contains("support")) {
            return String.format("[Demo AI] There are %d open support tickets.", openTickets);
        } else if (lower.contains("task") || lower.contains("activit")) {
            suggestions.add(Map.of("type", "create_task", "requiresConfirmation", true));
            return String.format("[Demo AI] You have %d open tasks/activities.", openTasks);
        } else if (lower.contains("email") || lower.contains("draft") || lower.contains("follow-up")) {
            suggestions.add(Map.of("type", "draft_email", "requiresConfirmation", true));
            return "[Demo AI] Draft follow-up:\n\nSubject: Following up\n\nHi {{first_name}},\n\nI wanted to follow up on our recent discussion.\n\nBest regards";
        } else if (lower.contains("dashboard") || lower.contains("summary") || lower.contains("report")) {
            return String.format("[Demo AI] Snapshot — Leads: %d | Open deals: %s | Tickets: %d | Tasks: %d | Weighted: %s",
                    totalLeads, pipe.get("openDeals"), openTickets, openTasks, pipe.get("weightedValue"));
        }
        return "[Demo AI] Try: pipeline summary, open tickets, draft follow-up. Set AI_PROVIDER=openai and OPENAI_API_KEY for live LLM.";
    }
}
