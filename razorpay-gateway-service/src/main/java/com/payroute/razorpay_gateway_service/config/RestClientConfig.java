package com.payroute.razorpay_gateway_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final RazorpayConfig razorpayConfig;

    @Bean
    public RestClient razorpayRestClient() {

        return RestClient.builder()
                .baseUrl(razorpayConfig.getBaseUrl())
                .defaultHeaders(headers ->
                        headers.setBasicAuth(
                                razorpayConfig.getKeyId(),
                                razorpayConfig.getKeySecret()
                        )
                )
                .build();
    }
}