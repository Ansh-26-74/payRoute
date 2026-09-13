package com.payroute.orchestration_service.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrchestrationResponse {

    private String status;
    private String gatewayUsed;
    private String declineReason;
}

