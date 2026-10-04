package com.aethercrm.reporting;

import com.aethercrm.activity.ActivityRepository;
import com.aethercrm.crm.AccountRepository;
import com.aethercrm.crm.ContactRepository;
import com.aethercrm.crm.LeadRepository;
import com.aethercrm.sales.OpportunityService;
import com.aethercrm.support.TicketRepository;
import com.aethercrm.tenant.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final LeadRepository leadRepository;
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final OpportunityService opportunityService;
    private final TicketRepository ticketRepository;
    private final ActivityRepository activityRepository;

    public DashboardController(LeadRepository leadRepository,
                               AccountRepository accountRepository,
                               ContactRepository contactRepository,
                               OpportunityService opportunityService,
                               TicketRepository ticketRepository,
                               ActivityRepository activityRepository) {
        this.leadRepository = leadRepository;
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.opportunityService = opportunityService;
        this.ticketRepository = ticketRepository;
        this.activityRepository = activityRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        UUID orgId = TenantContext.requireOrganizationId();
        long totalLeads = leadRepository.countByOrganizationIdAndDeletedAtIsNull(orgId);
        long newLeads = leadRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "NEW");
        long qualified = leadRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "QUALIFIED");
        long converted = leadRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "CONVERTED");
        long accounts = accountRepository.countByOrganizationIdAndDeletedAtIsNull(orgId);
        long contacts = contactRepository.countByOrganizationIdAndDeletedAtIsNull(orgId);
        long openTickets = ticketRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "OPEN");
        long totalTickets = ticketRepository.countByOrganizationIdAndDeletedAtIsNull(orgId);
        long openTasks = activityRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(orgId, "OPEN");

        Map<String, Object> pipeline = opportunityService.pipelineSummary();

        Map<String, Object> result = new HashMap<>();
        result.put("leads", Map.of(
                "total", totalLeads,
                "new", newLeads,
                "qualified", qualified,
                "converted", converted
        ));
        result.put("accounts", Map.of("total", accounts));
        result.put("contacts", Map.of("total", contacts));
        result.put("pipeline", pipeline);
        result.put("support", Map.of(
                "openTickets", openTickets,
                "totalTickets", totalTickets
        ));
        result.put("activity", Map.of(
                "openTasks", openTasks
        ));
        return ResponseEntity.ok(result);
    }
}
