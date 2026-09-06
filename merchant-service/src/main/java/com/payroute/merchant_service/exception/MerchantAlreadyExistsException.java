package com.payroute.merchant_service.exception;

public class MerchantAlreadyExistsException extends RuntimeException {

    public MerchantAlreadyExistsException(String message) {
        super(message);
    }
}