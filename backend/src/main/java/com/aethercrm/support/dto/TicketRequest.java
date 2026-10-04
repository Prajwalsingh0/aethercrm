package com.aethercrm.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class TicketRequest {
    @NotBlank
    @Size(max = 500)
    private String subject;

    private String description;

    @Size(max = 50)
    private String status;

    @Size(max = 20)
    private String priority;

    @Size(max = 100)
    private String category;

    private UUID contactId;
    private UUID accountId;
    private UUID assigneeId;
}
