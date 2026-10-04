package com.aethercrm.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    @Query("SELECT p FROM Product p WHERE p.organizationId = :orgId AND p.deletedAt IS NULL " +
           "AND (:activeOnly = false OR p.isActive = true) " +
           "AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Product> search(@Param("orgId") UUID orgId, @Param("activeOnly") boolean activeOnly, @Param("q") String q, Pageable pageable);

    Optional<Product> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);
    long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);
}
