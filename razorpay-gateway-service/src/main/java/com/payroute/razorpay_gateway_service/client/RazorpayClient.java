package com.payroute.razorpay_gateway_service.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RazorpayClient {

    private final RestClient restClient;

    public RazorpayClient(RestClient razorpayRestClient) {
        this.restClient = razorpayRestClient;
    }
}