package com.payroute.sim_gateway_service.controller;

import com.payroute.sim_gateway_service.dto.request.GatewayConfigurationRequest;
import com.payroute.sim_gateway_service.service.SimGatewayConfigurationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SimGatewayAdminController {

    private final SimGatewayConfigurationService configurationService;

    @PostMapping("/configure")
    public ResponseEntity<Void> configure(
            @Valid @RequestBody GatewayConfigurationRequest request) {

        configurationService.configure(request);

        return ResponseEntity.noContent().build();
    }
}