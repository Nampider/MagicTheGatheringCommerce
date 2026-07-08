package com.tradingcard.commerce.processor;

import com.tradingcard.commerce.dao.PaymentEntityDAO;
import com.tradingcard.commerce.dao.PaymentEntityMapper;
import com.tradingcard.commerce.dto.CreateCheckoutRequest;
import com.tradingcard.commerce.dto.CreateCheckoutResponse;
import com.tradingcard.commerce.dto.FrontendProperties;
import com.tradingcard.commerce.dto.StripeProperties;
import com.tradingcard.commerce.entities.PaymentEntity;
import com.tradingcard.commerce.enums.PaymentStatus;
import com.tradingcard.commerce.repository.PaymentRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class StripeCheckoutProcessor {

    private final PaymentEntityMapper paymentEntityMapper;

    private final PaymentEntityDAO paymentEntityDAO;

    public StripeCheckoutProcessor(PaymentEntityMapper paymentEntityMapper, PaymentEntityDAO paymentEntityDAO) {
        this.paymentEntityMapper = paymentEntityMapper;
        this.paymentEntityDAO = paymentEntityDAO;
    }

    public Mono<CreateCheckoutResponse> createCheckoutSession(String userId, CreateCheckoutRequest request, String idempotencyKey) {
        validateRequest(request);
        validateIdempotencyKey(idempotencyKey);
        PaymentEntity payment = paymentEntityMapper.paymentEntityMapper(request, PaymentStatus.PENDING.name(), userId);
        return paymentEntityDAO.paymentRepositoryDao(payment, idempotencyKey);
    }

    private void validateRequest(CreateCheckoutRequest request) {
        if (request.productName() == null || request.productName().isBlank()) {
            throw new IllegalArgumentException("productName is required");
        }

        if (request.amountCents() == null || request.amountCents() <= 0) {
            throw new IllegalArgumentException("amountCents must be greater than 0");
        }

        if (request.currency() == null || request.currency().isBlank()) {
            throw new IllegalArgumentException("currency is required");
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }
    }
}
