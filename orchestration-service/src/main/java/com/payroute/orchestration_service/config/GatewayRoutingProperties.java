package com.payroute.orchestration_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment.gateway")
public class GatewayRoutingProperties {

    private List<String> enabled = List.of();

    private double minimumSuccessRate = 0.80;

    private int minimumAttempts = 5;

    private Recovery recovery = new Recovery();

    @Getter
    @Setter
    public static class Recovery {

        private long cooldownSeconds = 300;

        private int probeAttempts = 5;
    }
}