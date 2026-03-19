package com.axvorquil.auth.service;

import com.axvorquil.auth.dto.InviteMemberRequest;
import com.axvorquil.auth.dto.OrgCreateRequest;
import com.axvorquil.auth.model.Organization;
import com.axvorquil.auth.model.User;
import com.axvorquil.auth.repository.OrganizationRepository;
import com.axvorquil.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository orgRepository;
    private final UserRepository         userRepository;
    private final PasswordEncoder        passwordEncoder;
    private final EmailService           emailService;

    // ── Create (called during first-user registration) ────────────────────────
    public Organization createDefault(String ownerUserId, String ownerEmail, String ownerName) {
        String slug = generateSlug(ownerName + " Organization");
        Organization org = Organization.builder()
                .name(ownerName + "'s Organization")
                .slug(slug)
                .type("CLINIC")
                .plan("STARTER")
                .status("TRIAL")
                .maxSeats(3)
                .usedSeats(1)
                .billingEmail(ownerEmail)
                .adminIds(new ArrayList<>(List.of(ownerUserId)))
                .onboardingStep(1)
                .onboardingCompleted(false)
                .build();
        return orgRepository.save(org);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public Organization getById(String id) {
        return orgRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found: " + id));
    }

    public Organization update(String orgId, OrgCreateRequest req) {
        Organization org = getById(orgId);
        org.setName(req.getName());
        if (req.getType()         != null) org.setType(req.getType());
        if (req.getBillingEmail() != null) org.setBillingEmail(req.getBillingEmail());
        if (req.getBillingPhone() != null) org.setBillingPhone(req.getBillingPhone());
        if (req.getAddress()      != null) org.setAddress(req.getAddress());
        if (req.getCity()         != null) org.setCity(req.getCity());
        if (req.getCountry()      != null) org.setCountry(req.getCountry());
        return orgRepository.save(org);
    }

    public List<Organization> listAll() {
        return orgRepository.findAllByOrderByCreatedAtDesc();
    }

    // ── Member management ─────────────────────────────────────────────────────

    public List<User> getMembers(String orgId) {
        return userRepository.findAll().stream()
                .filter(u -> orgId.equals(u.getOrgId()))
                .toList();
    }

    public User inviteMember(String orgId, InviteMemberRequest req) {
        Organization org = getById(orgId);

        if (org.getUsedSeats() >= org.getMaxSeats()) {
            throw new IllegalStateException(
                    "Seat limit reached (" + org.getMaxSeats() + " seats). Upgrade your plan to add more team members.");
        }

        // Create user with temporary password; they must reset it
        String tempPassword = UUID.randomUUID().toString().substring(0, 12);
        User member = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName() != null ? req.getLastName() : "")
                .email(req.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(tempPassword))
                .orgId(orgId)
                .clinicRole(req.getRole())
                .enabled(true)
                .emailVerified(true)
                .build();
        userRepository.save(member);

        org.setUsedSeats(org.getUsedSeats() + 1);
        orgRepository.save(org);

        // TODO: send invitation email with temp password
        log.info("Invited member {} to org {}", req.getEmail(), orgId);
        return member;
    }

    public void removeMember(String orgId, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!orgId.equals(user.getOrgId()))
            throw new RuntimeException("User does not belong to this organization");
        user.setOrgId(null);
        userRepository.save(user);

        Organization org = getById(orgId);
        org.setUsedSeats(Math.max(0, org.getUsedSeats() - 1));
        orgRepository.save(org);
    }

    // ── Onboarding ────────────────────────────────────────────────────────────

    public Organization advanceOnboardingStep(String orgId, int step) {
        Organization org = getById(orgId);
        if (step > org.getOnboardingStep()) org.setOnboardingStep(step);
        if (step >= 5) org.setOnboardingCompleted(true);
        return orgRepository.save(org);
    }

    public Organization updateModules(String orgId, boolean clinic, boolean campaigns,
            boolean stock, boolean security) {
        Organization org = getById(orgId);
        org.setClinicEnabled(clinic);
        org.setCampaignsEnabled(campaigns);
        org.setStockEnabled(stock);
        org.setSecurityEnabled(security);
        return orgRepository.save(org);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String generateSlug(String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        String slug = base;
        int suffix = 1;
        while (orgRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }
}
