package com.tradingcard.commerce.controller;

import com.tradingcard.commerce.dto.CreateCheckoutRequest;
import com.tradingcard.commerce.dto.CreateCheckoutResponse;
import com.tradingcard.commerce.processor.StripeCheckoutProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payment")
@Slf4j
public class CommerceController {

    private final StripeCheckoutProcessor stripeCheckoutProcessor;

    public CommerceController(StripeCheckoutProcessor stripeCheckoutProcessor) {
        this.stripeCheckoutProcessor = stripeCheckoutProcessor;
    }

    @PostMapping("/checkout")
    public Mono<ResponseEntity<CreateCheckoutResponse>> createCheckout(
            @RequestBody CreateCheckoutRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();

        return stripeCheckoutProcessor.createCheckoutSession(userId, request, idempotencyKey)
                .map(ResponseEntity::ok);
    }
}
