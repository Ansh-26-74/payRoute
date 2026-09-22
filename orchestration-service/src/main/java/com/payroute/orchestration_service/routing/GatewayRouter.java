package com.payroute.orchestration_service.routing;

import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;

public interface GatewayRouter {

    GatewaySelection selectGateway(
            OrchestratePaymentRequest request
    );

    record GatewaySelection(
            String gatewayId,
            boolean recoveryProbe
    ) {}
}