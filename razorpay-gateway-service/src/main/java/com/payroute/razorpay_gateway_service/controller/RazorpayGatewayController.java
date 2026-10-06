package com.payroute.razorpay_gateway_service.controller;

import com.payroute.razorpay_gateway_service.service.RazorpayGatewayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RazorpayGatewayController {

    private final RazorpayGatewayService razorpayGatewayService;

    @GetMapping("/test/razorpay")
    public String testRazorpay() {
        return razorpayGatewayService.createTestOrder();
    }
}