package com.axvorquil.auth.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "billing_transactions")
public class BillingTransaction {

    @Id
    private String id;

    @Indexed
    private String orgId;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private String plan;       // STARTER | PRO | ENTERPRISE
    private double amount;     // in paise (INR) or cents
    private String currency;   // INR | USD

    /** PENDING | SUCCESS | FAILED | REFUNDED */
    @Builder.Default
    private String status = "PENDING";

    /** SUBSCRIPTION | ONE_TIME */
    @Builder.Default
    private String type = "SUBSCRIPTION";

    private String description;
    private String invoiceNumber;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}
