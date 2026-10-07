package com.payroute.razorpay_gateway_service.service;

import com.payroute.razorpay_gateway_service.client.RazorpayClient;
import com.payroute.razorpay_gateway_service.dto.request.RazorpayOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RazorpayGatewayService {

    private final RazorpayClient razorpayClient;

    public String createTestOrder() {
        RazorpayOrderRequest request =
                new RazorpayOrderRequest(50000, "INR", "payroute_test_002");

        return razorpayClient.createOrder(request);
    }
}