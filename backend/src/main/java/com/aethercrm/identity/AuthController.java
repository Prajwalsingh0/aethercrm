package com.aethercrm.identity;

import com.aethercrm.identity.dto.AuthResponse;
import com.aethercrm.identity.dto.LoginRequest;
import com.aethercrm.identity.dto.RegisterRequest;
import com.aethercrm.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public AuthController(AuthService authService,
                          UserRepository userRepository,
                          OrganizationRepository organizationRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) Map<String, String> body) {
        if (body != null) {
            authService.logout(body.get("refreshToken"));
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> me() {
        var userId = TenantContext.requireUserId();
        var orgId = TenantContext.requireOrganizationId();
        User user = userRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(userId, orgId)
                .orElseThrow();
        Organization org = organizationRepository.findById(orgId).orElseThrow();
        return ResponseEntity.ok(AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .displayName(user.getDisplayName())
                .role(user.getRole() != null ? user.getRole().getName() : "USER")
                .organizationId(org.getId())
                .organizationName(org.getName())
                .organizationSlug(org.getSlug())
                .build());
    }
}
