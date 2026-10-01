package com.aethercrm.crm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {

    @Query("SELECT c FROM Contact c WHERE c.organizationId = :orgId AND c.deletedAt IS NULL " +
           "AND (:accountId IS NULL OR c.accountId = :accountId) " +
           "AND (:q IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Contact> search(@Param("orgId") UUID orgId,
                         @Param("accountId") UUID accountId,
                         @Param("q") String q,
                         Pageable pageable);

    Optional<Contact> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    List<Contact> findByAccountIdAndOrganizationIdAndDeletedAtIsNull(UUID accountId, UUID organizationId);

    long countByOrganizationIdAndDeletedAtIsNull(UUID organizationId);
}
