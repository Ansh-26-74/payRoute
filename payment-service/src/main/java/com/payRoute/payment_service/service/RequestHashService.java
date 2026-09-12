package com.payRoute.payment_service.service;

import com.payRoute.payment_service.dto.request.CreatePaymentRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class RequestHashService {

    public String generateHash(CreatePaymentRequest request) {

        String canonicalRequest =
                request.getMerchantId() + "|" +
                        request.getOrderId().trim() + "|" +
                        normalizeAmount(request.getAmount()) + "|" +
                        request.getCurrency().trim().toUpperCase();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    canonicalRequest.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder result = new StringBuilder();

            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private String normalizeAmount(BigDecimal amount) {
        return amount.stripTrailingZeros().toPlainString();
    }
}