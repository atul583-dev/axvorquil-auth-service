package com.axvorquil.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;      // in seconds
    private String id;           // MongoDB user ObjectId — used by other services as X-User-Id
    private String email;
    private String firstName;
    private String lastName;
    private String role;         // ADMIN | DOCTOR | RECEPTIONIST
}
