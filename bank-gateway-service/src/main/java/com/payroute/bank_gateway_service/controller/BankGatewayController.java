package com.payroute.bank_gateway_service.controller;

import com.payroute.bank_gateway_service.dto.request.ChargeRequest;
import com.payroute.bank_gateway_service.dto.response.ChargeResponse;
import com.payroute.bank_gateway_service.service.BankGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BankGatewayController {

    private final BankGatewayService bankGatewayService;

    @PostMapping("/charge")
    public ResponseEntity<ChargeResponse> charge(
            @Valid @RequestBody ChargeRequest request) {

        ChargeResponse response =
                bankGatewayService.charge(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/{paymentId}/status")
    public ResponseEntity<ChargeResponse> checkStatus(
            @PathVariable UUID paymentId) {

        return ResponseEntity.ok(
                bankGatewayService.checkStatus(paymentId)
        );
    }
}