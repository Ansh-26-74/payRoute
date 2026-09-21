package com.payroute.orchestration_service.service;

import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import com.payroute.orchestration_service.dto.response.OrchestrationResponse;
import com.payroute.orchestration_service.gateway.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrchestrationService {

    private final PaymentGateway paymentGateway;
    private final PaymentAttemptService paymentAttemptService;

    public OrchestrationResponse orchestrate(
            OrchestratePaymentRequest request) {

        long startTime = System.currentTimeMillis();

        PaymentGateway.GatewayResponse gatewayResponse =
                paymentGateway.charge(request);

        long latencyMs = System.currentTimeMillis() - startTime;

        paymentAttemptService.recordAttempt(
                request.getPaymentId(),
                paymentGateway.gatewayId(),
                gatewayResponse.status(),
                gatewayResponse.declineReason(),
                latencyMs
        );

        return OrchestrationResponse.builder()
                .paymentId(request.getPaymentId())
                .status(gatewayResponse.status())
                .gatewayUsed("SIM_GATEWAY")
                .declineReason(gatewayResponse.declineReason())
                .build();
    }
}