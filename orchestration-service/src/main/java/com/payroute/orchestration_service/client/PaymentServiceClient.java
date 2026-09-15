package com.payroute.orchestration_service.client;

import com.payroute.orchestration_service.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class PaymentServiceClient {

    private final RestClient.Builder restClientBuilder;

    public PaymentServiceClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder restClientBuilder) {

        this.restClientBuilder = restClientBuilder;
    }

    public PaymentResponse getPayment(UUID paymentId, UUID merchantId) {
        return restClientBuilder
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("payment-service")
                        .path("/payments/{paymentId}")
                        .queryParam("merchantId", merchantId)
                        .build(paymentId))
                .retrieve()
                .body(PaymentResponse.class);
    }
}