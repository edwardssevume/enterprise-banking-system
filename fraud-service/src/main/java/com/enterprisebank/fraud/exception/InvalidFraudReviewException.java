package com.enterprisebank.fraud.exception;

public class InvalidFraudReviewException
        extends RuntimeException {

    public InvalidFraudReviewException(String message) {
        super(message);
    }
}