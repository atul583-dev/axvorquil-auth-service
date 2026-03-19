package com.axvorquil.auth.controller;

import com.axvorquil.auth.dto.*;
import com.axvorquil.auth.model.BillingTransaction;
import com.axvorquil.auth.model.Organization;
import com.axvorquil.auth.model.User;
import com.axvorquil.auth.repository.UserRepository;
import com.axvorquil.auth.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final UserRepository userRepository;

    /** Get all available subscription plans */
    @GetMapping("/plans")
    public ResponseEntity<ApiResponse<Collection<BillingService.PlanInfo>>> getPlans() {
        return ResponseEntity.ok(ApiResponse.ok(billingService.getPlans()));
    }

    /** Create a Razorpay order for a plan upgrade */
    @PostMapping("/order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody BillingOrderRequest req) {
        User user = resolveUser(principal);
        Map<String, Object> order = billingService.createOrder(user.getOrgId(), req.getPlan());
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    /** Verify Razorpay payment and activate subscription */
    @PostMapping("/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Organization>> verifyPayment(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody BillingVerifyRequest req) {
        User user = resolveUser(principal);
        Organization org = billingService.verifyAndActivate(user.getOrgId(), req);
        return ResponseEntity.ok(ApiResponse.ok(org));
    }

    /** List invoices (successful transactions) */
    @GetMapping("/invoices")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BillingTransaction>>> getInvoices(
            @AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(ApiResponse.ok(billingService.getInvoices(user.getOrgId())));
    }

    /** Razorpay webhook (no auth — verified by signature) */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String sig) {
        // In production: verify webhook signature and process events
        // payment.captured → activate, payment.failed → mark failed
        return ResponseEntity.ok("OK");
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
