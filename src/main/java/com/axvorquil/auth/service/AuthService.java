package com.axvorquil.auth.service;

import com.axvorquil.auth.dto.AuthResponse;
import com.axvorquil.auth.dto.LoginRequest;
import com.axvorquil.auth.dto.RegisterRequest;
import com.axvorquil.auth.exception.TokenException;
import com.axvorquil.auth.exception.UserAlreadyExistsException;
import com.axvorquil.auth.model.User;
import com.axvorquil.auth.repository.UserRepository;
import com.axvorquil.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtUtil               jwtUtil;
    private final AuthenticationManager authManager;
    private final EmailService          emailService;

    // ── REGISTER ──────────────────────────────────────────────────
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .verificationToken(verificationToken)
                .enabled(false)          // stays disabled until email verified
                .emailVerified(false)
                .build();

        userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        // Send verification email async
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationToken);

        return "Registration successful. Please check your email to verify your account.";
    }

    // ── LOGIN ─────────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {
        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        String accessToken  = jwtUtil.generateAccessToken(
                user.getEmail(),
                Map.of("roles", user.getRoles(), "name", user.getFirstName())
        );
        String refreshToken = jwtUtil.generateRefreshToken();

        user.setRefreshToken(passwordEncoder.encode(refreshToken));
        userRepository.save(user);

        log.info("User logged in: {}", user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ── REFRESH TOKEN ─────────────────────────────────────────────
    public AuthResponse refreshToken(String refreshToken) {
        User user = userRepository.findAll().stream()
                .filter(u -> u.getRefreshToken() != null
                        && passwordEncoder.matches(refreshToken, u.getRefreshToken()))
                .findFirst()
                .orElseThrow(() -> new TokenException("Invalid or expired refresh token"));

        String newAccessToken  = jwtUtil.generateAccessToken(
                user.getEmail(),
                Map.of("roles", user.getRoles(), "name", user.getFirstName())
        );
        String newRefreshToken = jwtUtil.generateRefreshToken();

        user.setRefreshToken(passwordEncoder.encode(newRefreshToken));
        userRepository.save(user);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    // ── LOGOUT ────────────────────────────────────────────────────
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
            log.info("User logged out: {}", email);
        });
    }

    // ── VERIFY EMAIL ──────────────────────────────────────────────
    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new TokenException("Invalid or expired verification token"));

        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        // Send welcome email async
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        log.info("Email verified for: {}", user.getEmail());
        return "Email verified successfully. You can now log in.";
    }

    // ── FORGOT PASSWORD ───────────────────────────────────────────
    public String forgotPassword(String email) {
        // Always return success to prevent user enumeration attacks
        userRepository.findByEmail(email.toLowerCase().trim()).ifPresent(user -> {
            String resetToken  = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

            user.setPasswordResetToken(resetToken);
            user.setPasswordResetExpiry(expiry);
            userRepository.save(user);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetToken);
            log.info("Password reset requested for: {}", email);
        });

        return "If that email is registered, you will receive a password reset link shortly.";
    }

    // ── RESET PASSWORD ────────────────────────────────────────────
    public String resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new TokenException("Invalid or expired reset token"));

        if (user.getPasswordResetExpiry() == null
                || LocalDateTime.now().isAfter(user.getPasswordResetExpiry())) {
            throw new TokenException("Password reset token has expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setRefreshToken(null);  // invalidate all existing sessions
        userRepository.save(user);

        log.info("Password reset successful for: {}", user.getEmail());
        return "Password reset successful. You can now log in with your new password.";
    }

    // ── RESEND VERIFICATION ───────────────────────────────────────
    public String resendVerification(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), newToken);
        return "Verification email resent. Please check your inbox.";
    }

    // ── Helper ────────────────────────────────────────────────────
    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiryMs() / 1000)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}
