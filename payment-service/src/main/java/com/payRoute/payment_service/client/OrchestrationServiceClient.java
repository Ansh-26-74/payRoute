package com.payRoute.payment_service.client;

import com.payRoute.payment_service.dto.request.OrchestratePaymentRequest;
import com.payRoute.payment_service.dto.response.OrchestrationResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OrchestrationServiceClient {

    private final RestClient.Builder restClientBuilder;

    public OrchestrationServiceClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public OrchestrationResponse orchestrate(
            OrchestratePaymentRequest request) {

        try {
            return restClientBuilder
                    .build()
                    .post()
                    .uri("http://orchestration-service/orchestrate")
                    .body(request)
                    .retrieve()
                    .body(OrchestrationResponse.class);

        } catch (IllegalStateException ex) {
            throw new RestClientException(
                    "Orchestration service is unavailable",
                    ex
            );
        }
    }
}