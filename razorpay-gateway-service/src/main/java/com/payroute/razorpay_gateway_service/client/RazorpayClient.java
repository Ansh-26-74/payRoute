package com.payroute.razorpay_gateway_service.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RazorpayClient {

    private final RestClient restClient;

    public RazorpayClient(RestClient razorpayRestClient) {
        this.restClient = razorpayRestClient;
    }

    public String createOrder() {
        return restClient.post()
                .uri("/v1/orders")
                .body("""
                        {
                            "amount": 50000,
                            "currency": "INR",
                            "receipt": "payroute_test_001"
                        }
                        """)
                .retrieve()
                .body(String.class);
    }
}