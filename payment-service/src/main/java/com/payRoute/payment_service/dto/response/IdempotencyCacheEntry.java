package com.payRoute.payment_service.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyCacheEntry {

    private String requestHash;
    private PaymentResponse response;
}