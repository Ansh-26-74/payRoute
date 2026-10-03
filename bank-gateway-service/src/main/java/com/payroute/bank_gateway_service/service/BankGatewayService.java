package com.payroute.bank_gateway_service.service;

import com.payroute.bank_gateway_service.dto.request.ChargeRequest;
import com.payroute.bank_gateway_service.dto.response.ChargeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BankGatewayService {

    private final BankGatewayConfigurationService configurationService;

    private final Map<UUID, ChargeResponse> paymentStatuses =
            new ConcurrentHashMap<>();

    public ChargeResponse charge(ChargeRequest request) {

        BankGatewayConfigurationService.GatewayConfiguration configuration =
                configurationService.getConfiguration();

        ChargeResponse response;

        if (configuration.isSuccess()) {

            response = ChargeResponse.builder()
                    .status("SUCCESS")
                    .build();

        } else {

            response = ChargeResponse.builder()
                    .status("FAILED")
                    .declineReason(configuration.getDeclineReason())
                    .build();
        }

        paymentStatuses.put(
                request.getPaymentId(),
                response
        );

        applyLatency(configuration.getLatencyMs());

        return response;
    }

    public ChargeResponse checkStatus(UUID paymentId) {

        ChargeResponse response =
                paymentStatuses.get(paymentId);

        if (response == null) {

            return ChargeResponse.builder()
                    .status("NOT_FOUND")
                    .build();
        }

        return response;
    }

    private void applyLatency(long latencyMs) {

        if (latencyMs == 0) {
            return;
        }

        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Gateway processing was interrupted",
                    ex
            );
        }
    }
}