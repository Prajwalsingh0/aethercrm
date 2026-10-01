package com.aethercrm.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class LeadRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 50)
    private String phone;

    @Size(max = 255)
    private String company;

    @Size(max = 150)
    private String title;

    @Size(max = 100)
    private String source;

    @Size(max = 50)
    private String status;

    private UUID ownerId;

    private String description;

    private String address;

    @Size(max = 500)
    private String website;

    private BigDecimal annualRevenue;

    @Size(max = 3)
    private String currency;
}
