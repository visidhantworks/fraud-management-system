package com.sidhant.fraudmanagement.exception;

public class TransactionBlockedException extends RuntimeException {

    public TransactionBlockedException(String message) {
        super(message);
    }
}