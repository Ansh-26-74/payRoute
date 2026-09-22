package com.payroute.orchestration_service.repository;

import com.payroute.orchestration_service.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {

    List<PaymentAttempt> findByPaymentIdOrderByAttemptNumberAsc(UUID paymentId);

    PaymentAttempt findTopByPaymentIdOrderByAttemptNumberDesc(UUID paymentId);

    long countByGatewayId(String gatewayId);

    long countByGatewayIdAndStatus(String gatewayId, String status);

    @Query("""
    SELECT AVG(p.latencyMs)
    FROM PaymentAttempt p
    WHERE p.gatewayId = :gatewayId
""")
    Double findAverageLatencyByGatewayId(String gatewayId);
}