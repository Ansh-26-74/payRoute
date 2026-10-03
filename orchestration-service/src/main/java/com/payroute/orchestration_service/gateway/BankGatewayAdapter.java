package com.payroute.orchestration_service.gateway;

import com.payroute.orchestration_service.client.BankGatewayClient;
import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BankGatewayAdapter implements PaymentGateway {

    private final BankGatewayClient bankGatewayClient;

    @Override
    public String gatewayId() {
        return "BANK_GATEWAY";
    }

    @Override
    public GatewayResponse charge(OrchestratePaymentRequest request) {

        BankGatewayClient.BankGatewayResponse response =
                bankGatewayClient.charge(request);

        return new GatewayResponse(
                response.status(),
                response.declineReason()
        );
    }

    @Override
    public GatewayStatusResponse checkStatus(UUID paymentId) {

        BankGatewayClient.BankGatewayStatusResponse response =
                bankGatewayClient.checkStatus(paymentId);

        return new GatewayStatusResponse(
                response.status(),
                response.declineReason()
        );
    }
}