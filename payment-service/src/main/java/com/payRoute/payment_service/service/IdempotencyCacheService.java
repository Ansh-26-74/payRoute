package com.payRoute.payment_service.service;

import com.payRoute.payment_service.dto.response.IdempotencyCacheEntry;
import com.payRoute.payment_service.dto.response.PaymentResponse;
import com.payRoute.payment_service.exception.IdempotencyKeyConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdempotencyCacheService {

    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, IdempotencyCacheEntry> redisTemplate;

    public PaymentResponse get(
            UUID merchantId,
            String idempotencyKey,
            String requestHash) {

        String key = buildKey(merchantId, idempotencyKey);

        try {
            IdempotencyCacheEntry cachedEntry =
                    redisTemplate.opsForValue().get(key);

            if (cachedEntry == null) {
                return null;
            }

            if (!cachedEntry.getRequestHash().equals(requestHash)) {
                throw new IdempotencyKeyConflictException(
                        "Idempotency key was already used with a different request"
                );
            }

            return cachedEntry.getResponse();

        } catch (IdempotencyKeyConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            return null;
        }
    }

    public void put(
            UUID merchantId,
            String idempotencyKey,
            String requestHash,
            PaymentResponse response) {

        String key = buildKey(merchantId, idempotencyKey);

        try {
            IdempotencyCacheEntry cacheEntry =
                    IdempotencyCacheEntry.builder()
                            .requestHash(requestHash)
                            .response(response)
                            .build();

            redisTemplate.opsForValue().set(
                    key,
                    cacheEntry,
                    CACHE_TTL
            );

        } catch (Exception ignored) {
        }
    }

    private String buildKey(
            UUID merchantId,
            String idempotencyKey) {

        return "idempotency:v2:"
                + merchantId
                + ":"
                + idempotencyKey;
    }
}