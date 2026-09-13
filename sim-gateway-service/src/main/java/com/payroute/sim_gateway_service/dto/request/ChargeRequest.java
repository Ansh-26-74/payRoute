package com.payroute.sim_gateway_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ChargeRequest {

    @NotNull(message = "paymentId is required")
    private UUID paymentId;

    @NotNull(message = "merchantId is required")
    private UUID merchantId;

    @NotNull(message = "amount is required")
    private BigDecimal amount;

    @NotNull(message = "currency is required")
    private String currency;
}