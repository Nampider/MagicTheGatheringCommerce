package com.tradingcard.commerce.processor;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.tradingcard.commerce.entities.PaymentEntity;
import com.tradingcard.commerce.enums.PaymentStatus;
import com.tradingcard.commerce.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class StripeWebhookService {
    private final PaymentRepository paymentRepository;

    public StripeWebhookService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Mono<Void> handleEvent(Event event) {
        return switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
            case "checkout.session.expired" -> handleCheckoutSessionExpired(event);
            case "payment_intent.payment_failed" -> handlePaymentIntentFailed(event);
            default -> Mono.empty();
        };
    }

    private Mono<Void> handleCheckoutSessionCompleted(Event event) {
        Session session = deserialize(event, Session.class);
        if (session == null) {
            return Mono.empty();
        }

        return paymentRepository.findByStripeCheckoutSessionId(session.getId())
                .flatMap(payment -> {
                    payment.setStatus(PaymentStatus.SUCCEEDED.name());
                    payment.setStripePaymentIntentId(session.getPaymentIntent());
                    payment.setUpdatedAt(LocalDateTime.now());
                    return paymentRepository.save(payment);
                })
                .then();
    }

    private Mono<Void> handleCheckoutSessionExpired(Event event) {
        Session session = deserialize(event, Session.class);
        if (session == null) {
            return Mono.empty();
        }

        return updateCheckoutSessionStatus(session.getId(), PaymentStatus.CANCELED);
    }

    private Mono<Void> handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = deserialize(event, PaymentIntent.class);
        if (paymentIntent == null) {
            return Mono.empty();
        }

        return paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                .flatMap(payment -> updatePaymentStatus(payment, PaymentStatus.FAILED))
                .then();
    }

    private Mono<Void> updateCheckoutSessionStatus(String checkoutSessionId, PaymentStatus status) {
        return paymentRepository.findByStripeCheckoutSessionId(checkoutSessionId)
                .flatMap(payment -> updatePaymentStatus(payment, status))
                .then();
    }

    private Mono<PaymentEntity> updatePaymentStatus(PaymentEntity payment, PaymentStatus status) {
        payment.setStatus(status.name());
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    private <T> T deserialize(Event event, Class<T> type) {
        return event.getDataObjectDeserializer()
                .getObject()
                .filter(type::isInstance)
                .map(type::cast)
                .orElse(null);
    }
}
