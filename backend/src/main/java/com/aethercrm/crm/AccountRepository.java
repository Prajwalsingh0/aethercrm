package com.aethercrm.crm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    @Query("SELECT a FROM Account a WHERE a.organizationId = :orgId AND a.deletedAt IS NULL " +
           "AND (:q IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(a.email) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Account> search(@Param("orgId") UUID orgId, @Param("q") String q, Pageable pageable);

    Optional<Account> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);
}
