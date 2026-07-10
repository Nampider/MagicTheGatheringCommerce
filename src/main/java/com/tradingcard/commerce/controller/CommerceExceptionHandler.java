package com.tradingcard.commerce.controller;

import com.stripe.exception.IdempotencyException;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class CommerceExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleBadRequest(IllegalArgumentException ex) {
        return Mono.just(error(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(IdempotencyException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleStripeIdempotency(IdempotencyException ex) {
        return Mono.just(error(
                HttpStatus.CONFLICT,
                "Idempotency-Key was already used with different checkout parameters. Use a new Idempotency-Key for a different checkout request."
        ));
    }

    @ExceptionHandler(StripeException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleStripe(StripeException ex) {
        return Mono.just(error(HttpStatus.BAD_GATEWAY, "Stripe request failed: " + ex.getMessage()));
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
