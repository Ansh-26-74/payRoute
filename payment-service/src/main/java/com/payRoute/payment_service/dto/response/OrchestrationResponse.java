package com.payRoute.payment_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrchestrationResponse {

    private UUID paymentId;
    private String status;
    private String gatewayUsed;
    private String declineReason;
}