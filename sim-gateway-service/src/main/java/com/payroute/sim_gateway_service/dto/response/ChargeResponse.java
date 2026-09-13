package com.payroute.sim_gateway_service.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChargeResponse {

    private String status;
    private String declineReason;
}