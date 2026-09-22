package com.payroute.orchestration_service.routing;

import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;

public interface GatewayRouter {

    String selectGateway(OrchestratePaymentRequest request);
}