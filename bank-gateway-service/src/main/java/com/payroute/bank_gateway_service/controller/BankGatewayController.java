package com.payroute.bank_gateway_service.controller;

import com.payroute.bank_gateway_service.dto.request.ChargeRequest;
import com.payroute.bank_gateway_service.dto.response.ChargeResponse;
import com.payroute.bank_gateway_service.service.BankGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/charge")
@RequiredArgsConstructor
public class BankGatewayController {

    private final BankGatewayService bankGatewayService;

    @PostMapping
    public ResponseEntity<ChargeResponse> charge(
            @Valid @RequestBody ChargeRequest request) {

        ChargeResponse response = bankGatewayService.charge(request);

        return ResponseEntity.ok(response);
    }
}