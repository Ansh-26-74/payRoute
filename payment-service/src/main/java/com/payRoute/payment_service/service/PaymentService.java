package com.payRoute.payment_service.service;

import com.payRoute.payment_service.dto.request.CreatePaymentRequest;
import com.payRoute.payment_service.dto.response.OrchestrationResponse;
import com.payRoute.payment_service.dto.response.PaymentResponse;
import com.payRoute.payment_service.entity.IdempotencyRecord;
import com.payRoute.payment_service.entity.Payment;
import com.payRoute.payment_service.entity.PaymentStatus;
import com.payRoute.payment_service.exception.IdempotencyKeyConflictException;
import com.payRoute.payment_service.exception.ResourceNotFoundException;
import com.payRoute.payment_service.repository.IdempotencyRecordRepository;
import com.payRoute.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import com.payRoute.payment_service.client.OrchestrationServiceClient;
import com.payRoute.payment_service.dto.request.OrchestratePaymentRequest;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final RequestHashService requestHashService;
    private final PaymentCreationService paymentCreationService;
    private final IdempotencyCacheService idempotencyCacheService;
    private final OrchestrationServiceClient orchestrationServiceClient;

    public PaymentResponse createPayment(
            CreatePaymentRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key is required");
        }

        idempotencyKey = idempotencyKey.trim();

        if (idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency-Key must not exceed 100 characters"
            );
        }

        String requestHash = requestHashService.generateHash(request);

        PaymentResponse cachedResponse = idempotencyCacheService.get(
                request.getMerchantId(),
                idempotencyKey,
                requestHash
        );

        if (cachedResponse != null) {

            Optional<Payment> cachedPayment =
                    paymentRepository.findById(cachedResponse.getPaymentId());

            if (cachedPayment.isPresent()) {
                return toPaymentResponse(cachedPayment.get());
            }
        }

        Optional<IdempotencyRecord> existingRecord =
                idempotencyRecordRepository.findByMerchantIdAndIdempotencyKey(
                        request.getMerchantId(),
                        idempotencyKey
                );

        if (existingRecord.isPresent()) {

            PaymentResponse response =
                    handleExistingRecord(
                            existingRecord.get(),
                            requestHash
                    );

            idempotencyCacheService.put(
                    request.getMerchantId(),
                    idempotencyKey,
                    requestHash,
                    response
            );

            return response;
        }

        try {
            Payment payment = paymentCreationService.createPayment(
                    request,
                    idempotencyKey,
                    requestHash
            );

            OrchestrationResponse orchestrationResponse =
                    orchestrationServiceClient.orchestrate(
                            OrchestratePaymentRequest.builder()
                                    .paymentId(payment.getPaymentId())
                                    .merchantId(payment.getMerchantId())
                                    .amount(payment.getAmount())
                                    .currency(payment.getCurrency())
                                    .build()
                    );

            updatePaymentStatus(payment, orchestrationResponse);

            PaymentResponse response = toPaymentResponse(payment);

            idempotencyCacheService.put(
                    request.getMerchantId(),
                    idempotencyKey,
                    requestHash,
                    response
            );

            return response;

        } catch (DataIntegrityViolationException ex) {

            IdempotencyRecord concurrentRecord =
                    idempotencyRecordRepository
                            .findByMerchantIdAndIdempotencyKey(
                                    request.getMerchantId(),
                                    idempotencyKey
                            )
                            .orElseThrow(() -> ex);

            PaymentResponse response =
                    handleExistingRecord(
                            concurrentRecord,
                            requestHash
                    );

            idempotencyCacheService.put(
                    request.getMerchantId(),
                    idempotencyKey,
                    requestHash,
                    response
            );

            return response;
        }
    }

    private void updatePaymentStatus(
            Payment payment,
            OrchestrationResponse orchestrationResponse) {

        payment.setStatus(
                PaymentStatus.valueOf(orchestrationResponse.getStatus())
        );

        paymentRepository.save(payment);
    }

    private PaymentResponse handleExistingRecord(
            IdempotencyRecord record,
            String requestHash) {

        if (!record.getRequestHash().equals(requestHash)) {
            throw new IdempotencyKeyConflictException(
                    "Idempotency key was already used with a different request"
            );
        }

        Payment payment = paymentRepository
                .findById(record.getPaymentId())
                .orElseThrow(() -> new IllegalStateException(
                        "Payment associated with idempotency record was not found"
                ));

        return toPaymentResponse(payment);
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .merchantId(payment.getMerchantId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public PaymentResponse getPayment(UUID paymentId, UUID merchantId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found"
                ));

        if (!payment.getMerchantId().equals(merchantId)) {
            throw new ResourceNotFoundException(
                    "Payment not found"
            );
        }

        return toPaymentResponse(payment);
    }
}