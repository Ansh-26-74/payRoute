package com.payroute.orchestration_service.gateway;

import java.util.OptionalDouble;

public record GatewayPerformance(
        String gatewayId,
        long totalAttempts,
        OptionalDouble successRate,
        OptionalDouble averageLatencyMs
) {
}