package com.tradingcard.commerce.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Table("payments")
public class PaymentEntity {
    @Id
    private UUID id;

    private String userId;

    private String productName;

    private Long amountCents;

    private String currency;

    private String status;

    private String stripeCheckoutSessionId;

    private String stripePaymentIntentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
