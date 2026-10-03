package com.payroute.orchestration_service.client;

import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class BankGatewayClient {

    private final RestClient.Builder restClientBuilder;

    public BankGatewayClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder restClientBuilder) {

        this.restClientBuilder = restClientBuilder;
    }

    public BankGatewayResponse charge(
            OrchestratePaymentRequest request) {

        return restClientBuilder.build()
                .post()
                .uri("http://bank-gateway-service/charge")
                .body(request)
                .retrieve()
                .body(BankGatewayResponse.class);
    }

    public BankGatewayStatusResponse checkStatus(UUID paymentId) {

        return restClientBuilder.build()
                .get()
                .uri(
                        "http://bank-gateway-service/payments/{paymentId}/status",
                        paymentId
                )
                .retrieve()
                .body(BankGatewayStatusResponse.class);
    }

    public record BankGatewayResponse(
            String status,
            String declineReason
    ) {
    }

    public record BankGatewayStatusResponse(
            String status,
            String declineReason
    ) {
    }
}