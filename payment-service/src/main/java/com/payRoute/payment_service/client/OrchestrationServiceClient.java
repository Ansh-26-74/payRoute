package com.payRoute.payment_service.client;

import com.payRoute.payment_service.dto.request.OrchestratePaymentRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OrchestrationServiceClient {

    private final RestClient.Builder restClientBuilder;

    public OrchestrationServiceClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public void orchestrate(OrchestratePaymentRequest request) {
        restClientBuilder
                .build()
                .post()
                .uri("http://orchestration-service/orchestrate")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}