package com.axvorquil.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrgCreateRequest {
    @NotBlank private String name;
    private String type;         // CLINIC | HOSPITAL | PHARMACY | LAB | OTHER
    private String billingEmail;
    private String billingPhone;
    private String address;
    private String city;
    private String country;
}
