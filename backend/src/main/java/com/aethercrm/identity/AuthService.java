package com.aethercrm.identity;

import com.aethercrm.identity.dto.AuthResponse;
import com.aethercrm.identity.dto.LoginRequest;
import com.aethercrm.identity.dto.RegisterRequest;
import com.aethercrm.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(OrganizationRepository organizationRepository,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String slug = req.getOrganizationSlug().toLowerCase().replaceAll("[^a-z0-9-]", "-");
        if (organizationRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Organization slug already taken");
        }

        Organization org = Organization.builder()
                .name(req.getOrganizationName())
                .slug(slug)
                .status("ACTIVE")
                .plan("FREE")
                .build();
        organizationRepository.save(org);

        List<Permission> allPerms = permissionRepository.findAll();
        Role adminRole = Role.builder()
                .name("ADMIN")
                .description("Organization administrator")
                .system(true)
                .permissions(Set.copyOf(allPerms))
                .build();
        adminRole.setOrganizationId(org.getId());
        roleRepository.save(adminRole);

        createSystemRole(org.getId(), "MANAGER", "Sales / team manager",
                List.of("lead:read", "lead:write", "contact:read", "contact:write", "account:read", "account:write",
                        "opportunity:read", "opportunity:write", "task:read", "task:write", "ticket:read", "ticket:write",
                        "campaign:read", "report:read", "ai:use"));
        createSystemRole(org.getId(), "SALES", "Sales representative",
                List.of("lead:read", "lead:write", "contact:read", "contact:write", "account:read", "account:write",
                        "opportunity:read", "opportunity:write", "task:read", "task:write", "ai:use"));
        createSystemRole(org.getId(), "SUPPORT", "Support agent",
                List.of("contact:read", "account:read", "ticket:read", "ticket:write", "task:read", "task:write", "ai:use"));

        User user = User.builder()
                .email(req.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .displayName(req.getFirstName() + " " + req.getLastName())
                .status("ACTIVE")
                .role(adminRole)
                .build();
        user.setOrganizationId(org.getId());
        userRepository.save(user);

        return issueTokens(user, org);
    }

    private void createSystemRole(UUID orgId, String name, String description, List<String> permCodes) {
        List<Permission> perms = permissionRepository.findByCodeIn(permCodes);
        Role role = Role.builder()
                .name(name)
                .description(description)
                .system(true)
                .permissions(Set.copyOf(perms))
                .build();
        role.setOrganizationId(orgId);
        roleRepository.save(role);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        List<User> candidates = userRepository.findAllByEmailActive(req.getEmail().toLowerCase());
        if (candidates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = candidates.get(0);
        if (req.getOrganizationSlug() != null && !req.getOrganizationSlug().isBlank()) {
            user = candidates.stream()
                    .filter(u -> {
                        Organization o = organizationRepository.findById(u.getOrganizationId()).orElse(null);
                        return o != null && o.getSlug().equalsIgnoreCase(req.getOrganizationSlug());
                    })
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is not active");
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        Organization org = organizationRepository.findById(user.getOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Organization missing"));

        return issueTokens(user, org);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        String hash = hashToken(refreshTokenValue);
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (stored.isExpired()) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Organization org = organizationRepository.findById(user.getOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Organization missing"));

        return issueTokens(user, org);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) return;
        String hash = hashToken(refreshTokenValue);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(hash).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    private AuthResponse issueTokens(User user, Organization org) {
        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        String access = jwtService.generateAccessToken(user.getId(), org.getId(), user.getEmail(), roleName);
        String refreshValue = jwtService.generateRefreshTokenValue();

        RefreshToken rt = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(refreshValue))
                .expiresAt(jwtService.getRefreshExpiry())
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refreshValue)
                .tokenType("Bearer")
                .expiresInSeconds(15 * 60L)
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .displayName(user.getDisplayName())
                        .role(roleName)
                        .organizationId(org.getId())
                        .organizationName(org.getName())
                        .organizationSlug(org.getSlug())
                        .build())
                .build();
    }

    private static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
