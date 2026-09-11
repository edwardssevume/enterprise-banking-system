package com.enterprisebank.fraud.exception;

public class FraudAssessmentNotFoundException
        extends RuntimeException {

    public FraudAssessmentNotFoundException(String message) {
        super(message);
    }
}