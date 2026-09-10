package com.example.payment_service.service;

import com.example.payment_service.dto.request.CreatePaymentRequest;
import com.example.payment_service.entity.IdempotencyRecord;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.repository.IdempotencyRecordRepository;
import com.example.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCreationService {

    private final PaymentRepository paymentRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Payment createPayment(
            CreatePaymentRequest request,
            String idempotencyKey,
            String requestHash) {

        Payment payment = Payment.builder()
                .merchantId(request.getMerchantId())
                .orderId(request.getOrderId().trim())
                .amount(request.getAmount())
                .currency(request.getCurrency().trim().toUpperCase())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        IdempotencyRecord idempotencyRecord = IdempotencyRecord.builder()
                .merchantId(savedPayment.getMerchantId())
                .idempotencyKey(idempotencyKey)
                .requestHash(requestHash)
                .paymentId(savedPayment.getPaymentId())
                .build();

        try {
            idempotencyRecordRepository.saveAndFlush(idempotencyRecord);
        } catch (DataIntegrityViolationException ex) {
            throw ex;
        }

        return savedPayment;
    }
}