package com.aethercrm.crm;

import com.aethercrm.crm.dto.ContactRequest;
import com.aethercrm.crm.dto.ContactResponse;
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
public class ContactService {

    private final ContactRepository contactRepository;
    private final AccountRepository accountRepository;

    public ContactService(ContactRepository contactRepository, AccountRepository accountRepository) {
        this.contactRepository = contactRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Page<ContactResponse> search(UUID accountId, String q, Pageable pageable) {
        UUID orgId = TenantContext.requireOrganizationId();
        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        return contactRepository.search(orgId, accountId, query, pageable).map(ContactResponse::from);
    }

    @Transactional(readOnly = true)
    public ContactResponse get(UUID id) {
        return ContactResponse.from(findOwned(id));
    }

    @Transactional
    public ContactResponse create(ContactRequest req) {
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();
        if (req.getAccountId() != null) {
            accountRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(req.getAccountId(), orgId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account not found"));
        }
        Contact contact = Contact.builder()
                .accountId(req.getAccountId())
                .firstName(req.getFirstName()).lastName(req.getLastName())
                .email(req.getEmail() != null ? req.getEmail().toLowerCase() : null)
                .phone(req.getPhone()).mobile(req.getMobile())
                .title(req.getTitle()).department(req.getDepartment())
                .ownerId(req.getOwnerId() != null ? req.getOwnerId() : userId)
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .source(req.getSource()).createdBy(userId).updatedBy(userId).build();
        contact.setOrganizationId(orgId);
        contactRepository.save(contact);
        return ContactResponse.from(contact);
    }

    @Transactional
    public ContactResponse update(UUID id, ContactRequest req) {
        Contact contact = findOwned(id);
        UUID userId = TenantContext.requireUserId();
        if (req.getAccountId() != null) contact.setAccountId(req.getAccountId());
        if (req.getFirstName() != null) contact.setFirstName(req.getFirstName());
        if (req.getLastName() != null) contact.setLastName(req.getLastName());
        if (req.getEmail() != null) contact.setEmail(req.getEmail().toLowerCase());
        if (req.getPhone() != null) contact.setPhone(req.getPhone());
        if (req.getMobile() != null) contact.setMobile(req.getMobile());
        if (req.getTitle() != null) contact.setTitle(req.getTitle());
        if (req.getDepartment() != null) contact.setDepartment(req.getDepartment());
        if (req.getOwnerId() != null) contact.setOwnerId(req.getOwnerId());
        if (req.getStatus() != null) contact.setStatus(req.getStatus());
        if (req.getSource() != null) contact.setSource(req.getSource());
        contact.setUpdatedBy(userId);
        contactRepository.save(contact);
        return ContactResponse.from(contact);
    }

    @Transactional
    public void softDelete(UUID id) {
        Contact contact = findOwned(id);
        contact.setDeletedAt(Instant.now());
        contact.setUpdatedBy(TenantContext.requireUserId());
        contactRepository.save(contact);
    }

    private Contact findOwned(UUID id) {
        return contactRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, TenantContext.requireOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));
    }
}
