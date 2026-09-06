package com.payroute.merchant_service.repository;

import com.payroute.merchant_service.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    boolean existsByEmail(String email);

    Optional<Merchant> findByApiKeyId(String apiKeyId);
}