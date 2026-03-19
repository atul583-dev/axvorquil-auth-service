package com.axvorquil.auth.controller;

import com.axvorquil.auth.dto.*;
import com.axvorquil.auth.model.Organization;
import com.axvorquil.auth.model.User;
import com.axvorquil.auth.repository.UserRepository;
import com.axvorquil.auth.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService orgService;
    private final UserRepository      userRepository;

    /** Get the current user's organization */
    @GetMapping
    public ResponseEntity<ApiResponse<Organization>> getMyOrg(
            @AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        if (user.getOrgId() == null)
            return ResponseEntity.ok(ApiResponse.error("No organization found"));
        return ResponseEntity.ok(ApiResponse.ok(orgService.getById(user.getOrgId())));
    }

    /** Update organization profile */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Organization>> update(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody OrgCreateRequest req) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(ApiResponse.ok(orgService.update(user.getOrgId(), req)));
    }

    /** Create a new organization (admin) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Organization>> create(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody OrgCreateRequest req) {
        User user = resolveUser(principal);
        Organization org = orgService.createDefault(user.getId(), user.getEmail(), req.getName());
        // Assign user to org
        user.setOrgId(org.getId());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok(org));
    }

    /** List all organizations (super-admin view) */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Organization>>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(orgService.listAll()));
    }

    /** Get members of the current org */
    @GetMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getMembers(
            @AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(ApiResponse.ok(orgService.getMembers(user.getOrgId())));
    }

    /** Invite a new member */
    @PostMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> inviteMember(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody InviteMemberRequest req) {
        User user = resolveUser(principal);
        return ResponseEntity.ok(ApiResponse.ok(orgService.inviteMember(user.getOrgId(), req)));
    }

    /** Remove a member */
    @DeleteMapping("/members/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable String userId) {
        User user = resolveUser(principal);
        orgService.removeMember(user.getOrgId(), userId);
        return ResponseEntity.ok(ApiResponse.ok("Member removed", null));
    }

    /** Update which modules are enabled */
    @PutMapping("/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Organization>> updateModules(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody Map<String, Boolean> modules) {
        User user = resolveUser(principal);
        Organization org = orgService.updateModules(user.getOrgId(),
                modules.getOrDefault("clinic",    true),
                modules.getOrDefault("campaigns", true),
                modules.getOrDefault("stock",     true),
                modules.getOrDefault("security",  true));
        return ResponseEntity.ok(ApiResponse.ok(org));
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
