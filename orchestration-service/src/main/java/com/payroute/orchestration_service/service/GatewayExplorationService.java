package com.payroute.orchestration_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GatewayExplorationService {

    private static final String ROUTING_COUNTER_KEY =
            "gateway-exploration:routing-counter";

    private static final String LAST_EXPLORED_KEY =
            "gateway-exploration:last-explored";

    private static final String LAST_SELECTED_KEY =
            "gateway-routing:last-selected";

    private final StringRedisTemplate redisTemplate;

    public boolean isExplorationDue(int interval) {

        Long count =
                redisTemplate.opsForValue()
                        .increment(ROUTING_COUNTER_KEY);

        return count != null && count % interval == 0;
    }

    public String getLeastRecentlyExploredGateway(
            List<String> gatewayIds) {

        if (gatewayIds.isEmpty()) {
            return null;
        }

        for (String gatewayId : gatewayIds) {

            Double score =
                    redisTemplate.opsForZSet()
                            .score(
                                    LAST_EXPLORED_KEY,
                                    gatewayId
                            );

            if (score == null) {
                return gatewayId;
            }
        }

        Set<String> leastRecentlyExplored =
                redisTemplate.opsForZSet()
                        .range(
                                LAST_EXPLORED_KEY,
                                0,
                                0
                        );

        if (leastRecentlyExplored == null ||
                leastRecentlyExplored.isEmpty()) {

            return gatewayIds.get(0);
        }

        String gatewayId =
                leastRecentlyExplored.iterator().next();

        if (gatewayIds.contains(gatewayId)) {
            return gatewayId;
        }

        return gatewayIds.stream()
                .min((first, second) ->
                        Double.compare(
                                getLastExploredTime(first),
                                getLastExploredTime(second)
                        ))
                .orElse(null);
    }

    public String getLeastRecentlySelectedGateway(
            List<String> gatewayIds) {

        if (gatewayIds.isEmpty()) {
            return null;
        }

        for (String gatewayId : gatewayIds) {

            Double score =
                    redisTemplate.opsForZSet()
                            .score(
                                    LAST_SELECTED_KEY,
                                    gatewayId
                            );

            if (score == null) {
                return gatewayId;
            }
        }

        Set<String> leastRecentlySelected =
                redisTemplate.opsForZSet()
                        .range(
                                LAST_SELECTED_KEY,
                                0,
                                0
                        );

        if (leastRecentlySelected == null ||
                leastRecentlySelected.isEmpty()) {

            return gatewayIds.get(0);
        }

        String gatewayId =
                leastRecentlySelected.iterator().next();

        if (gatewayIds.contains(gatewayId)) {
            return gatewayId;
        }

        return gatewayIds.stream()
                .min((first, second) ->
                        Double.compare(
                                getLastSelectedTime(first),
                                getLastSelectedTime(second)
                        ))
                .orElse(null);
    }

    public void recordExploration(String gatewayId) {

        redisTemplate.opsForZSet()
                .add(
                        LAST_EXPLORED_KEY,
                        gatewayId,
                        System.currentTimeMillis()
                );
    }

    public void recordGatewaySelection(String gatewayId) {

        redisTemplate.opsForZSet()
                .add(
                        LAST_SELECTED_KEY,
                        gatewayId,
                        System.currentTimeMillis()
                );
    }

    private double getLastExploredTime(String gatewayId) {

        Double score =
                redisTemplate.opsForZSet()
                        .score(
                                LAST_EXPLORED_KEY,
                                gatewayId
                        );

        return Objects.requireNonNullElse(
                score,
                0.0
        );
    }

    private double getLastSelectedTime(String gatewayId) {

        Double score =
                redisTemplate.opsForZSet()
                        .score(
                                LAST_SELECTED_KEY,
                                gatewayId
                        );

        return Objects.requireNonNullElse(
                score,
                0.0
        );
    }
}