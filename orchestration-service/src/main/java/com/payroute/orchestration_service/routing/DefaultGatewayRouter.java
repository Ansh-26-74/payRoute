package com.payroute.orchestration_service.routing;

import com.payroute.orchestration_service.config.GatewayRoutingProperties;
import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import com.payroute.orchestration_service.gateway.GatewayPerformance;
import com.payroute.orchestration_service.service.GatewayPerformanceService;
import com.payroute.orchestration_service.service.GatewayRecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultGatewayRouter implements GatewayRouter {

    private final GatewayRoutingProperties gatewayRoutingProperties;
    private final GatewayPerformanceService gatewayPerformanceService;
    private final GatewayRecoveryService gatewayRecoveryService;

    @Override
    public GatewaySelection selectGateway(
            OrchestratePaymentRequest request) {

        List<String> enabledGateways =
                gatewayRoutingProperties.getEnabled();

        if (enabledGateways.isEmpty()) {
            throw new IllegalStateException(
                    "No payment gateways are enabled"
            );
        }

        double minimumSuccessRate =
                gatewayRoutingProperties.getMinimumSuccessRate();

        int probeAttempts =
                gatewayRoutingProperties
                        .getRecovery()
                        .getProbeAttempts();

        List<GatewayPerformance> performances =
                gatewayPerformanceService
                        .getPerformanceForGateways(enabledGateways);

        for (GatewayPerformance performance : performances) {

            if (gatewayRecoveryService.isInCooldown(
                    performance.gatewayId())) {
                continue;
            }

            if (isEligible(
                    performance,
                    minimumSuccessRate)) {
                continue;
            }

            boolean probeReserved =
                    gatewayRecoveryService.tryReserveProbeAttempt(
                            performance.gatewayId(),
                            probeAttempts
                    );

            if (probeReserved) {
                return new GatewaySelection(
                        performance.gatewayId(),
                        true
                );
            }
        }

        List<GatewayPerformance> healthyGateways =
                performances.stream()
                        .filter(performance ->
                                !gatewayRecoveryService.isInCooldown(
                                        performance.gatewayId()))
                        .filter(performance ->
                                isEligible(
                                        performance,
                                        minimumSuccessRate))
                        .toList();

        if (healthyGateways.isEmpty()) {
            throw new IllegalStateException(
                    "No eligible payment gateways available"
            );
        }

        List<GatewayPerformance> gatewaysWithEnoughHistory =
                healthyGateways.stream()
                        .filter(performance ->
                                performance.totalAttempts()
                                        >= gatewayRoutingProperties
                                        .getMinimumAttempts())
                        .toList();

        if (!gatewaysWithEnoughHistory.isEmpty()) {

            String selectedGatewayId =
                    gatewaysWithEnoughHistory.stream()
                            .min((first, second) ->
                                    Double.compare(
                                            first.averageLatencyMs()
                                                    .orElse(Double.MAX_VALUE),
                                            second.averageLatencyMs()
                                                    .orElse(Double.MAX_VALUE)
                                    ))
                            .map(GatewayPerformance::gatewayId)
                            .orElseThrow();

            return new GatewaySelection(
                    selectedGatewayId,
                    false
            );
        }

        String selectedGatewayId =
                healthyGateways.stream()
                        .min((first, second) ->
                                Long.compare(
                                        first.totalAttempts(),
                                        second.totalAttempts()
                                ))
                        .map(GatewayPerformance::gatewayId)
                        .orElseThrow();

        return new GatewaySelection(
                selectedGatewayId,
                false
        );
    }

    private boolean isEligible(
            GatewayPerformance performance,
            double minimumSuccessRate) {

        if (performance.successRate().isEmpty()) {
            return true;
        }

        return performance.successRate().getAsDouble()
                >= minimumSuccessRate;
    }
}