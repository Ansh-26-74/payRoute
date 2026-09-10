package com.example.payment_service.service;

import com.example.payment_service.dto.request.CreatePaymentRequest;
import com.example.payment_service.dto.response.PaymentResponse;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentResponse createPayment(CreatePaymentRequest request) {

        Payment payment = new Payment();

        payment.setMerchantId(request.getMerchantId());
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.builder()
                .paymentId(savedPayment.getPaymentId())
                .merchantId(savedPayment.getMerchantId())
                .orderId(savedPayment.getOrderId())
                .amount(savedPayment.getAmount())
                .currency(savedPayment.getCurrency())
                .status(savedPayment.getStatus())
                .createdAt(savedPayment.getCreatedAt())
                .updatedAt(savedPayment.getUpdatedAt())
                .build();
    }
}