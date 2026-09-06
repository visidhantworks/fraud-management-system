package com.sidhant.fraudmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPin(
            InvalidPinException exception) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "code", "INVALID_PIN",
                        "error", exception.getMessage()
                ));
    }

    @ExceptionHandler(TransactionFailedException.class)
    public ResponseEntity<Map<String, String>> handleTransactionFailed(
            TransactionFailedException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "code", "PAYMENT_FAILED",
                        "error", exception.getMessage()
                ));
    }

    @ExceptionHandler(TransactionBlockedException.class)
    public ResponseEntity<Map<String, String>> handleTransactionBlocked(
            TransactionBlockedException exception) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "code", "PAYMENT_BLOCKED",
                        "error", exception.getMessage()
                ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(
            UserNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "code", "USER_NOT_FOUND",
                        "error", exception.getMessage()
                ));
    }

    @ExceptionHandler(AdminPaymentException.class)
    public ResponseEntity<Map<String, String>> handleAdminPayment(
            AdminPaymentException exception) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "code", "ADMIN_PAYMENT_NOT_ALLOWED",
                        "error", exception.getMessage()
                ));
    }

    @ExceptionHandler(ActiveSessionException.class)
    public ResponseEntity<Map<String, String>> handleActiveSession(
            ActiveSessionException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "code", "ACTIVE_SESSION",
                        "error", exception.getMessage()
                ));
    }
}