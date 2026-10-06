package com.payroute.razorpay_gateway_service.service;

import com.payroute.razorpay_gateway_service.client.RazorpayClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RazorpayGatewayService {

    private final RazorpayClient razorpayClient;

    public String createTestOrder() {
        return razorpayClient.createOrder();
    }
}