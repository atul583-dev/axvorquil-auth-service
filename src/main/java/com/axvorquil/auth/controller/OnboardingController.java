package com.axvorquil.auth.controller;

import com.axvorquil.auth.dto.ApiResponse;
import com.axvorquil.auth.dto.OnboardingStepRequest;
import com.axvorquil.auth.model.Organization;
import com.axvorquil.auth.model.User;
import com.axvorquil.auth.repository.OrganizationRepository;
import com.axvorquil.auth.repository.UserRepository;
import com.axvorquil.auth.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OrganizationService    orgService;
    private final OrganizationRepository orgRepository;
    private final UserRepository         userRepository;

    /** Get current onboarding status */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus(
            @AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        if (user.getOrgId() == null)
            return ResponseEntity.ok(ApiResponse.ok(Map.of("step", 0, "completed", false)));

        Organization org = orgService.getById(user.getOrgId());
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "step",          org.getOnboardingStep(),
                "completed",     org.isOnboardingCompleted(),
                "orgId",         org.getId(),
                "orgName",       org.getName() != null ? org.getName() : "",
                "plan",          org.getPlan(),
                "status",        org.getStatus(),
                "modules", Map.of(
                    "clinic",    org.isClinicEnabled(),
                    "campaigns", org.isCampaignsEnabled(),
                    "stock",     org.isStockEnabled(),
                    "security",  org.isSecurityEnabled()
                )
        )));
    }

    /** Advance to a step and save step data */
    @PostMapping("/step/{step}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Organization>> saveStep(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable int step,
            @RequestBody(required = false) OnboardingStepRequest body) {
        User user = resolveUser(principal);

        // If user has no org yet (e.g. first-time setup), create one now on step 1
        if (user.getOrgId() == null) {
            if (step != 1)
                throw new RuntimeException("No organization found. Please complete step 1 first.");
            String orgName = user.getFirstName() + "'s Organization";
            String baseSlug = orgName.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
            String slug = baseSlug;
            int s = 1;
            while (orgRepository.existsBySlug(slug)) slug = baseSlug + "-" + s++;
            Organization newOrg = Organization.builder()
                    .name(orgName).slug(slug).type("CLINIC")
                    .plan("STARTER").status("TRIAL")
                    .maxSeats(3).usedSeats(1)
                    .billingEmail(user.getEmail())
                    .adminIds(new ArrayList<>(java.util.List.of(user.getId())))
                    .onboardingStep(1).onboardingCompleted(false)
                    .build();
            newOrg = orgRepository.save(newOrg);
            user.setOrgId(newOrg.getId());
            userRepository.save(user);
        }

        // Apply step-specific data
        if (body != null && body.getData() != null && step == 1) {
            Organization org = orgService.getById(user.getOrgId());
            Map<String, Object> data = body.getData();
            if (data.containsKey("name"))    org.setName((String) data.get("name"));
            if (data.containsKey("type"))    org.setType((String) data.get("type"));
            if (data.containsKey("city"))    org.setCity((String) data.get("city"));
            if (data.containsKey("country")) org.setCountry((String) data.get("country"));
            orgRepository.save(org);
        }
        if (body != null && body.getData() != null && step == 3) {
            Map<String, Object> data = body.getData();
            orgService.updateModules(user.getOrgId(),
                    (Boolean) data.getOrDefault("clinic",    true),
                    (Boolean) data.getOrDefault("campaigns", true),
                    (Boolean) data.getOrDefault("stock",     true),
                    (Boolean) data.getOrDefault("security",  true));
        }

        Organization org = orgService.advanceOnboardingStep(user.getOrgId(), step + 1);
        return ResponseEntity.ok(ApiResponse.ok(org));
    }

    /** Mark onboarding as complete */
    @PostMapping("/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Organization>> complete(
            @AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        Organization org = orgService.advanceOnboardingStep(user.getOrgId(), 5);
        return ResponseEntity.ok(ApiResponse.ok(org));
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
