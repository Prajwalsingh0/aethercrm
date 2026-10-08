package com.aethercrm.crm.dto;

import com.aethercrm.crm.Lead;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class LeadResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String company;
    private String title;
    private String source;
    private String status;
    private Integer score;
    private String scoreFactorsJson;
    private UUID ownerId;
    private String description;
    private String address;
    private String website;
    private BigDecimal annualRevenue;
    private String currency;
    private Instant convertedAt;
    private UUID convertedContactId;
    private UUID convertedAccountId;
    private Instant createdAt;
    private Instant updatedAt;

    public static LeadResponse from(Lead l) {
        return LeadResponse.builder()
                .id(l.getId())
                .firstName(l.getFirstName())
                .lastName(l.getLastName())
                .fullName(l.getFullName())
                .email(l.getEmail())
                .phone(l.getPhone())
                .company(l.getCompany())
                .title(l.getTitle())
                .source(l.getSource())
                .status(l.getStatus())
                .score(l.getScore())
                .scoreFactorsJson(l.getScoreFactorsJson())
                .ownerId(l.getOwnerId())
                .description(l.getDescription())
                .address(l.getAddress())
                .website(l.getWebsite())
                .annualRevenue(l.getAnnualRevenue())
                .currency(l.getCurrency())
                .convertedAt(l.getConvertedAt())
                .convertedContactId(l.getConvertedContactId())
                .convertedAccountId(l.getConvertedAccountId())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
