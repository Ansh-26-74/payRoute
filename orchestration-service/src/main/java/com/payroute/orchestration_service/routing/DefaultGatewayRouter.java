package com.payroute.orchestration_service.routing;

import com.payroute.orchestration_service.config.GatewayRoutingProperties;
import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import com.payroute.orchestration_service.gateway.GatewayPerformance;
import com.payroute.orchestration_service.service.GatewayExplorationService;
import com.payroute.orchestration_service.service.GatewayPerformanceCache;
import com.payroute.orchestration_service.service.GatewayRecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DefaultGatewayRouter implements GatewayRouter {

    private final GatewayRoutingProperties gatewayRoutingProperties;
    private final GatewayPerformanceCache gatewayPerformanceCache;
    private final GatewayRecoveryService gatewayRecoveryService;
    private final GatewayExplorationService gatewayExplorationService;

    @Override
    public GatewaySelection selectGateway(
            OrchestratePaymentRequest request) {

        return selectGateway(request, Set.of());
    }

    @Override
    public GatewaySelection selectGateway(
            OrchestratePaymentRequest request,
            Set<String> excludedGateways) {

        List<String> enabledGateways =
                gatewayRoutingProperties.getEnabled()
                        .stream()
                        .filter(gatewayId ->
                                !excludedGateways.contains(gatewayId))
                        .toList();

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
                enabledGateways.stream()
                        .map(gatewayPerformanceCache::getRecentPerformance)
                        .toList();

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
                        true,
                        false
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

        if (healthyGateways.size() > 1) {

            int explorationInterval =
                    gatewayRoutingProperties
                            .getExploration()
                            .getInterval();

            boolean explorationDue =
                    gatewayExplorationService
                            .isExplorationDue(explorationInterval);

            if (explorationDue) {

                String explorationGateway =
                        gatewayExplorationService
                                .getLeastRecentlyExploredGateway(
                                        healthyGateways.stream()
                                                .map(GatewayPerformance::gatewayId)
                                                .toList()
                                );

                if (explorationGateway != null) {
                    return new GatewaySelection(
                            explorationGateway,
                            false,
                            true
                    );
                }
            }
        }

        int minimumAttempts =
                gatewayRoutingProperties.getMinimumAttempts();

        List<GatewayPerformance> gatewaysWithoutEnoughHistory =
                healthyGateways.stream()
                        .filter(performance ->
                                performance.totalAttempts()
                                        < minimumAttempts)
                        .toList();

        if (!gatewaysWithoutEnoughHistory.isEmpty()) {

            long minimumAttemptsCount =
                    gatewaysWithoutEnoughHistory.stream()
                            .mapToLong(GatewayPerformance::totalAttempts)
                            .min()
                            .orElseThrow();

            List<GatewayPerformance> leastTestedGateways =
                    gatewaysWithoutEnoughHistory.stream()
                            .filter(performance ->
                                    performance.totalAttempts()
                                            == minimumAttemptsCount)
                            .toList();

            String selectedGatewayId =
                    gatewayExplorationService
                            .getLeastRecentlySelectedGateway(
                                    leastTestedGateways.stream()
                                            .map(GatewayPerformance::gatewayId)
                                            .toList()
                            );

            gatewayExplorationService.recordGatewaySelection(
                    selectedGatewayId
            );

            return new GatewaySelection(
                    selectedGatewayId,
                    false,
                    false
            );
        }

        String selectedGatewayId =
                healthyGateways.stream()
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
                false,
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