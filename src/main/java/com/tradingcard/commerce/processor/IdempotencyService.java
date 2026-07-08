package com.tradingcard.commerce.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingcard.commerce.dto.CreateCheckoutResponse;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class IdempotencyService {
    private static final String PREFIX = "commerce:payment:idempotency:";
    private static final Duration RESPONSE_TTL = Duration.ofHours(24);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IdempotencyService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<CreateCheckoutResponse> getCheckoutResponse(String key) {
        return redisTemplate.opsForValue()
                .get(PREFIX + key)
                .flatMap(this::readCheckoutResponse);
    }

    public Mono<CreateCheckoutResponse> storeCheckoutResponse(String key, CreateCheckoutResponse response) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(response))
                .flatMap(json -> redisTemplate.opsForValue().set(PREFIX + key, json, RESPONSE_TTL))
                .thenReturn(response);
    }

    private Mono<CreateCheckoutResponse> readCheckoutResponse(String json) {
        try {
            return Mono.just(objectMapper.readValue(json, CreateCheckoutResponse.class));
        } catch (JsonProcessingException e) {
            return Mono.error(new IllegalStateException("Unable to read cached checkout response", e));
        }
    }
}
