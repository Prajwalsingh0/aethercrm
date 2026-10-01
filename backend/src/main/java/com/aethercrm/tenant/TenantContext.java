package com.aethercrm.tenant;

import java.util.UUID;

/**
 * Holds the current tenant (organization) and user for the request thread.
 * Populated by the JWT authentication filter. Never trust client-supplied tenant IDs.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> ORGANIZATION_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_EMAIL = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID organizationId, UUID userId, String email, String role) {
        ORGANIZATION_ID.set(organizationId);
        USER_ID.set(userId);
        USER_EMAIL.set(email);
        ROLE.set(role);
    }

    public static UUID getOrganizationId() {
        return ORGANIZATION_ID.get();
    }

    public static UUID getUserId() {
        return USER_ID.get();
    }

    public static String getUserEmail() {
        return USER_EMAIL.get();
    }

    public static String getRole() {
        return ROLE.get();
    }

    public static UUID requireOrganizationId() {
        UUID id = getOrganizationId();
        if (id == null) {
            throw new IllegalStateException("No tenant context available");
        }
        return id;
    }

    public static UUID requireUserId() {
        UUID id = getUserId();
        if (id == null) {
            throw new IllegalStateException("No user context available");
        }
        return id;
    }

    public static void clear() {
        ORGANIZATION_ID.remove();
        USER_ID.remove();
        USER_EMAIL.remove();
        ROLE.remove();
    }
}
