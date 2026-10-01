package com.aethercrm.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ContactRequest {
    private UUID accountId;
    @NotBlank @Size(max = 100) private String firstName;
    @NotBlank @Size(max = 100) private String lastName;
    @Email @Size(max = 255) private String email;
    @Size(max = 50) private String phone;
    @Size(max = 50) private String mobile;
    @Size(max = 150) private String title;
    @Size(max = 100) private String department;
    private UUID ownerId;
    @Size(max = 50) private String status;
    @Size(max = 100) private String source;
}
