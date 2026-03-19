package com.axvorquil.auth.dto;

import lombok.Data;

@Data
public class BillingVerifyRequest {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private String plan;
}
