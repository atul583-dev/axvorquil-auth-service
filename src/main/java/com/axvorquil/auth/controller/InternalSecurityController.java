package com.axvorquil.auth.controller;

import com.axvorquil.auth.dto.ApiResponse;
import com.axvorquil.auth.dto.UserDto;
import com.axvorquil.auth.model.ActiveSession;
import com.axvorquil.auth.model.RevokedToken;
import com.axvorquil.auth.repository.ActiveSessionRepository;
import com.axvorquil.auth.repository.RevokedTokenRepository;
import com.axvorquil.auth.repository.UserRepository;
import com.axvorquil.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
public class InternalSecurityController {

    private final UserRepository           userRepository;
    private final ActiveSessionRepository  activeSessionRepository;
    private final RevokedTokenRepository   revokedTokenRepository;
    private final UserService              userService;

    /**
     * Called by security-service to get currently locked accounts.
     * Internal endpoint — not exposed to browser clients.
     */
    @GetMapping("/locked-accounts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> lockedAccounts() {
        List<Map<String, Object>> locked = userRepository.findAll().stream()
                .filter(u -> u.getLockedUntil() != null
                        && LocalDateTime.now().isBefore(u.getLockedUntil()))
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",          u.getId());
                    m.put("email",       u.getEmail());
                    m.put("name",        u.getFirstName() + " " + u.getLastName());
                    m.put("lockedUntil", u.getLockedUntil().toString());
                    m.put("attempts",    u.getFailedLoginAttempts());
                    return m;
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.success("ok", locked));
    }

    /**
     * Called by security-service to unlock a user account.
     * Internal endpoint — not exposed to browser clients.
     */
    @PostMapping("/unlock/{userId}")
    public ResponseEntity<ApiResponse<Void>> unlock(@PathVariable String userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        });
        return ResponseEntity.ok(ApiResponse.success("Unlocked", null));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ActiveSession>>> sessions() {
        return ResponseEntity.ok(ApiResponse.success("ok", activeSessionRepository.findByActiveTrue()));
    }

    @DeleteMapping("/sessions/{jti}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(@PathVariable String jti) {
        activeSessionRepository.findById(jti).ifPresent(s -> {
            s.setActive(false);
            activeSessionRepository.save(s);
        });
        // Add to revocation blacklist — expire in 24h as safe upper bound
        revokedTokenRepository.save(RevokedToken.builder()
                .jti(jti)
                .expireAt(new Date(System.currentTimeMillis() + 86_400_000L))
                .build());
        return ResponseEntity.ok(ApiResponse.success("Session revoked", null));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDto>>> allUsers() {
        return ResponseEntity.ok(ApiResponse.success("ok", userService.listAll()));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserDto>> updateRole(@PathVariable String id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success("Role updated", userService.updateRole(id, body.get("role"))));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserDto>> toggleStatus(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", userService.toggleStatus(id)));
    }

    @PostMapping("/users/{id}/force-reset")
    public ResponseEntity<ApiResponse<Void>> forceReset(@PathVariable String id) {
        userService.forcePasswordReset(id);
        return ResponseEntity.ok(ApiResponse.success("Password reset triggered", null));
    }
}
