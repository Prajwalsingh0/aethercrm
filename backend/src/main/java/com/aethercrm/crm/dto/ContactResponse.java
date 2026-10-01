package com.aethercrm.crm.dto;

import com.aethercrm.crm.Contact;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ContactResponse {
    private UUID id;
    private UUID accountId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String mobile;
    private String title;
    private String department;
    private UUID ownerId;
    private String status;
    private String source;
    private Instant createdAt;
    private Instant updatedAt;

    public static ContactResponse from(Contact c) {
        return ContactResponse.builder()
                .id(c.getId()).accountId(c.getAccountId())
                .firstName(c.getFirstName()).lastName(c.getLastName()).fullName(c.getFullName())
                .email(c.getEmail()).phone(c.getPhone()).mobile(c.getMobile())
                .title(c.getTitle()).department(c.getDepartment())
                .ownerId(c.getOwnerId()).status(c.getStatus()).source(c.getSource())
                .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).build();
    }
}
