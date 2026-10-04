package com.aethercrm.workflow;

import com.aethercrm.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "workflow_definitions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkflowDefinition extends TenantAwareEntity {
    @Column(nullable = false) private String name;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "is_active", nullable = false) @Builder.Default private boolean isActive = true;
    @Column(name = "trigger_type", nullable = false, length = 50) private String triggerType;
    @Column(name = "trigger_config_json", nullable = false, columnDefinition = "TEXT") private String triggerConfigJson;
    @Column(name = "conditions_json", columnDefinition = "TEXT") private String conditionsJson;
    @Column(name = "actions_json", nullable = false, columnDefinition = "TEXT") private String actionsJson;
    @Column(name = "created_by") private UUID createdBy;
}
