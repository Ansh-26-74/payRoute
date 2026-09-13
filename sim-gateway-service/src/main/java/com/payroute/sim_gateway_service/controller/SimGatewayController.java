package com.payroute.sim_gateway_service.controller;

import com.payroute.sim_gateway_service.dto.request.ChargeRequest;
import com.payroute.sim_gateway_service.dto.response.ChargeResponse;
import com.payroute.sim_gateway_service.service.SimGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/charge")
@RequiredArgsConstructor
public class SimGatewayController {

    private final SimGatewayService simGatewayService;

    @PostMapping
    public ResponseEntity<ChargeResponse> charge(
            @Valid @RequestBody ChargeRequest request) {

        ChargeResponse response = simGatewayService.charge(request);

        return ResponseEntity.ok(response);
    }
}