package com.axvorquil.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteMemberRequest {
    @NotBlank @Email private String email;
    @NotBlank        private String firstName;
                     private String lastName;
    @NotBlank        private String role;  // ADMIN | DOCTOR | RECEPTIONIST
}
