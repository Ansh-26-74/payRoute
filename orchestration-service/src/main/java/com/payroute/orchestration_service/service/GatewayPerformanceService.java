package com.payroute.orchestration_service.service;

import com.payroute.orchestration_service.gateway.GatewayPerformance;
import com.payroute.orchestration_service.repository.PaymentAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.OptionalDouble;

@Service
@RequiredArgsConstructor
public class GatewayPerformanceService {

    private final PaymentAttemptRepository paymentAttemptRepository;

    public OptionalDouble getSuccessRate(String gatewayId) {

        long totalAttempts =
                paymentAttemptRepository.countByGatewayId(gatewayId);

        if (totalAttempts == 0) {
            return OptionalDouble.empty();
        }

        long successfulAttempts =
                paymentAttemptRepository
                        .countByGatewayIdAndStatus(
                                gatewayId,
                                "SUCCESS"
                        );

        return OptionalDouble.of(
                (double) successfulAttempts / totalAttempts
        );
    }

    public OptionalDouble getAverageLatency(String gatewayId) {

        Double averageLatency =
                paymentAttemptRepository
                        .findAverageLatencyByGatewayId(gatewayId);

        if (averageLatency == null) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(averageLatency);
    }

    public GatewayPerformance getPerformance(String gatewayId) {

        long totalAttempts =
                paymentAttemptRepository.countByGatewayId(gatewayId);

        return new GatewayPerformance(
                gatewayId,
                totalAttempts,
                getSuccessRate(gatewayId),
                getAverageLatency(gatewayId)
        );
    }

    public List<GatewayPerformance> getPerformanceForGateways(
            List<String> gatewayIds) {

        return gatewayIds.stream()
                .map(this::getPerformance)
                .toList();
    }
}