package com.aethercrm.ai;

import com.aethercrm.crm.LeadRepository;
import com.aethercrm.sales.OpportunityService;
import com.aethercrm.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final LeadRepository leadRepository;
    private final OpportunityService opportunityService;

    public AiController(LeadRepository leadRepository, OpportunityService opportunityService) {
        this.leadRepository = leadRepository;
        this.opportunityService = opportunityService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        String message = body.get("message") != null ? body.get("message").toString().trim() : "";
        UUID orgId = TenantContext.requireOrganizationId();
        String lower = message.toLowerCase();

        String reply;
        List<Map<String, Object>> suggestions = new ArrayList<>();

        if (lower.contains("lead") && (lower.contains("not contacted") || lower.contains("inactive") || lower.contains("14"))) {
            long total = leadRepository.countByOrganizationIdAndDeletedAtIsNull(orgId);
            long neu = leadRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "NEW");
            reply = String.format("[Demo AI] %d leads total, %d still NEW. Filter by status=NEW on the Leads page for inactive follow-ups.", total, neu);
        } else if (lower.contains("deal") || lower.contains("pipeline") || lower.contains("closing") || lower.contains("forecast")) {
            Map<String, Object> pipe = opportunityService.pipelineSummary();
            reply = String.format("[Demo AI] Open pipeline: %s deals, value %s, weighted forecast %s.",
                    pipe.get("openDeals"), pipe.get("openValue"), pipe.get("weightedValue"));
        } else if (lower.contains("dashboard") || lower.contains("summary") || lower.contains("report")) {
            long leads = leadRepository.countByOrganizationIdAndDeletedAtIsNull(orgId);
            Map<String, Object> pipe = opportunityService.pipelineSummary();
            reply = String.format("[Demo AI] Snapshot — Leads: %d | Open deals: %s | Weighted: %s. Live from your tenant DB.",
                    leads, pipe.get("openDeals"), pipe.get("weightedValue"));
        } else if (lower.contains("email") || lower.contains("draft") || lower.contains("follow-up")) {
            reply = "[Demo AI] Draft follow-up:\n\nSubject: Following up\n\nHi {{first_name}},\n\nI wanted to follow up on our discussion. Let me know a good time to connect.\n\nBest regards";
            suggestions.add(Map.of("type", "draft_email", "requiresConfirmation", true));
        } else if (lower.contains("task") || lower.contains("remind")) {
            reply = "[Demo AI] I can suggest a task but will not create it without confirmation. Example: follow up with lead tomorrow.";
            suggestions.add(Map.of("type", "create_task", "requiresConfirmation", true));
        } else {
            reply = "[Demo AI] Ask about leads, pipeline, drafts, or summaries. Demo mode uses rule-based answers over your authorized CRM data.";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reply", reply);
        result.put("demoMode", true);
        result.put("provider", "mock");
        result.put("suggestions", suggestions);
        result.put("userMessage", message);
        return ResponseEntity.ok(result);
    }
}
