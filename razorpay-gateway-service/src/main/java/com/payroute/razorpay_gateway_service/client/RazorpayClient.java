package com.payroute.razorpay_gateway_service.client;

import com.payroute.razorpay_gateway_service.dto.request.RazorpayOrderRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RazorpayClient {

    private final RestClient restClient;

    public RazorpayClient(RestClient razorpayRestClient) {
        this.restClient = razorpayRestClient;
    }

    public String createOrder(RazorpayOrderRequest request) {
        return restClient.post()
                .uri("/v1/orders")
                .body(request)
                .retrieve()
                .body(String.class);
    }
}