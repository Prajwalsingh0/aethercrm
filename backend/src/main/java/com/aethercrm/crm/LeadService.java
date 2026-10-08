package com.aethercrm.crm;

import com.aethercrm.audit.AuditService;
import com.aethercrm.crm.dto.LeadRequest;
import com.aethercrm.crm.dto.LeadResponse;
import com.aethercrm.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LeadService {

    private final LeadRepository leadRepository;
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final AuditService auditService;

    public LeadService(LeadRepository leadRepository,
                       AccountRepository accountRepository,
                       ContactRepository contactRepository,
                       AuditService auditService) {
        this.leadRepository = leadRepository;
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> search(String status, UUID ownerId, String q, Pageable pageable) {
        UUID orgId = TenantContext.requireOrganizationId();
        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        return leadRepository.search(orgId, status, ownerId, query, pageable).map(LeadResponse::from);
    }

    @Transactional(readOnly = true)
    public LeadResponse get(UUID id) {
        return LeadResponse.from(findOwned(id));
    }

    @Transactional
    public LeadResponse create(LeadRequest req) {
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();

        Lead lead = Lead.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail() != null ? req.getEmail().toLowerCase() : null)
                .phone(req.getPhone())
                .company(req.getCompany())
                .title(req.getTitle())
                .source(req.getSource() != null ? req.getSource() : "MANUAL")
                .status(req.getStatus() != null ? req.getStatus() : "NEW")
                .score(0)
                .ownerId(req.getOwnerId() != null ? req.getOwnerId() : userId)
                .description(req.getDescription())
                .address(req.getAddress())
                .website(req.getWebsite())
                .annualRevenue(req.getAnnualRevenue())
                .currency(req.getCurrency() != null ? req.getCurrency() : "INR")
                .createdBy(userId)
                .updatedBy(userId)
                .build();
        lead.setOrganizationId(orgId);
        applyScore(lead);
        leadRepository.save(lead);
        auditService.record("CREATED", "LEAD", lead.getId(), "Lead created: " + lead.getFullName(), null);
        return LeadResponse.from(lead);
    }

    @Transactional
    public LeadResponse update(UUID id, LeadRequest req) {
        Lead lead = findOwned(id);
        UUID userId = TenantContext.requireUserId();
        if (req.getFirstName() != null) lead.setFirstName(req.getFirstName());
        if (req.getLastName() != null) lead.setLastName(req.getLastName());
        if (req.getEmail() != null) lead.setEmail(req.getEmail().toLowerCase());
        if (req.getPhone() != null) lead.setPhone(req.getPhone());
        if (req.getCompany() != null) lead.setCompany(req.getCompany());
        if (req.getTitle() != null) lead.setTitle(req.getTitle());
        if (req.getSource() != null) lead.setSource(req.getSource());
        if (req.getStatus() != null) lead.setStatus(req.getStatus());
        if (req.getOwnerId() != null) lead.setOwnerId(req.getOwnerId());
        if (req.getDescription() != null) lead.setDescription(req.getDescription());
        if (req.getAddress() != null) lead.setAddress(req.getAddress());
        if (req.getWebsite() != null) lead.setWebsite(req.getWebsite());
        if (req.getAnnualRevenue() != null) lead.setAnnualRevenue(req.getAnnualRevenue());
        if (req.getCurrency() != null) lead.setCurrency(req.getCurrency());
        lead.setUpdatedBy(userId);
        applyScore(lead);
        leadRepository.save(lead);
        auditService.record("UPDATED", "LEAD", lead.getId(), "Lead updated: " + lead.getFullName(), null);
        return LeadResponse.from(lead);
    }

    @Transactional
    public LeadResponse rescore(UUID id) {
        Lead lead = findOwned(id);
        applyScore(lead);
        lead.setUpdatedBy(TenantContext.requireUserId());
        leadRepository.save(lead);
        auditService.record("SCORED", "LEAD", lead.getId(),
                "Lead rescored to " + lead.getScore() + ": " + lead.getFullName(),
                lead.getScoreFactorsJson());
        return LeadResponse.from(lead);
    }

    @Transactional
    public void softDelete(UUID id) {
        Lead lead = findOwned(id);
        lead.setDeletedAt(Instant.now());
        lead.setUpdatedBy(TenantContext.requireUserId());
        leadRepository.save(lead);
    }

    private Lead findOwned(UUID id) {
        UUID orgId = TenantContext.requireOrganizationId();
        return leadRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found"));
    }

    @Transactional
    public LeadResponse convert(UUID leadId) {
        Lead lead = findOwned(leadId);
        if ("CONVERTED".equals(lead.getStatus()) || lead.getConvertedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lead already converted");
        }
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();

        Account account = null;
        if (lead.getCompany() != null && !lead.getCompany().isBlank()) {
            account = Account.builder()
                    .name(lead.getCompany())
                    .website(lead.getWebsite())
                    .annualRevenue(lead.getAnnualRevenue())
                    .currency(lead.getCurrency() != null ? lead.getCurrency() : "INR")
                    .phone(lead.getPhone())
                    .email(lead.getEmail())
                    .ownerId(lead.getOwnerId() != null ? lead.getOwnerId() : userId)
                    .status("ACTIVE")
                    .createdBy(userId)
                    .updatedBy(userId)
                    .build();
            account.setOrganizationId(orgId);
            accountRepository.save(account);
        }

        String firstName = lead.getFirstName() != null ? lead.getFirstName() : "Unknown";
        String lastName = lead.getLastName() != null ? lead.getLastName() : "";
        Contact contact = Contact.builder()
                .accountId(account != null ? account.getId() : null)
                .firstName(firstName)
                .lastName(lastName)
                .email(lead.getEmail())
                .phone(lead.getPhone())
                .title(lead.getTitle())
                .ownerId(lead.getOwnerId() != null ? lead.getOwnerId() : userId)
                .status("ACTIVE")
                .source(lead.getSource())
                .createdBy(userId)
                .updatedBy(userId)
                .build();
        contact.setOrganizationId(orgId);
        contactRepository.save(contact);

        lead.setStatus("CONVERTED");
        lead.setConvertedAt(Instant.now());
        lead.setConvertedContactId(contact.getId());
        if (account != null) lead.setConvertedAccountId(account.getId());
        lead.setUpdatedBy(userId);
        leadRepository.save(lead);
        auditService.record("CONVERTED", "LEAD", lead.getId(), "Lead converted: " + lead.getFullName(), null);
        return LeadResponse.from(lead);
    }

    private void applyScore(Lead lead) {
        List<Map<String, Object>> factors = new ArrayList<>();
        int score = 0;
        if (lead.getEmail() != null && !lead.getEmail().isBlank()) {
            score += 15;
            factors.add(factor("email", 15, "Has business email"));
        }
        if (lead.getPhone() != null && !lead.getPhone().isBlank()) {
            score += 10;
            factors.add(factor("phone", 10, "Phone number present"));
        }
        if (lead.getCompany() != null && !lead.getCompany().isBlank()) {
            score += 20;
            factors.add(factor("company", 20, "Company identified"));
        }
        if (lead.getTitle() != null && !lead.getTitle().isBlank()) {
            score += 10;
            int pts = 10;
            String t = lead.getTitle().toLowerCase();
            if (t.contains("ceo") || t.contains("founder") || t.contains("director") || t.contains("vp") || t.contains("head")) {
                pts = 18;
                score += 8;
            }
            factors.add(factor("title", pts, "Title: " + lead.getTitle()));
        }
        if (lead.getWebsite() != null && !lead.getWebsite().isBlank()) {
            score += 5;
            factors.add(factor("website", 5, "Company website"));
        }
        if (lead.getAnnualRevenue() != null && lead.getAnnualRevenue().doubleValue() > 0) {
            double rev = lead.getAnnualRevenue().doubleValue();
            int pts = rev >= 10_000_000 ? 25 : (rev >= 1_000_000 ? 20 : 12);
            score += pts;
            factors.add(factor("revenue", pts, "Annual revenue signal (INR)"));
        }
        if (lead.getSource() != null) {
            String s = lead.getSource().toUpperCase();
            if ("REFERRAL".equals(s)) {
                score += 15;
                factors.add(factor("source", 15, "Referral source (high trust)"));
            } else if ("WEBSITE".equals(s) || "INBOUND".equals(s)) {
                score += 12;
                factors.add(factor("source", 12, "Inbound / website"));
            } else if ("WHATSAPP".equals(s) || "INDIA_INBOUND".equals(s)) {
                score += 10;
                factors.add(factor("source", 10, "WhatsApp / India inbound"));
            }
        }
        if ("QUALIFIED".equalsIgnoreCase(lead.getStatus())) {
            score += 10;
            factors.add(factor("status", 10, "Already marked qualified"));
        }
        score = Math.min(score, 100);
        lead.setScore(score);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < factors.size(); i++) {
            Map<String, Object> f = factors.get(i);
            if (i > 0) sb.append(",");
            String label = String.valueOf(f.get("label")).replace("\\", "\\\\").replace("\"", "'");
            sb.append("{\"key\":\"").append(f.get("key")).append("\",");
            sb.append("\"points\":").append(f.get("points")).append(",");
            sb.append("\"label\":\"").append(label).append("\"}");
        }
        sb.append("]");
        lead.setScoreFactorsJson(sb.toString());
    }

    private static Map<String, Object> factor(String key, int points, String label) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key", key);
        m.put("points", points);
        m.put("label", label);
        return m;
    }
}
