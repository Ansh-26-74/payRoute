package com.payroute.merchant_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRequestDto {

    @NotBlank(message = "API key is required")
    private String apiKey;
}