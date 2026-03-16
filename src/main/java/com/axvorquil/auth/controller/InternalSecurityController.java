package com.axvorquil.auth.controller;

import com.axvorquil.auth.dto.ApiResponse;
import com.axvorquil.auth.model.User;
import com.axvorquil.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
public class InternalSecurityController {

    private final UserRepository userRepository;

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
}
