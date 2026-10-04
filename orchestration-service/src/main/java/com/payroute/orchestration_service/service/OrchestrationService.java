package com.payroute.orchestration_service.service;

import com.payroute.orchestration_service.config.GatewayRoutingProperties;
import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import com.payroute.orchestration_service.dto.response.OrchestrationResponse;
import com.payroute.orchestration_service.gateway.PaymentGateway;
import com.payroute.orchestration_service.gateway.PaymentGatewayRegistry;
import com.payroute.orchestration_service.routing.GatewayRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrchestrationService {

    private final PaymentGatewayRegistry paymentGatewayRegistry;
    private final PaymentAttemptService paymentAttemptService;
    private final GatewayRouter gatewayRouter;
    private final GatewayRecoveryService gatewayRecoveryService;
    private final GatewayRoutingProperties gatewayRoutingProperties;
    private final GatewayExplorationService gatewayExplorationService;

    public OrchestrationResponse orchestrate(
            OrchestratePaymentRequest request) {

        Set<String> excludedGateways = new HashSet<>();

        for (int attempt = 0; attempt < 2; attempt++) {

            GatewayRouter.GatewaySelection selection =
                    gatewayRouter.selectGateway(
                            request,
                            excludedGateways
                    );

            String gatewayId = selection.gatewayId();

            PaymentGateway gateway =
                    paymentGatewayRegistry.getGateway(gatewayId);

            long startTime = System.currentTimeMillis();

            try {

                PaymentGateway.GatewayResponse gatewayResponse =
                        gateway.charge(request);

                long latencyMs =
                        System.currentTimeMillis() - startTime;

                paymentAttemptService.recordAttempt(
                        request.getPaymentId(),
                        gateway.gatewayId(),
                        gatewayResponse.status(),
                        gatewayResponse.declineReason(),
                        latencyMs
                );

                if (selection.recoveryProbe()) {

                    boolean success =
                            "SUCCESS".equalsIgnoreCase(
                                    gatewayResponse.status());

                    gatewayRecoveryService.completeProbe(
                            gatewayId,
                            success,
                            gatewayRoutingProperties
                                    .getMinimumSuccessRate(),
                            gatewayRoutingProperties
                                    .getRecovery()
                                    .getProbeAttempts(),
                            Duration.ofSeconds(
                                    gatewayRoutingProperties
                                            .getRecovery()
                                            .getCooldownSeconds()
                            )
                    );
                }

                if (selection.exploration()) {
                    gatewayExplorationService.recordExploration(
                            gatewayId
                    );
                }

                return OrchestrationResponse.builder()
                        .paymentId(request.getPaymentId())
                        .status(gatewayResponse.status())
                        .gatewayUsed(gateway.gatewayId())
                        .declineReason(gatewayResponse.declineReason())
                        .build();

            } catch (RestClientException ex) {

                long latencyMs =
                        System.currentTimeMillis() - startTime;

                paymentAttemptService.recordAttempt(
                        request.getPaymentId(),
                        gateway.gatewayId(),
                        "TIMEOUT",
                        "Gateway timeout or connection failure",
                        latencyMs
                );

                if (selection.recoveryProbe()) {

                    gatewayRecoveryService.completeProbe(
                            gatewayId,
                            false,
                            gatewayRoutingProperties.getMinimumSuccessRate(),
                            gatewayRoutingProperties.getRecovery().getProbeAttempts(),
                            Duration.ofSeconds(
                                    gatewayRoutingProperties.getRecovery().getCooldownSeconds()
                            )
                    );
                }

                if (selection.exploration()) {
                    gatewayExplorationService.recordExploration(gatewayId);
                }

                try {

                    PaymentGateway.GatewayStatusResponse statusResponse =
                            gateway.checkStatus(
                                    request.getPaymentId()
                            );

                    String status =
                            statusResponse.status();

                    if ("SUCCESS".equalsIgnoreCase(status) ||
                            "PROCESSING".equalsIgnoreCase(status)) {

                        return OrchestrationResponse.builder()
                                .paymentId(request.getPaymentId())
                                .status(status)
                                .gatewayUsed(gateway.gatewayId())
                                .declineReason(
                                        statusResponse.declineReason()
                                )
                                .build();
                    }

                    if ("FAILED".equalsIgnoreCase(status) ||
                            "DECLINED".equalsIgnoreCase(status) ||
                            "NOT_FOUND".equalsIgnoreCase(status)) {

                        excludedGateways.add(gatewayId);
                        continue;
                    }

                    return OrchestrationResponse.builder()
                            .paymentId(request.getPaymentId())
                            .status("PROCESSING")
                            .gatewayUsed(gateway.gatewayId())
                            .declineReason(null)
                            .build();

                } catch (RestClientException statusException) {

                    return OrchestrationResponse.builder()
                            .paymentId(request.getPaymentId())
                            .status("PROCESSING")
                            .gatewayUsed(gateway.gatewayId())
                            .declineReason(null)
                            .build();
                }
            }
        }

        return OrchestrationResponse.builder()
                .paymentId(request.getPaymentId())
                .status("FAILED")
                .gatewayUsed(null)
                .declineReason("All payment gateways failed")
                .build();
    }
}