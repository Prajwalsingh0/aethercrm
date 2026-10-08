package com.aethercrm.insights;

import com.aethercrm.activity.ActivityRepository;
import com.aethercrm.crm.Lead;
import com.aethercrm.crm.LeadRepository;
import com.aethercrm.sales.OpportunityService;
import com.aethercrm.support.TicketRepository;
import com.aethercrm.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InsightsService {

    private final LeadRepository leadRepository;
    private final OpportunityService opportunityService;
    private final TicketRepository ticketRepository;
    private final ActivityRepository activityRepository;

    public InsightsService(LeadRepository leadRepository,
                           OpportunityService opportunityService,
                           TicketRepository ticketRepository,
                           ActivityRepository activityRepository) {
        this.leadRepository = leadRepository;
        this.opportunityService = opportunityService;
        this.ticketRepository = ticketRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> snapshot() {
        UUID orgId = TenantContext.requireOrganizationId();
        List<Lead> leads = leadRepository.findByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(orgId);

        List<Map<String, Object>> hotLeads = leads.stream()
                .filter(l -> !"CONVERTED".equals(l.getStatus()))
                .filter(l -> l.getScore() != null && l.getScore() >= 60)
                .sorted((a, b) -> Integer.compare(
                        b.getScore() != null ? b.getScore() : 0,
                        a.getScore() != null ? a.getScore() : 0))
                .limit(5)
                .map(l -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", l.getId());
                    m.put("name", l.getFullName());
                    m.put("company", l.getCompany());
                    m.put("score", l.getScore());
                    m.put("status", l.getStatus());
                    return m;
                })
                .collect(Collectors.toList());

        long newLeads = leads.stream().filter(l -> "NEW".equals(l.getStatus())).count();
        long highScore = leads.stream()
                .filter(l -> !"CONVERTED".equals(l.getStatus()))
                .filter(l -> l.getScore() != null && l.getScore() >= 70)
                .count();
        long openTasks = activityRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "OPEN");
        long openTickets = ticketRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "OPEN");
        Map<String, Object> pipe = opportunityService.pipelineSummary();

        List<Map<String, Object>> insights = new ArrayList<>();
        if (highScore > 0) {
            insights.add(insight("HOT_LEADS", "warning",
                    highScore + " lead(s) score >= 70 — prioritize outreach or qualify them."));
        }
        if (newLeads >= 3) {
            insights.add(insight("NEW_LEADS", "info",
                    newLeads + " NEW leads waiting — create follow-up tasks from AI Command Center."));
        }
        if (openTasks == 0 && newLeads > 0) {
            insights.add(insight("NO_TASKS", "warning",
                    "No open tasks but you have NEW leads — pipeline may stall."));
        }
        if (openTickets >= 5) {
            insights.add(insight("SUPPORT_LOAD", "danger",
                    openTickets + " open tickets — support queue is elevated."));
        }
        Object openDeals = pipe.get("openDeals");
        if (openDeals instanceof Number && ((Number) openDeals).longValue() == 0 && highScore > 0) {
            insights.add(insight("NO_DEALS", "info",
                    "High-score leads exist but no open deals — convert or create opportunities."));
        }
        if (insights.isEmpty()) {
            insights.add(insight("HEALTHY", "success", "Workspace looks balanced. Keep scoring and following up."));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("insights", insights);
        out.put("hotLeads", hotLeads);
        out.put("stats", Map.of(
                "newLeads", newLeads,
                "highScoreLeads", highScore,
                "openTasks", openTasks,
                "openTickets", openTickets,
                "openDeals", pipe.get("openDeals"),
                "weightedValue", pipe.get("weightedValue")
        ));
        out.put("differentiator", "Explainable lead scoring · AI actions · INR-native · multi-tenant audit");
        return out;
    }

    private static Map<String, Object> insight(String code, String severity, String text) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", code);
        m.put("severity", severity);
        m.put("text", text);
        return m;
    }
}
