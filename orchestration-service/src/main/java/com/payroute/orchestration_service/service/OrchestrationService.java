package com.payroute.orchestration_service.service;

import com.payroute.orchestration_service.config.GatewayRoutingProperties;
import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import com.payroute.orchestration_service.dto.response.OrchestrationResponse;
import com.payroute.orchestration_service.gateway.PaymentGateway;
import com.payroute.orchestration_service.gateway.PaymentGatewayRegistry;
import com.payroute.orchestration_service.routing.GatewayRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OrchestrationService {

    private final PaymentGatewayRegistry paymentGatewayRegistry;
    private final PaymentAttemptService paymentAttemptService;
    private final GatewayRouter gatewayRouter;
    private final GatewayRecoveryService gatewayRecoveryService;
    private final GatewayRoutingProperties gatewayRoutingProperties;

    public OrchestrationResponse orchestrate(
            OrchestratePaymentRequest request) {

        long startTime = System.currentTimeMillis();

        GatewayRouter.GatewaySelection selection =
                gatewayRouter.selectGateway(request);

        String gatewayId = selection.gatewayId();

        PaymentGateway gateway =
                paymentGatewayRegistry.getGateway(gatewayId);

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

        return OrchestrationResponse.builder()
                .paymentId(request.getPaymentId())
                .status(gatewayResponse.status())
                .gatewayUsed(gateway.gatewayId())
                .declineReason(gatewayResponse.declineReason())
                .build();
    }
}