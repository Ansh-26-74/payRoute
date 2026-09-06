package com.payroute.merchant_service.security;

import com.payroute.merchant_service.exception.InvalidApiKeyException;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class ApiKeyGenerator {

    private static final String PREFIX = "pr_live_";
    private static final int KEY_ID_LENGTH = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateKeyId() {
        byte[] randomBytes = new byte[12];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    public String generateSecret() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    public String buildApiKey(String keyId, String secret) {
        return PREFIX + keyId + "_" + secret;
    }

    public ApiKeyParts parseApiKey(String apiKey) {
        if (apiKey == null || !apiKey.startsWith(PREFIX)) {
            throw new InvalidApiKeyException("Invalid API key");
        }

        String value = apiKey.substring(PREFIX.length());

        if (value.length() <= KEY_ID_LENGTH) {
            throw new InvalidApiKeyException("Invalid API key");
        }

        if (value.charAt(KEY_ID_LENGTH) != '_') {
            throw new InvalidApiKeyException("Invalid API key");
        }

        String keyId = value.substring(0, KEY_ID_LENGTH);
        String secret = value.substring(KEY_ID_LENGTH + 1);

        if (secret.isBlank()) {
            throw new InvalidApiKeyException("Invalid API key");
        }

        return new ApiKeyParts(keyId, secret);
    }

    public record ApiKeyParts(String keyId, String secret) {
    }
}