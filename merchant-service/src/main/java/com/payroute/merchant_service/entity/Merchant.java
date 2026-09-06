package com.payroute.merchant_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "merchants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_merchants_email",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "api_key_id", nullable = false, unique = true)
    private String apiKeyId;

    @Column(name = "api_key_hash", nullable = false)
    private String apiKeyHash;

    @Column(name = "webhook_url", nullable = false, length = 500)
    private String webhookUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}