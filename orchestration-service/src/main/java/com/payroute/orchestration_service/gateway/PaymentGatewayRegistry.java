package com.payroute.orchestration_service.gateway;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentGatewayRegistry {

    private final Map<String, PaymentGateway> gateways;

    public PaymentGatewayRegistry(List<PaymentGateway> gatewayList) {
        this.gateways = gatewayList.stream()
                .collect(Collectors.toMap(
                        PaymentGateway::gatewayId,
                        Function.identity()
                ));
    }

    public PaymentGateway getGateway(String gatewayId) {
        PaymentGateway gateway = gateways.get(gatewayId);

        if (gateway == null) {
            throw new IllegalArgumentException(
                    "Unsupported payment gateway: " + gatewayId
            );
        }

        return gateway;
    }
}