package com.axvorquil.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BillingOrderRequest {
    @NotBlank private String plan;   // STARTER | PRO | ENTERPRISE
    private int seats;               // override seat count (for Enterprise)
}
