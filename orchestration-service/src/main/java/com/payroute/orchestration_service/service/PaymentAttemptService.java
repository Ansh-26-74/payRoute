package com.payroute.orchestration_service.service;

import com.payroute.orchestration_service.entity.PaymentAttempt;
import com.payroute.orchestration_service.repository.PaymentAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentAttemptService {

    private final PaymentAttemptRepository paymentAttemptRepository;

    public PaymentAttempt recordAttempt(
            UUID paymentId,
            String gatewayId,
            String status,
            String declineReason,
            Long latencyMs
    ) {
        PaymentAttempt lastAttempt =
                paymentAttemptRepository.findTopByPaymentIdOrderByAttemptNumberDesc(paymentId);

        int attemptNumber = lastAttempt == null
                ? 1
                : lastAttempt.getAttemptNumber() + 1;

        PaymentAttempt paymentAttempt = PaymentAttempt.builder()
                .paymentId(paymentId)
                .gatewayId(gatewayId)
                .status(status)
                .declineReason(declineReason)
                .latencyMs(latencyMs)
                .attemptNumber(attemptNumber)
                .build();

        return paymentAttemptRepository.save(paymentAttempt);
    }
}