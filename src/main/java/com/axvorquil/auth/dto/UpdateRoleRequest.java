package com.axvorquil.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateRoleRequest {

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "ADMIN|DOCTOR|RECEPTIONIST",
             message = "Role must be ADMIN, DOCTOR, or RECEPTIONIST")
    private String role;
}
