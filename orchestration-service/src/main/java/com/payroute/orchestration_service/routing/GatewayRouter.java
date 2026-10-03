package com.payroute.orchestration_service.routing;

import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;

import java.util.Set;

public interface GatewayRouter {

    GatewaySelection selectGateway(
            OrchestratePaymentRequest request
    );

    GatewaySelection selectGateway(
            OrchestratePaymentRequest request,
            Set<String> excludedGateways
    );

    record GatewaySelection(
            String gatewayId,
            boolean recoveryProbe,
            boolean exploration
    ) {}
}