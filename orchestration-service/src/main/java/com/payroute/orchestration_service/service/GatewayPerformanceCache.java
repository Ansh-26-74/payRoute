package com.payroute.orchestration_service.service;

import com.payroute.orchestration_service.gateway.GatewayPerformance;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
public class GatewayPerformanceCache {

    private static final String KEY_PREFIX =
            "gateway-performance:";

    private static final int WINDOW_SIZE = 100;

    private static final long TTL_SECONDS = 3600;

    private final StringRedisTemplate redisTemplate;

    public void recordAttempt(
            String gatewayId,
            String status,
            Long latencyMs) {

        String key = KEY_PREFIX + gatewayId;

        String value = status + "|" + latencyMs;

        String script = """
                redis.call('RPUSH', KEYS[1], ARGV[1])
                redis.call('LTRIM', KEYS[1], -100, -1)
                redis.call('EXPIRE', KEYS[1], 3600)
                return 1
                """;

        redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                List.of(key),
                value
        );
    }

    public GatewayPerformance getRecentPerformance(
            String gatewayId) {

        String key = KEY_PREFIX + gatewayId;

        List<String> attempts =
                redisTemplate.opsForList().range(
                        key,
                        0,
                        -1
                );

        if (attempts == null || attempts.isEmpty()) {
            return new GatewayPerformance(
                    gatewayId,
                    0,
                    OptionalDouble.empty(),
                    OptionalDouble.empty()
            );
        }

        long successfulAttempts = 0;
        long totalLatency = 0;

        for (String attempt : attempts) {

            String[] parts = attempt.split("\\|");

            String status = parts[0];
            long latency = Long.parseLong(parts[1]);

            if ("SUCCESS".equals(status)) {
                successfulAttempts++;
            }

            totalLatency += latency;
        }

        int totalAttempts = attempts.size();

        double successRate =
                (double) successfulAttempts / totalAttempts;

        double averageLatency =
                (double) totalLatency / totalAttempts;

        return new GatewayPerformance(
                gatewayId,
                totalAttempts,
                OptionalDouble.of(successRate),
                OptionalDouble.of(averageLatency)
        );
    }
}