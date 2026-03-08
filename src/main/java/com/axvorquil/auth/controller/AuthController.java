package com.axvorquil.auth.controller;

import com.axvorquil.auth.dto.*;
import com.axvorquil.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ── POST /api/auth/register ───────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequest request) {

        String message = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, null));
    }

    // ── POST /api/auth/login ──────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // ── POST /api/auth/refresh ────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    // ── POST /api/auth/logout ─────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @AuthenticationPrincipal UserDetails userDetails) {

        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    // ── GET /api/auth/verify-email?token=xxx ─────────────────────
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(
            @RequestParam String token) {

        String message = authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // ── POST /api/auth/resend-verification ───────────────────────
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(
            @RequestParam String email) {

        String message = authService.resendVerification(email);
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // ── POST /api/auth/forgot-password ───────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        String message = authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // ── POST /api/auth/reset-password ────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        String message = authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // ── GET /api/auth/me (protected) ─────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                ApiResponse.success("Authenticated user", userDetails.getUsername()));
    }
}
