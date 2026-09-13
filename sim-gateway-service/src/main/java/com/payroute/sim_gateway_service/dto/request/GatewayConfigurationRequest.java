package com.payroute.sim_gateway_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GatewayConfigurationRequest {

    @NotNull(message = "success is required")
    private Boolean success;

    @NotNull(message = "latencyMs is required")
    @Min(value = 0, message = "latencyMs cannot be negative")
    private Long latencyMs;

    private String declineReason;
}