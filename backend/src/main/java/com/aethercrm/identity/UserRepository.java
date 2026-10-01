package com.aethercrm.identity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndOrganizationIdAndDeletedAtIsNull(String email, UUID organizationId);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    List<User> findAllByEmailActive(String email);

    Optional<User> findByIdAndOrganizationIdAndDeletedAtIsNull(UUID id, UUID organizationId);

    boolean existsByEmailAndOrganizationId(String email, UUID organizationId);
}
