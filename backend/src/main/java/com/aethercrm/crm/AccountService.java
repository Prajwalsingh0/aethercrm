package com.aethercrm.crm;

import com.aethercrm.crm.dto.AccountRequest;
import com.aethercrm.crm.dto.AccountResponse;
import com.aethercrm.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> search(String q, Pageable pageable) {
        UUID orgId = TenantContext.requireOrganizationId();
        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        return accountRepository.search(orgId, query, pageable).map(AccountResponse::from);
    }

    @Transactional(readOnly = true)
    public AccountResponse get(UUID id) {
        return AccountResponse.from(findOwned(id));
    }

    @Transactional
    public AccountResponse create(AccountRequest req) {
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();
        Account account = Account.builder()
                .name(req.getName()).website(req.getWebsite()).industry(req.getIndustry())
                .size(req.getSize()).annualRevenue(req.getAnnualRevenue())
                .currency(req.getCurrency() != null ? req.getCurrency() : "INR")
                .phone(req.getPhone()).email(req.getEmail())
                .billingAddress(req.getBillingAddress()).shippingAddress(req.getShippingAddress())
                .description(req.getDescription())
                .ownerId(req.getOwnerId() != null ? req.getOwnerId() : userId)
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .createdBy(userId).updatedBy(userId).build();
        account.setOrganizationId(orgId);
        accountRepository.save(account);
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse update(UUID id, AccountRequest req) {
        Account account = findOwned(id);
        UUID userId = TenantContext.requireUserId();
        if (req.getName() != null) account.setName(req.getName());
        if (req.getWebsite() != null) account.setWebsite(req.getWebsite());
        if (req.getIndustry() != null) account.setIndustry(req.getIndustry());
        if (req.getSize() != null) account.setSize(req.getSize());
        if (req.getAnnualRevenue() != null) account.setAnnualRevenue(req.getAnnualRevenue());
        if (req.getCurrency() != null) account.setCurrency(req.getCurrency());
        if (req.getPhone() != null) account.setPhone(req.getPhone());
        if (req.getEmail() != null) account.setEmail(req.getEmail());
        if (req.getBillingAddress() != null) account.setBillingAddress(req.getBillingAddress());
        if (req.getShippingAddress() != null) account.setShippingAddress(req.getShippingAddress());
        if (req.getDescription() != null) account.setDescription(req.getDescription());
        if (req.getOwnerId() != null) account.setOwnerId(req.getOwnerId());
        if (req.getStatus() != null) account.setStatus(req.getStatus());
        account.setUpdatedBy(userId);
        accountRepository.save(account);
        return AccountResponse.from(account);
    }

    @Transactional
    public void softDelete(UUID id) {
        Account account = findOwned(id);
        account.setDeletedAt(Instant.now());
        account.setUpdatedBy(TenantContext.requireUserId());
        accountRepository.save(account);
    }

    private Account findOwned(UUID id) {
        return accountRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, TenantContext.requireOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }
}
