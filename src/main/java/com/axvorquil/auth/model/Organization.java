package com.axvorquil.auth.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "organizations")
public class Organization {

    @Id
    private String id;

    private String name;
    private String slug;         // URL-safe unique identifier
    private String type;         // CLINIC | HOSPITAL | PHARMACY | LAB | OTHER

    /** Subscription plan: STARTER | PRO | ENTERPRISE */
    @Builder.Default
    private String plan = "STARTER";

    /** TRIAL | ACTIVE | PAST_DUE | CANCELLED */
    @Builder.Default
    private String status = "TRIAL";

    @Builder.Default private int maxSeats  = 3;
    @Builder.Default private int usedSeats = 1;

    private String billingEmail;
    private String billingPhone;
    private String address;
    private String city;
    private String country;

    // ── Razorpay ──────────────────────────────────────────────────────────────
    private String razorpayCustomerId;
    private String currentSubscriptionId;

    private LocalDateTime currentPeriodEnd;

    @Builder.Default
    private LocalDateTime trialEndsAt = LocalDateTime.now().plusDays(14);

    // ── Module toggles ────────────────────────────────────────────────────────
    @Builder.Default private boolean clinicEnabled    = true;
    @Builder.Default private boolean campaignsEnabled = true;
    @Builder.Default private boolean stockEnabled     = true;
    @Builder.Default private boolean securityEnabled  = true;

    // ── Onboarding ────────────────────────────────────────────────────────────
    @Builder.Default private int     onboardingStep      = 1;
    @Builder.Default private boolean onboardingCompleted = false;

    /** Admin user IDs */
    private List<String> adminIds;

    @CreatedDate
    private LocalDateTime createdAt;
}
