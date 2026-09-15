package com.payroute.orchestration_service.service;

import com.payroute.orchestration_service.client.PaymentServiceClient;
import com.payroute.orchestration_service.client.SimGatewayClient;
import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import com.payroute.orchestration_service.dto.response.OrchestrationResponse;
import com.payroute.orchestration_service.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrchestrationService {

    private final SimGatewayClient simGatewayClient;
    private final PaymentAttemptService paymentAttemptService;
    private final PaymentServiceClient paymentServiceClient;

    public OrchestrationResponse orchestrate(OrchestratePaymentRequest request) {

        PaymentResponse payment =
                paymentServiceClient.getPayment(
                        request.getPaymentId(),
                        request.getMerchantId()
                );

        long startTime = System.currentTimeMillis();

        SimGatewayClient.SimGatewayResponse gatewayResponse =
                simGatewayClient.charge(request);

        long latencyMs = System.currentTimeMillis() - startTime;

        paymentAttemptService.recordAttempt(
                payment.getPaymentId(),
                "SIM_GATEWAY",
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