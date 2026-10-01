package com.aethercrm.reporting;

import com.aethercrm.crm.AccountRepository;
import com.aethercrm.crm.ContactRepository;
import com.aethercrm.crm.LeadRepository;
import com.aethercrm.sales.OpportunityService;
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

    public DashboardController(LeadRepository leadRepository,
                               AccountRepository accountRepository,
                               ContactRepository contactRepository,
                               OpportunityService opportunityService) {
        this.leadRepository = leadRepository;
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.opportunityService = opportunityService;
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
        Map<String, Object> pipeline = opportunityService.pipelineSummary();

        Map<String, Object> result = new HashMap<>();
        result.put("leads", Map.of("total", totalLeads, "new", newLeads, "qualified", qualified, "converted", converted));
        result.put("accounts", Map.of("total", accounts));
        result.put("contacts", Map.of("total", contacts));
        result.put("pipeline", pipeline);
        result.put("activity", Map.of("tasksDueToday", 0, "overdue", 0));
        return ResponseEntity.ok(result);
    }
}
