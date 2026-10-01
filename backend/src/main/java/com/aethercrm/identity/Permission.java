package com.aethercrm.identity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 50)
    private String module;
}
