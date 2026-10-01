package com.aethercrm.crm.dto;

import com.aethercrm.crm.Account;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AccountResponse {
    private UUID id;
    private String name;
    private String website;
    private String industry;
    private String size;
    private BigDecimal annualRevenue;
    private String currency;
    private String phone;
    private String email;
    private String billingAddress;
    private String shippingAddress;
    private String description;
    private UUID ownerId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static AccountResponse from(Account a) {
        return AccountResponse.builder()
                .id(a.getId()).name(a.getName()).website(a.getWebsite())
                .industry(a.getIndustry()).size(a.getSize())
                .annualRevenue(a.getAnnualRevenue()).currency(a.getCurrency())
                .phone(a.getPhone()).email(a.getEmail())
                .billingAddress(a.getBillingAddress()).shippingAddress(a.getShippingAddress())
                .description(a.getDescription()).ownerId(a.getOwnerId()).status(a.getStatus())
                .createdAt(a.getCreatedAt()).updatedAt(a.getUpdatedAt()).build();
    }
}
