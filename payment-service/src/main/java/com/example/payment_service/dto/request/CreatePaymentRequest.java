package com.example.payment_service.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreatePaymentRequest {

    private UUID merchantId;

    private String orderId;

    private BigDecimal amount;

    private String currency;
}