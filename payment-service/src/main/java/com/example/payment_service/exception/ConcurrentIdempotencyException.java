package com.example.payment_service.exception;

public class ConcurrentIdempotencyException extends RuntimeException {
    public ConcurrentIdempotencyException() {
        super("Concurrent idempotency request detected");
    }
}