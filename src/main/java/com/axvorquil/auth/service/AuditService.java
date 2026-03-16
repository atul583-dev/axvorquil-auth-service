package com.axvorquil.auth.service;

import com.axvorquil.auth.model.User;
import com.axvorquil.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES      = 30;

    private final UserRepository userRepository;
    private final SecurityClient securityClient;

    // ── Forward event to security service ────────────────────────────

    public void log(String userId, String email, String action,
                    String ip, String userAgent, String details, String severity) {
        securityClient.sendEvent(userId, email, action, ip, userAgent, details, severity);
    }

    // ── Lockout helpers (state lives in User, managed here) ───────────

    public boolean isLocked(User user) {
        return user.getLockedUntil() != null
                && LocalDateTime.now().isBefore(user.getLockedUntil());
    }

    public String lockoutMessage(User user) {
        long minutesLeft = java.time.Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes() + 1;
        return "Account locked due to too many failed attempts. Try again in " + minutesLeft + " minute(s).";
    }

    public void recordFailedLogin(User user, String ip, String userAgent) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
            userRepository.save(user);
            securityClient.sendEvent(user.getId(), user.getEmail(), "ACCOUNT_LOCKED", ip, userAgent,
                    "Locked after " + attempts + " failed attempts", "CRITICAL");
            log.warn("Account locked: {} after {} failed attempts from {}", user.getEmail(), attempts, ip);
        } else {
            userRepository.save(user);
            securityClient.sendEvent(user.getId(), user.getEmail(), "LOGIN_FAILED", ip, userAgent,
                    "Attempt " + attempts + " of " + MAX_FAILED_ATTEMPTS, "WARNING");
        }
    }

    public void recordSuccessfulLogin(User user, String ip, String userAgent) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userRepository.save(user);
        securityClient.sendEvent(user.getId(), user.getEmail(), "LOGIN_SUCCESS", ip, userAgent, null, "INFO");
    }
}
