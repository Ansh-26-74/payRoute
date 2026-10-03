package com.payroute.orchestration_service.client;

import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class SimGatewayClient {

    private final RestClient.Builder restClientBuilder;

    public SimGatewayClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder restClientBuilder) {

        this.restClientBuilder = restClientBuilder;
    }

    public SimGatewayResponse charge(OrchestratePaymentRequest request) {

        return restClientBuilder.build()
                .post()
                .uri("http://sim-gateway-service/charge")
                .body(request)
                .retrieve()
                .body(SimGatewayResponse.class);
    }

    public SimGatewayStatusResponse checkStatus(UUID paymentId) {

        return restClientBuilder.build()
                .get()
                .uri(
                        "http://sim-gateway-service/payments/{paymentId}/status",
                        paymentId
                )
                .retrieve()
                .body(SimGatewayStatusResponse.class);
    }

    public record SimGatewayResponse(
            String status,
            String declineReason
    ) {
    }

    public record SimGatewayStatusResponse(
            String status,
            String declineReason
    ) {
    }
}