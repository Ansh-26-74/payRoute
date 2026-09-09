package com.example.payment_service.dto.response;

import com.example.payment_service.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {

    private UUID paymentId;

    private UUID merchantId;

    private String orderId;

    private BigDecimal amount;

    private String currency;

    private PaymentStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}