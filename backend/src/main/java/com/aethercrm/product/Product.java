package com.aethercrm.product;

import com.aethercrm.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product extends TenantAwareEntity {
    @Column(nullable = false) private String name;
    @Column(length = 100) private String sku;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    @Builder.Default private BigDecimal unitPrice = BigDecimal.ZERO;
    @Column(length = 3) @Builder.Default private String currency = "INR";
    @Column(name = "is_active", nullable = false) @Builder.Default private boolean isActive = true;
    @Column(length = 100) private String category;
    @Column(name = "deleted_at") private Instant deletedAt;
}
