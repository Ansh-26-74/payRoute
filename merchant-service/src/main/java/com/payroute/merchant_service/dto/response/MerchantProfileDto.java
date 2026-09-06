package com.payroute.merchant_service.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantProfileDto {

    private UUID merchantId;
    private String name;
    private String email;
    private String webhookUrl;
}