package com.tradingcard.commerce.controller;

import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.tradingcard.commerce.dto.StripeProperties;
import com.tradingcard.commerce.processor.StripeWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payment/webhook")
public class StripeWebhookController {
    private final StripeProperties stripeProperties;
    private final StripeWebhookService stripeWebhookService;

    public StripeWebhookController(StripeProperties stripeProperties, StripeWebhookService stripeWebhookService) {
        this.stripeProperties = stripeProperties;
        this.stripeWebhookService = stripeWebhookService;
    }

    @PostMapping("/stripe")
    public Mono<ResponseEntity<String>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, signature, stripeProperties.webhookSecret());
        } catch (Exception e) {
            return Mono.just(ResponseEntity.badRequest().body("Invalid Stripe webhook signature"));
        }

        return stripeWebhookService.handleEvent(event)
                .thenReturn(ResponseEntity.ok("ok"));
    }
}
