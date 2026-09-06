package com.payroute.merchant_service.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponseDto {

    private String jwt;
    private Instant expiresAt;
}