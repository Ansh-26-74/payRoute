package com.example.payment_service.service;

import com.example.payment_service.dto.request.CreatePaymentRequest;
import com.example.payment_service.dto.response.PaymentResponse;
import com.example.payment_service.entity.IdempotencyRecord;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.exception.IdempotencyKeyConflictException;
import com.example.payment_service.repository.IdempotencyRecordRepository;
import com.example.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final RequestHashService requestHashService;
    private final PaymentCreationService paymentCreationService;

    public PaymentResponse createPayment(
            CreatePaymentRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key is required");
        }

        idempotencyKey = idempotencyKey.trim();

        if (idempotencyKey.length() > 100) {
            throw new IllegalArgumentException("Idempotency-Key must not exceed 100 characters");
        }

        String requestHash = requestHashService.generateHash(request);

        Optional<IdempotencyRecord> existingRecord =
                idempotencyRecordRepository.findByMerchantIdAndIdempotencyKey(
                        request.getMerchantId(),
                        idempotencyKey
                );

        if (existingRecord.isPresent()) {
            return handleExistingRecord(existingRecord.get(), requestHash);
        }

        try {
            Payment payment = paymentCreationService.createPayment(
                    request,
                    idempotencyKey,
                    requestHash
            );

            return toPaymentResponse(payment);

        } catch (DataIntegrityViolationException ex) {

            IdempotencyRecord concurrentRecord =
                    idempotencyRecordRepository
                            .findByMerchantIdAndIdempotencyKey(
                                    request.getMerchantId(),
                                    idempotencyKey
                            )
                            .orElseThrow(() -> ex);

            return handleExistingRecord(concurrentRecord, requestHash);
        }
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
}