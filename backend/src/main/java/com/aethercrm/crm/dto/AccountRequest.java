package com.aethercrm.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AccountRequest {
    @NotBlank @Size(max = 255)
    private String name;
    @Size(max = 500) private String website;
    @Size(max = 100) private String industry;
    @Size(max = 50) private String size;
    private BigDecimal annualRevenue;
    @Size(max = 3) private String currency;
    @Size(max = 50) private String phone;
    @Size(max = 255) private String email;
    private String billingAddress;
    private String shippingAddress;
    private String description;
    private UUID ownerId;
    @Size(max = 50) private String status;
}
