package com.payroute.sim_gateway_service.service;

import com.payroute.sim_gateway_service.dto.request.ChargeRequest;
import com.payroute.sim_gateway_service.dto.response.ChargeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimGatewayService {

    private final SimGatewayConfigurationService configurationService;

    public ChargeResponse charge(ChargeRequest request) {

        SimGatewayConfigurationService.GatewayConfiguration configuration =
                configurationService.getConfiguration();

        applyLatency(configuration.getLatencyMs());

        if (configuration.isSuccess()) {
            return ChargeResponse.builder()
                    .status("SUCCESS")
                    .build();
        }

        return ChargeResponse.builder()
                .status("FAILED")
                .declineReason(configuration.getDeclineReason())
                .build();
    }

    private void applyLatency(long latencyMs) {

        if (latencyMs == 0) {
            return;
        }

        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gateway processing was interrupted", ex);
        }
    }
}