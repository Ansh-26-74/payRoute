package com.payroute.merchant_service.service;

import com.payroute.merchant_service.dto.request.MerchantRegisterRequestDto;
import com.payroute.merchant_service.dto.request.TokenRequestDto;
import com.payroute.merchant_service.dto.response.MerchantProfileDto;
import com.payroute.merchant_service.dto.response.MerchantRegisterResponseDto;
import com.payroute.merchant_service.dto.response.TokenResponseDto;
import com.payroute.merchant_service.entity.Merchant;
import com.payroute.merchant_service.exception.InvalidApiKeyException;
import com.payroute.merchant_service.exception.MerchantAlreadyExistsException;
import com.payroute.merchant_service.repository.MerchantRepository;
import com.payroute.merchant_service.security.ApiKeyGenerator;
import com.payroute.merchant_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApiKeyGenerator apiKeyGenerator;
    private final JwtService jwtService;

    public MerchantRegisterResponseDto registerMerchant(
            MerchantRegisterRequestDto request) {

        if (merchantRepository.existsByEmail(request.getEmail())) {
            throw new MerchantAlreadyExistsException(
                    "Merchant already exists with email: " + request.getEmail()
            );
        }

        String keyId = apiKeyGenerator.generateKeyId();
        String secret = apiKeyGenerator.generateSecret();
        String apiKey = apiKeyGenerator.buildApiKey(keyId, secret);

        String apiKeyHash = passwordEncoder.encode(secret);

        Merchant merchant = Merchant.builder()
                .name(request.getName())
                .email(request.getEmail())
                .apiKeyId(keyId)
                .apiKeyHash(apiKeyHash)
                .webhookUrl(request.getWebhookUrl())
                .build();

        Merchant savedMerchant = merchantRepository.save(merchant);

        return MerchantRegisterResponseDto.builder()
                .merchantId(savedMerchant.getId())
                .apiKey(apiKey)
                .build();
    }

    public TokenResponseDto generateToken(TokenRequestDto request) {

        ApiKeyGenerator.ApiKeyParts parts =
                apiKeyGenerator.parseApiKey(request.getApiKey());

        Merchant merchant = merchantRepository
                .findByApiKeyId(parts.keyId())
                .orElseThrow(() ->
                        new InvalidApiKeyException("Invalid API key"));

        boolean validSecret = passwordEncoder.matches(
                parts.secret(),
                merchant.getApiKeyHash()
        );

        if (!validSecret) {
            throw new InvalidApiKeyException("Invalid API key");
        }

        String jwt = jwtService.generateToken(merchant.getId());

        return TokenResponseDto.builder()
                .jwt(jwt)
                .expiresAt(jwtService.getExpirationTime())
                .build();
    }

    public MerchantProfileDto getCurrentMerchant(String merchantId) {
        Merchant merchant = merchantRepository.findById(UUID.fromString(merchantId))
                .orElseThrow(() -> new InvalidApiKeyException("Merchant not found"));

        return MerchantProfileDto.builder()
                .merchantId(merchant.getId())
                .name(merchant.getName())
                .email(merchant.getEmail())
                .webhookUrl(merchant.getWebhookUrl())
                .build();
    }
}