package com.payroute.orchestration_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GatewayRecoveryService {

    private static final String COOLDOWN_PREFIX =
            "gateway-recovery:";

    private static final String PROBE_ATTEMPTS_PREFIX =
            "gateway-probe:attempts:";

    private static final String PROBE_SUCCESSES_PREFIX =
            "gateway-probe:successes:";

    private final StringRedisTemplate redisTemplate;

    public void startCooldown(
            String gatewayId,
            Duration cooldown) {

        redisTemplate.opsForValue().set(
                COOLDOWN_PREFIX + gatewayId,
                "COOLDOWN",
                cooldown
        );

        clearProbeState(gatewayId);
    }

    public boolean isInCooldown(String gatewayId) {

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(
                        COOLDOWN_PREFIX + gatewayId
                )
        );
    }

    public void recordProbeSuccess(String gatewayId) {

        redisTemplate.opsForValue().increment(
                PROBE_SUCCESSES_PREFIX + gatewayId
        );
    }

    public long getProbeAttempts(String gatewayId) {

        String value = redisTemplate.opsForValue().get(
                PROBE_ATTEMPTS_PREFIX + gatewayId
        );

        return value == null ? 0 : Long.parseLong(value);
    }

    public long getProbeSuccesses(String gatewayId) {

        String value = redisTemplate.opsForValue().get(
                PROBE_SUCCESSES_PREFIX + gatewayId
        );

        return value == null ? 0 : Long.parseLong(value);
    }

    public void clearProbeState(String gatewayId) {

        redisTemplate.delete(
                PROBE_ATTEMPTS_PREFIX + gatewayId
        );

        redisTemplate.delete(
                PROBE_SUCCESSES_PREFIX + gatewayId
        );
    }

    public boolean tryReserveProbeAttempt(
            String gatewayId,
            int maxAttempts) {

        String script = """
            local current = redis.call('GET', KEYS[1])

            if not current then
                current = 0
            else
                current = tonumber(current)
            end

            if current >= tonumber(ARGV[1]) then
                return 0
            end

            redis.call('INCR', KEYS[1])
            return 1
            """;

        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                List.of(PROBE_ATTEMPTS_PREFIX + gatewayId),
                String.valueOf(maxAttempts)
        );

        return Long.valueOf(1).equals(result);
    }

    public void completeProbe(
            String gatewayId,
            boolean success,
            double minimumSuccessRate,
            int probeAttempts,
            Duration cooldown) {

        if (success) {
            recordProbeSuccess(gatewayId);
        }

        long attempts = getProbeAttempts(gatewayId);

        if (attempts < probeAttempts) {
            return;
        }

        long successes = getProbeSuccesses(gatewayId);

        double successRate =
                (double) successes / attempts;

        if (successRate >= minimumSuccessRate) {
            clearProbeState(gatewayId);
            return;
        }

        startCooldown(gatewayId, cooldown);
    }
}