package com.payroute.orchestration_service.gateway;

import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;

public interface PaymentGateway {

    GatewayResponse charge(OrchestratePaymentRequest request);

    record GatewayResponse(
            String status,
            String declineReason
    ) {
    }
}