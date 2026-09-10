package com.example.payment_service.repository;

import com.example.payment_service.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, UUID> {

    Optional<IdempotencyRecord> findByMerchantIdAndIdempotencyKey(
            UUID merchantId,
            String idempotencyKey
    );
}