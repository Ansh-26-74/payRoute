package com.payroute.merchant_service.controller;

import com.payroute.merchant_service.dto.request.TokenRequestDto;
import com.payroute.merchant_service.dto.response.TokenResponseDto;
import com.payroute.merchant_service.service.MerchantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MerchantService merchantService;

    @PostMapping("/token")
    public ResponseEntity<TokenResponseDto> generateToken(
            @Valid @RequestBody TokenRequestDto request) {

        TokenResponseDto response =
                merchantService.generateToken(request);

        return ResponseEntity.ok(response);
    }
}