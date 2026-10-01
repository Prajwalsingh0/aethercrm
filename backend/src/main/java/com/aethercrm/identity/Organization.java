package com.aethercrm.identity;

import com.aethercrm.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String plan = "FREE";

    @Column(name = "settings_json", columnDefinition = "TEXT")
    private String settingsJson;
}
