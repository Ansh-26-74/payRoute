package com.payroute.orchestration_service.gateway;

import com.payroute.orchestration_service.client.SimGatewayClient;
import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SimGatewayAdapter implements PaymentGateway {

    private final SimGatewayClient simGatewayClient;

    @Override
    public String gatewayId() {
        return "SIM_GATEWAY";
    }

    @Override
    public GatewayResponse charge(OrchestratePaymentRequest request) {

        SimGatewayClient.SimGatewayResponse response =
                simGatewayClient.charge(request);

        return new GatewayResponse(
                response.status(),
                response.declineReason()
        );
    }
}