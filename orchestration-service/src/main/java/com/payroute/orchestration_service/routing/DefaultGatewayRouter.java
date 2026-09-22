package com.payroute.orchestration_service.routing;

import com.payroute.orchestration_service.config.GatewayRoutingProperties;
import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import com.payroute.orchestration_service.gateway.GatewayPerformance;
import com.payroute.orchestration_service.service.GatewayPerformanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultGatewayRouter implements GatewayRouter {

    private final GatewayRoutingProperties gatewayRoutingProperties;
    private final GatewayPerformanceService gatewayPerformanceService;

    @Override
    public String selectGateway(OrchestratePaymentRequest request) {

        List<String> enabledGateways =
                gatewayRoutingProperties.getEnabled();

        if (enabledGateways.isEmpty()) {
            throw new IllegalStateException(
                    "No payment gateways are enabled"
            );
        }

        double minimumSuccessRate =
                gatewayRoutingProperties.getMinimumSuccessRate();

        List<GatewayPerformance> eligibleGateways =
                gatewayPerformanceService
                        .getPerformanceForGateways(enabledGateways)
                        .stream()
                        .filter(performance -> isEligible(
                                performance,
                                minimumSuccessRate
                        ))
                        .toList();

        if (eligibleGateways.isEmpty()) {
            throw new IllegalStateException(
                    "No eligible payment gateways available"
            );
        }

        List<GatewayPerformance> gatewaysWithEnoughHistory =
                eligibleGateways.stream()
                        .filter(performance ->
                                performance.totalAttempts()
                                        >= gatewayRoutingProperties
                                        .getMinimumAttempts())
                        .toList();

        if (!gatewaysWithEnoughHistory.isEmpty()) {
            return gatewaysWithEnoughHistory.stream()
                    .min((first, second) ->
                            Double.compare(
                                    first.averageLatencyMs().orElse(Double.MAX_VALUE),
                                    second.averageLatencyMs().orElse(Double.MAX_VALUE)
                            ))
                    .map(GatewayPerformance::gatewayId)
                    .orElseThrow();
        }

        return eligibleGateways.stream()
                .min((first, second) ->
                        Long.compare(
                                first.totalAttempts(),
                                second.totalAttempts()
                        ))
                .map(GatewayPerformance::gatewayId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No eligible payment gateways available"
                        ));
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