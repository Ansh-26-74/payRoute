package com.payroute.razorpay_gateway_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RazorpayOrderRequest {
    private int amount;
    private String currency;
    private String receipt;
}