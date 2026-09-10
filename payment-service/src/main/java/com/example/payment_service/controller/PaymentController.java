package com.example.payment_service.controller;

import com.example.payment_service.dto.request.CreatePaymentRequest;
import com.example.payment_service.dto.response.PaymentResponse;
import com.example.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid CreatePaymentRequest request) {

        PaymentResponse response = paymentService.createPayment(request, idempotencyKey);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}