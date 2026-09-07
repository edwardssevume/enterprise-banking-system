package com.enterprisebank.notification.exception;

public class NotificationAccessDeniedException
        extends RuntimeException {

    public NotificationAccessDeniedException(
            String message
    ) {
        super(message);
    }
}