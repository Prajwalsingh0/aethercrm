package com.aethercrm.sales;

import com.aethercrm.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pipeline_stages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PipelineStage extends TenantAwareEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private Integer position = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer probability = 0;

    @Column(name = "is_won", nullable = false)
    @Builder.Default
    private boolean won = false;

    @Column(name = "is_lost", nullable = false)
    @Builder.Default
    private boolean lost = false;

    @Column(length = 20)
    private String color;
}
