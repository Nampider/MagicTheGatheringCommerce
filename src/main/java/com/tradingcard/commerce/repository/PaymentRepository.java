package com.tradingcard.commerce.repository;

import com.tradingcard.commerce.entities.PaymentEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentRepository extends ReactiveCrudRepository<PaymentEntity, UUID> {
    Mono<PaymentEntity> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    Mono<PaymentEntity> findByStripePaymentIntentId(String stripePaymentIntentId);

    Mono<PaymentEntity> findByIdempotencyKey(String idempotencyKey);
}
