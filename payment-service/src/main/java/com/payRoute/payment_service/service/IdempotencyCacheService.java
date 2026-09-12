package com.payRoute.payment_service.service;

import com.payRoute.payment_service.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyCacheService {

    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, PaymentResponse> redisTemplate;

    public PaymentResponse get(
            UUID merchantId,
            String idempotencyKey) {

        String key = buildKey(merchantId, idempotencyKey);

        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception ex) {
            return null;
        }
    }

    public void put(
            UUID merchantId,
            String idempotencyKey,
            PaymentResponse response) {

        String key = buildKey(merchantId, idempotencyKey);

        try {
            redisTemplate.opsForValue().set(
                    key,
                    response,
                    CACHE_TTL
            );
        } catch (Exception ignored) {
        }
    }

    private String buildKey(
            UUID merchantId,
            String idempotencyKey) {

        return "idempotency:"
                + merchantId
                + ":"
                + idempotencyKey;
    }
}