package com.tradingcard.commerce.dao;

import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import com.tradingcard.commerce.dto.CreateCheckoutRequest;
import com.tradingcard.commerce.dto.CreateCheckoutResponse;
import com.tradingcard.commerce.dto.FrontendProperties;
import com.tradingcard.commerce.dto.StripeProperties;
import com.tradingcard.commerce.entities.PaymentEntity;
import com.tradingcard.commerce.processor.IdempotencyService;
import com.tradingcard.commerce.repository.PaymentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Component
public class PaymentEntityDAO {
    private final PaymentRepository paymentRepository;
    private final StripeProperties stripeProperties;
    private final FrontendProperties frontendProperties;
    private final IdempotencyService idempotencyService;

    public PaymentEntityDAO(PaymentRepository paymentRepository, StripeProperties stripeProperties, FrontendProperties frontendProperties, IdempotencyService idempotencyService) {
        this.paymentRepository = paymentRepository;
        this.stripeProperties = stripeProperties;
        this.frontendProperties = frontendProperties;
        this.idempotencyService = idempotencyService;
    }

    public Mono<CreateCheckoutResponse> paymentRepositoryDao(PaymentEntity paymentEntity, String idempotencyKey) {
        String checkoutKey = idempotencyKey + "-checkout";

        return idempotencyService.getCheckoutResponse(checkoutKey)
                .switchIfEmpty(findExistingCheckoutResponse(checkoutKey))
                .switchIfEmpty(createAndCacheCheckoutResponse(paymentEntity, checkoutKey));
    }

    private Mono<CreateCheckoutResponse> findExistingCheckoutResponse(String checkoutKey) {
        return paymentRepository.findByIdempotencyKey(checkoutKey)
                .filter(payment -> payment.getStripeCheckoutSessionId() != null && payment.getStripeCheckoutUrl() != null)
                .map(this::toCheckoutResponse)
                .flatMap(response -> idempotencyService.storeCheckoutResponse(checkoutKey, response));
    }

    private Mono<CreateCheckoutResponse> createAndCacheCheckoutResponse(PaymentEntity paymentEntity, String checkoutKey) {
        paymentEntity.setIdempotencyKey(checkoutKey);

        return paymentRepository.save(paymentEntity)
                .onErrorResume(DataIntegrityViolationException.class, ex -> paymentRepository.findByIdempotencyKey(checkoutKey))
                .flatMap(savedPayment -> createStripeSession(savedPayment, checkoutKey)
                        .flatMap(session -> {
                            savedPayment.setStripeCheckoutSessionId(session.getId());
                            savedPayment.setStripePaymentIntentId(session.getPaymentIntent());
                            savedPayment.setStripeCheckoutUrl(session.getUrl());
                            savedPayment.setUpdatedAt(LocalDateTime.now());

                            return paymentRepository.save(savedPayment)
                                    .map(this::toCheckoutResponse);
                        }))
                .flatMap(response -> idempotencyService.storeCheckoutResponse(checkoutKey, response));
    }

    private CreateCheckoutResponse toCheckoutResponse(PaymentEntity paymentEntity) {
        return new CreateCheckoutResponse(
                paymentEntity.getId(),
                paymentEntity.getStripeCheckoutSessionId(),
                paymentEntity.getStripeCheckoutUrl()
        );
    }

    private Mono<Session> createStripeSession(PaymentEntity paymentEntity, String idempotencyKey) {
        return Mono.fromCallable(() -> {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(frontendProperties.successUrl())
                    .setCancelUrl(frontendProperties.cancelUrl())
                    .putMetadata("paymentId", paymentEntity.getId().toString())
                    .putMetadata("userId", paymentEntity.getUserId())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(paymentEntity.getCurrency())
                                                    .setUnitAmount(paymentEntity.getAmountCents())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(paymentEntity.getProductName())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            RequestOptions.RequestOptionsBuilder requestOptionsBuilder = RequestOptions.builder()
                    .setApiKey(stripeProperties.secretKey());

            if (idempotencyKey != null && !idempotencyKey.isBlank()) {
                requestOptionsBuilder.setIdempotencyKey(idempotencyKey);
            }

            return Session.create(params, requestOptionsBuilder.build());
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
