package com.payroute.orchestration_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payment_attempts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_attempt_number",
                        columnNames = {"payment_id", "attempt_number"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID paymentId;

    @Column(nullable = false)
    private String gatewayId;

    @Column(nullable = false)
    private String status;

    private String declineReason;

    @Column(nullable = false)
    private Long latencyMs;

    @Column(nullable = false)
    private Integer attemptNumber;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}