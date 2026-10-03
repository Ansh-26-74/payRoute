package com.payroute.orchestration_service.gateway;

import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;

import java.util.UUID;

public interface PaymentGateway {

    GatewayResponse charge(OrchestratePaymentRequest request);

    GatewayStatusResponse checkStatus(UUID paymentId);

    String gatewayId();

    record GatewayResponse(
            String status,
            String declineReason
    ) {
    }

    record GatewayStatusResponse(
            String status,
            String declineReason
    ) {
    }
}