package com.payroute.sim_gateway_service.service;

import com.payroute.sim_gateway_service.dto.request.GatewayConfigurationRequest;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
public class SimGatewayConfigurationService {

    private boolean success = true;
    private long latencyMs = 0;
    private String declineReason;

    public synchronized void configure(GatewayConfigurationRequest request) {
        this.success = request.getSuccess();
        this.latencyMs = request.getLatencyMs();
        this.declineReason = request.getDeclineReason();
    }

    public synchronized GatewayConfiguration getConfiguration() {
        return new GatewayConfiguration(
                success,
                latencyMs,
                declineReason
        );
    }

    @Getter
    public static class GatewayConfiguration {

        private final boolean success;
        private final long latencyMs;
        private final String declineReason;

        public GatewayConfiguration(
                boolean success,
                long latencyMs,
                String declineReason) {

            this.success = success;
            this.latencyMs = latencyMs;
            this.declineReason = declineReason;
        }
    }
}