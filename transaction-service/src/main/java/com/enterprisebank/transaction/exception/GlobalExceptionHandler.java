package com.enterprisebank.transaction.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalExceptionHandler(
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }


    // =====================================================
    // INSUFFICIENT FUNDS
    // =====================================================

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, Object>>
    handleInsufficientFunds(
            InsufficientFundsException exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    // =====================================================
    // VALIDATION ERRORS
    // =====================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
    handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult()
                        .getFieldErrors()) {

            validationErrors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                request.getRequestURI(),
                validationErrors
        );
    }


    // =====================================================
    // MISSING REQUEST HEADER
    // =====================================================

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>>
    handleMissingRequestHeader(
            MissingRequestHeaderException exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }


    // =====================================================
    // DOWNSTREAM ACCOUNT SERVICE HTTP ERROR
    // =====================================================

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<Map<String, Object>>
    handleRestClientResponseException(
            RestClientResponseException exception,
            HttpServletRequest request
    ) {

        HttpStatus status =
                HttpStatus.resolve(
                        exception
                                .getStatusCode()
                                .value()
                );

        if (status == null) {
            status =
                    HttpStatus.BAD_GATEWAY;
        }

        String message =
                extractDownstreamMessage(
                        exception
                );

        return buildResponse(
                status,
                message,
                request.getRequestURI(),
                null
        );
    }


    // =====================================================
    // ACCOUNT SERVICE CONNECTIVITY FAILURE
    // =====================================================

    @ExceptionHandler(AccountServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>>
    handleAccountServiceUnavailableException(
            AccountServiceUnavailableException exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }


    // =====================================================
    // OTHER ACCOUNT SERVICE FAILURE
    // =====================================================

    @ExceptionHandler(AccountServiceException.class)
    public ResponseEntity<Map<String, Object>>
    handleAccountServiceException(
            AccountServiceException exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }


    // =====================================================
    // ILLEGAL ARGUMENT
    // =====================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>>
    handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }


    // =====================================================
    // ILLEGAL STATE
    // =====================================================

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>>
    handleIllegalStateException(
            IllegalStateException exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }


    // =====================================================
    // FALLBACK
    // =====================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>>
    handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getRequestURI(),
                null
        );
    }


    // =====================================================
    // EXTRACT MESSAGE FROM DOWNSTREAM JSON
    // =====================================================

    private String extractDownstreamMessage(
            RestClientResponseException exception
    ) {

        String responseBody =
                exception.getResponseBodyAsString();

        if (responseBody == null
                || responseBody.isBlank()) {

            return exception.getMessage();
        }

        try {

            JsonNode root =
                    objectMapper.readTree(
                            responseBody
                    );

            JsonNode messageNode =
                    root.get("message");

            if (messageNode != null
                    && !messageNode.isNull()) {

                return messageNode.asText();
            }

        } catch (Exception ignored) {

            // Fall back to HTTP exception message.
        }

        return exception.getMessage();
    }


    // =====================================================
    // STANDARD ERROR RESPONSE
    // =====================================================

    private ResponseEntity<Map<String, Object>>
    buildResponse(
            HttpStatus status,
            String message,
            String path,
            Object validationErrors
    ) {

        Map<String, Object> body =
                new LinkedHashMap<>();

        body.put(
                "timestamp",
                LocalDateTime.now()
        );

        body.put(
                "status",
                status.value()
        );

        body.put(
                "error",
                status.getReasonPhrase()
        );

        body.put(
                "message",
                message
        );

        body.put(
                "path",
                path
        );

        if (validationErrors != null) {

            body.put(
                    "validationErrors",
                    validationErrors
            );
        }

        return ResponseEntity
                .status(status)
                .body(body);
    }
}