package com.tradingcard.commerce.dao;

import com.tradingcard.commerce.dto.CreateCheckoutRequest;
import com.tradingcard.commerce.entities.PaymentEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PaymentEntityMapper {
    public PaymentEntity paymentEntityMapper(CreateCheckoutRequest createCheckoutRequest, String paymentStatus, String userId) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setUserId(userId);
        paymentEntity.setProductName(createCheckoutRequest.productName());
        paymentEntity.setAmountCents(createCheckoutRequest.amountCents());
        paymentEntity.setCurrency(createCheckoutRequest.currency().toLowerCase());
        paymentEntity.setStatus(paymentStatus);
        paymentEntity.setCreatedAt(LocalDateTime.now());
        paymentEntity.setUpdatedAt(LocalDateTime.now());

        return paymentEntity;
    }
}
