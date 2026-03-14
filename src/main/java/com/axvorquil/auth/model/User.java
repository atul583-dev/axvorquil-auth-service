package com.axvorquil.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String firstName;
    private String lastName;

    @Indexed(unique = true)
    private String email;

    private String password;

    @Builder.Default
    private Set<String> roles = Set.of("ROLE_USER");

    /** Clinic-level role: ADMIN | DOCTOR | RECEPTIONIST */
    @Builder.Default
    private String clinicRole = "RECEPTIONIST";

    @Builder.Default
    private boolean enabled = false;          // becomes true after email verification

    @Builder.Default
    private boolean emailVerified = false;

    private String verificationToken;           // for email verification
    private String refreshToken;               // latest refresh token hash
    private String passwordResetToken;         // for password reset
    private LocalDateTime passwordResetExpiry; // reset token expiry (15 min)

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
