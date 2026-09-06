package com.payroute.merchant_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantRegisterRequestDto {

    @NotBlank(message = "Merchant name is required")
    @Size(max = 100, message = "Merchant name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Webhook URL is required")
    @Size(max = 500, message = "Webhook URL must not exceed 500 characters")
    private String webhookUrl;
}