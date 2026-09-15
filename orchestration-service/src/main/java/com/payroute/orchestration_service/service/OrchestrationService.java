package com.payroute.orchestration_service.service;

import com.payroute.orchestration_service.client.SimGatewayClient;
import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import com.payroute.orchestration_service.dto.response.OrchestrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrchestrationService {

    private final SimGatewayClient simGatewayClient;

    public OrchestrationResponse orchestrate(OrchestratePaymentRequest request) {

        SimGatewayClient.SimGatewayResponse gatewayResponse =
                simGatewayClient.charge(request);

        return OrchestrationResponse.builder()
                .status(gatewayResponse.status())
                .gatewayUsed("SIM_GATEWAY")
                .declineReason(gatewayResponse.declineReason())
                .build();
    }
}