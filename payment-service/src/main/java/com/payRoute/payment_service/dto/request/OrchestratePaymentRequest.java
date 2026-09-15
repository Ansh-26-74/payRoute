package com.payRoute.payment_service.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrchestratePaymentRequest {
    private UUID paymentId;
    private UUID merchantId;
    private BigDecimal amount;
    private String currency;
}