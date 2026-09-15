package com.payroute.orchestration_service.controller;

import com.payroute.orchestration_service.dto.request.OrchestratePaymentRequest;
import com.payroute.orchestration_service.dto.response.OrchestrationResponse;
import com.payroute.orchestration_service.service.OrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orchestrate")
@RequiredArgsConstructor
public class OrchestrationController {

    private final OrchestrationService orchestrationService;

    @PostMapping
    public ResponseEntity<OrchestrationResponse> orchestrate(
            @Valid @RequestBody OrchestratePaymentRequest request) {

        return ResponseEntity.ok(
                orchestrationService.orchestrate(request)
        );
    }
}