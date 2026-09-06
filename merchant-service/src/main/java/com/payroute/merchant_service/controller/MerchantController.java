package com.payroute.merchant_service.controller;

import com.payroute.merchant_service.dto.request.MerchantRegisterRequestDto;
import com.payroute.merchant_service.dto.response.MerchantProfileDto;
import com.payroute.merchant_service.dto.response.MerchantRegisterResponseDto;
import com.payroute.merchant_service.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping("/register")
    public ResponseEntity<MerchantRegisterResponseDto> registerMerchant(
            @Valid @RequestBody MerchantRegisterRequestDto request) {

        MerchantRegisterResponseDto response =
                merchantService.registerMerchant(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<MerchantProfileDto> getCurrentMerchant(
            Authentication authentication) {

        MerchantProfileDto response =
                merchantService.getCurrentMerchant(authentication.getName());

        return ResponseEntity.ok(response);
    }
}